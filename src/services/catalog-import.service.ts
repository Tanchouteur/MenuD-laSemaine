import 'server-only';
import { z } from 'zod';
import { getPrisma } from '@/lib/prisma';

const units = ['GRAM', 'KILOGRAM', 'MILLILITER', 'CENTILITER', 'LITER', 'PIECE', 'SLICE', 'CAN'] as const;
const categories = ['PROTEIN', 'STARCH', 'VEGETABLE', 'GROCERY', 'DAIRY', 'OTHER'] as const;

const ingredientSchema = z.object({
  name: z.string().trim().min(1).max(100),
  category: z.enum(categories),
  useInComposedMeals: z.boolean(),
  useAsStarter: z.boolean().optional(),
  isActive: z.boolean().optional(),
  portionPerPerson: z.number().positive().nullable(),
  unit: z.enum(units).nullable(),
  aisle: z.string().trim().min(1).max(100).nullable(),
});

const recipeSchema = z.object({
  name: z.string().trim().min(1).max(120),
  previousName: z.string().trim().min(1).max(120).optional(),
  role: z.enum(['MAIN', 'STARTER', 'SIDE_STARCH', 'SIDE_VEGETABLE']).optional(),
  allowStarchSide: z.boolean().optional(),
  allowVegetableSide: z.boolean().optional(),
  variantOfName: z.string().trim().min(1).max(120).nullable().optional(),
  style: z.string().trim().max(60).nullable().optional(),
  rating: z.number().int().min(1).max(5).optional(),
  prepTimeMinutes: z.number().int().nonnegative().nullable().optional(),
  cookTimeMinutes: z.number().int().nonnegative().nullable().optional(),
  basePortions: z.number().int().min(1).max(30),
  seasons: z.array(z.enum(['WINTER', 'SPRING', 'SUMMER', 'AUTUMN'])).min(1).optional(),
  allowedMoments: z.object({
    lunchWeekday: z.boolean(),
    dinnerWeekday: z.boolean(),
    lunchWeekend: z.boolean(),
    dinnerWeekend: z.boolean(),
  }).optional(),
  ingredients: z.array(z.object({
    name: z.string().trim().min(1).max(100),
    quantity: z.number().positive().nullable(),
    unit: z.enum(units),
  })).min(1),
});

