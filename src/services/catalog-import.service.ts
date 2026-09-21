import 'server-only';
import { z } from 'zod';
import { getPrisma } from '@/lib/prisma';

const units = ['GRAM', 'KILOGRAM', 'MILLILITER', 'CENTILITER', 'LITER', 'PIECE', 'SLICE', 'CAN'] as const;
const categories = ['PROTEIN', 'STARCH', 'VEGETABLE', 'GROCERY', 'DAIRY', 'OTHER'] as const;

const ingredientSchema = z.object({
  name: z.string().trim().min(1).max(100),
  category: z.enum(categories),
  useInComposedMeals: z.boolean(),
  portionPerPerson: z.number().positive().nullable(),
  unit: z.enum(units).nullable(),
  aisle: z.string().trim().min(1).max(100).nullable(),
});

const recipeSchema = z.object({
  name: z.string().trim().min(1).max(120),
  basePortions: z.number().int().min(1).max(30),
  ingredients: z.array(z.object({
    name: z.string().trim().min(1).max(100),
    quantity: z.number().positive(),
    unit: z.enum(units),
  })).min(1),
});

export const catalogEnrichmentSchema = z.object({
  format: z.literal('menu-de-la-semaine-enrichment'),
  version: z.literal(1),
  ingredients: z.array(ingredientSchema).max(500),
  recipes: z.array(recipeSchema).max(500),
});

export type CatalogEnrichment = z.infer<typeof catalogEnrichmentSchema>;

function ensureUnique(values: string[], label: string) {
  if (new Set(values).size !== values.length) throw new Error(`${label} contient des doublons.`);
}

export async function previewCatalogEnrichment(input: unknown) {
  const document = catalogEnrichmentSchema.parse(input);
  ensureUnique(document.ingredients.map((item) => item.name), 'La liste des ingrédients');
  ensureUnique(document.recipes.map((item) => item.name), 'La liste des recettes');
  for (const recipe of document.recipes) {
    ensureUnique(recipe.ingredients.map((item) => item.name), `La recette « ${recipe.name} »`);
  }

  const prisma = getPrisma();
  const [knownIngredients, knownRecipes, aisles] = await Promise.all([
    prisma.ingredient.findMany({ select: { name: true } }),
    prisma.recipe.findMany({ select: { name: true } }),
    prisma.aisle.findMany({ select: { name: true } }),
  ]);
  const ingredientNames = new Set(knownIngredients.map((item) => item.name));
  const recipeNames = new Set(knownRecipes.map((item) => item.name));
  const aisleNames = new Set(aisles.map((item) => item.name));
  const declaredNames = new Set(document.ingredients.map((item) => item.name));
  const missingReferences = document.recipes.flatMap((recipe) =>
    recipe.ingredients
      .filter((item) => !ingredientNames.has(item.name) && !declaredNames.has(item.name))
      .map((item) => `${recipe.name} : ${item.name}`),
  );
  const unknownAisles = document.ingredients
    .map((item) => item.aisle)
    .filter((name): name is string => name !== null)
    .filter((name) => !aisleNames.has(name));
  if (missingReferences.length) throw new Error(`Ingrédients absents : ${missingReferences.join(', ')}.`);
  if (unknownAisles.length) throw new Error(`Rayons inconnus : ${[...new Set(unknownAisles)].join(', ')}.`);

  return {
    document,
    summary: {
      ingredientsToCreate: document.ingredients.filter((item) => !ingredientNames.has(item.name)).map((item) => item.name),
      ingredientsToUpdate: document.ingredients.filter((item) => ingredientNames.has(item.name)).map((item) => item.name),
      recipesToCreate: document.recipes.filter((item) => !recipeNames.has(item.name)).map((item) => item.name),
      recipesToUpdate: document.recipes.filter((item) => recipeNames.has(item.name)).map((item) => item.name),
    },
  };
}

export async function applyCatalogEnrichment(input: unknown) {
  const { document, summary } = await previewCatalogEnrichment(input);
  const prisma = getPrisma();
  await prisma.$transaction(async (transaction) => {
    const aisles = await transaction.aisle.findMany();
    const aisleIds = new Map(aisles.map((aisle) => [aisle.name, aisle.id]));

    for (const item of document.ingredients) {
      await transaction.ingredient.upsert({
        where: { name: item.name },
        update: {
          category: item.category,
          useInComposedMeals: item.useInComposedMeals,
          portionPerPerson: item.portionPerPerson,
          unit: item.unit,
          aisleId: item.aisle ? aisleIds.get(item.aisle) : null,
          isActive: true,
        },
        create: {
          name: item.name,
          category: item.category,
          useInComposedMeals: item.useInComposedMeals,
          portionPerPerson: item.portionPerPerson,
          unit: item.unit,
          aisleId: item.aisle ? aisleIds.get(item.aisle) : null,
        },
      });
    }

    const ingredients = await transaction.ingredient.findMany({ select: { id: true, name: true } });
    const ingredientIds = new Map(ingredients.map((ingredient) => [ingredient.name, ingredient.id]));
    for (const item of document.recipes) {
      const ingredientsData = item.ingredients.map((entry) => ({
        ingredientId: ingredientIds.get(entry.name)!,
        quantity: entry.quantity,
        unit: entry.unit,
      }));
      const recipe = await transaction.recipe.findUnique({ where: { name: item.name }, select: { id: true } });
      if (recipe) {
        await transaction.recipeIngredient.deleteMany({ where: { recipeId: recipe.id } });
        await transaction.recipe.update({
          where: { id: recipe.id },
          data: { basePortions: item.basePortions, ingredients: { create: ingredientsData } },
        });
      } else {
        await transaction.recipe.create({
          data: { name: item.name, basePortions: item.basePortions, ingredients: { create: ingredientsData } },
        });
      }
    }
  });
  return summary;
}