export const catalogEnrichmentSchema = z.object({
  format: z.literal('menu-de-la-semaine-enrichment'),
  version: z.union([z.literal(1), z.literal(2)]),
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
  const pendingQuantities = document.recipes.flatMap((recipe) => recipe.ingredients
    .filter((entry) => entry.quantity === null)
    .map((entry) => `${recipe.name} : ${entry.name}`));
  const pendingPortions = document.ingredients.filter((item) => document.version === 2 && item.isActive !== false &&
    (item.useInComposedMeals || item.useAsStarter) && (item.portionPerPerson === null || item.unit === null))
    .map((item) => item.name);
  const missingVariants = document.recipes.filter((recipe) => recipe.variantOfName &&
    !recipeNames.has(recipe.variantOfName) && !document.recipes.some((item) => item.name === recipe.variantOfName));
  if (missingVariants.length) throw new Error(`Recette de référence absente : ${missingVariants.map((item) => item.variantOfName).join(', ')}.`);
  for (const recipe of document.recipes) {
    if (recipe.previousName && recipeNames.has(recipe.name) && recipe.previousName !== recipe.name) {
      throw new Error(`Impossible de renommer « ${recipe.previousName} » : « ${recipe.name} » existe déjà.`);
    }
  }

  return {
    document,
    summary: {
      ingredientsToCreate: document.ingredients.filter((item) => !ingredientNames.has(item.name)).map((item) => item.name),
      ingredientsToUpdate: document.ingredients.filter((item) => ingredientNames.has(item.name)).map((item) => item.name),
      ingredientsToArchive: document.ingredients.filter((item) => item.isActive === false).map((item) => item.name),
      recipesToCreate: document.recipes.filter((item) => !recipeNames.has(item.previousName ?? item.name)).map((item) => item.name),
      recipesToUpdate: document.recipes.filter((item) => recipeNames.has(item.previousName ?? item.name)).map((item) => item.name),
      pendingQuantities,
      pendingPortions,
    },
  };
}

export async function applyCatalogEnrichment(input: unknown) {
  const { document, summary } = await previewCatalogEnrichment(input);
  if (summary.pendingQuantities.length || summary.pendingPortions.length) throw new Error(`Quantités à vérifier avant application : ${[...summary.pendingQuantities, ...summary.pendingPortions].join(', ')}.`);
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
          useAsStarter: item.useAsStarter,
          portionPerPerson: item.portionPerPerson,
          unit: item.unit,
          aisleId: item.aisle ? aisleIds.get(item.aisle) : null,
          isActive: item.isActive ?? true,
        },
        create: {
          name: item.name,
          category: item.category,
          useInComposedMeals: item.useInComposedMeals,
          useAsStarter: item.useAsStarter ?? false,
          portionPerPerson: item.portionPerPerson,
          unit: item.unit,
          aisleId: item.aisle ? aisleIds.get(item.aisle) : null,
          isActive: item.isActive ?? true,
        },
      });
    }

    const ingredients = await transaction.ingredient.findMany({ select: { id: true, name: true } });
    const ingredientIds = new Map(ingredients.map((ingredient) => [ingredient.name, ingredient.id]));
    for (const item of document.recipes) {
      const ingredientsData = item.ingredients.map((entry) => ({
        ingredientId: ingredientIds.get(entry.name)!,
        quantity: entry.quantity!,
        unit: entry.unit,
      }));
      const recipe = await transaction.recipe.findUnique({ where: { name: item.previousName ?? item.name }, select: { id: true, role: true } });
      if (recipe && item.role && recipe.role !== item.role) {
        const used = await transaction.mealSlot.count({ where: { OR: [{ recipeId: recipe.id }, { starterRecipeId: recipe.id }, { starchRecipeId: recipe.id }, { vegetableRecipeId: recipe.id }] } });
        if (used) throw new Error(`Le rôle de « ${item.name} » ne peut pas changer car la recette est déjà utilisée.`);
      }
      const metadata = {
        name: item.name,
        role: item.role,
        allowStarchSide: item.allowStarchSide,
        allowVegetableSide: item.allowVegetableSide,
        basePortions: item.basePortions,
        style: item.style,
        rating: item.rating,
        prepTimeMinutes: item.prepTimeMinutes,
        cookTimeMinutes: item.cookTimeMinutes,
        seasons: item.seasons,
        okLunchWeekday: item.allowedMoments?.lunchWeekday,
        okDinnerWeekday: item.allowedMoments?.dinnerWeekday,
        okLunchWeekend: item.allowedMoments?.lunchWeekend,
        okDinnerWeekend: item.allowedMoments?.dinnerWeekend,
      };
      if (recipe) {
        await transaction.recipeIngredient.deleteMany({ where: { recipeId: recipe.id } });
        await transaction.recipe.update({
          where: { id: recipe.id },
          data: { ...metadata, ingredients: { create: ingredientsData } },
        });
      } else {
        await transaction.recipe.create({
          data: { ...metadata, ingredients: { create: ingredientsData } },
        });
      }
    }
    for (const item of document.recipes.filter((recipe) => recipe.variantOfName !== undefined)) {
      const child = await transaction.recipe.findUniqueOrThrow({ where: { name: item.name } });
      const parent = item.variantOfName ? await transaction.recipe.findUniqueOrThrow({ where: { name: item.variantOfName } }) : null;
      if (parent && parent.role !== child.role) throw new Error('Les variantes doivent avoir le même rôle.');
      if (parent?.id === child.id) throw new Error('Une recette ne peut pas être sa propre variante.');
      await transaction.recipe.update({ where: { id: child.id }, data: { variantOfId: parent?.variantOfId ?? parent?.id ?? null } });
    }
    const linkedRecipes = await transaction.recipe.findMany({ where: { variantOfId: { not: null } }, select: { id: true, name: true, role: true, variantOfId: true } });
    for (const child of linkedRecipes) {
      const parent = await transaction.recipe.findUniqueOrThrow({ where: { id: child.variantOfId! }, select: { role: true, variantOfId: true } });
      if (parent.role !== child.role || parent.variantOfId) {
        throw new Error(`Le lien de variante de « ${child.name} » n’est plus cohérent.`);
      }
    }
  });
  return summary;
}
