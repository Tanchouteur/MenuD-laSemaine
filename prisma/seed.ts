import 'dotenv/config';
import { PrismaPg } from '@prisma/adapter-pg';
import {
  IngredientCategory,
  PrismaClient,
  QuantityUnit,
  Season,
} from '../generated/prisma/client';

const connectionString = process.env.DATABASE_URL;

if (!connectionString) {
  throw new Error('DATABASE_URL est requise pour exécuter le seed.');
}

const prisma = new PrismaClient({
  adapter: new PrismaPg({ connectionString }),
});

const everySeason = [Season.WINTER, Season.SPRING, Season.SUMMER, Season.AUTUMN];

async function main() {
  await prisma.appSettings.upsert({
    where: { id: 'default' },
    update: {},
    create: { id: 'default' },
  });

  const aisleNames = [
    'Fruits et légumes',
    'Boucherie',
    'Poissonnerie',
    'Produits frais',
    'Épicerie',
  ];
  const aisles = new Map<string, string>();

  for (const [sortOrder, name] of aisleNames.entries()) {
    const aisle = await prisma.aisle.upsert({
      where: { name },
      update: { sortOrder },
      create: { name, sortOrder },
    });
    aisles.set(name, aisle.id);
  }

  const ingredientDefinitions = [
    ['Poulet', IngredientCategory.PROTEIN, 'poultry', 5, 150, QuantityUnit.GRAM, 'Boucherie'],
    ['Saumon', IngredientCategory.PROTEIN, 'fish', 5, 150, QuantityUnit.GRAM, 'Poissonnerie'],
    ['Œuf', IngredientCategory.PROTEIN, 'egg', 4, 2, QuantityUnit.PIECE, 'Produits frais'],
    ['Tofu', IngredientCategory.PROTEIN, 'vegetarian', 3, 140, QuantityUnit.GRAM, 'Produits frais'],
    ['Riz', IngredientCategory.STARCH, 'rice', 4, 70, QuantityUnit.GRAM, 'Épicerie'],
    ['Pâtes', IngredientCategory.STARCH, 'pasta', 5, 90, QuantityUnit.GRAM, 'Épicerie'],
    ['Pommes de terre', IngredientCategory.STARCH, 'potato', 4, 250, QuantityUnit.GRAM, 'Fruits et légumes'],
    ['Haricots verts', IngredientCategory.VEGETABLE, null, 4, 150, QuantityUnit.GRAM, 'Fruits et légumes'],
    ['Courgette', IngredientCategory.VEGETABLE, null, 4, 150, QuantityUnit.GRAM, 'Fruits et légumes'],
    ['Carotte', IngredientCategory.VEGETABLE, null, 4, 120, QuantityUnit.GRAM, 'Fruits et légumes'],
    ['Tomate', IngredientCategory.VEGETABLE, null, 5, 150, QuantityUnit.GRAM, 'Fruits et légumes'],
    ['Crème', IngredientCategory.DAIRY, null, 3, 5, QuantityUnit.CENTILITER, 'Produits frais'],
  ] as const;
  const ingredients = new Map<string, string>();

  for (const definition of ingredientDefinitions) {
    const [name, category, subFamily, rating, portionPerPerson, unit, aisleName] =
      definition;
    const ingredient = await prisma.ingredient.upsert({
      where: { name },
      update: {
        category,
        subFamily,
        rating,
        portionPerPerson,
        unit,
        aisleId: aisles.get(aisleName),
        isActive: true,
        okLunchWeekday: name === 'Saumon' ? false : true,
      },
      create: {
        name,
        category,
        subFamily,
        rating,
        portionPerPerson,
        unit,
        aisleId: aisles.get(aisleName),
        seasons: everySeason,
        okLunchWeekday: name === 'Saumon' ? false : true,
      },
    });
    ingredients.set(name, ingredient.id);
  }

  const omelette = await prisma.recipe.upsert({
    where: { name: 'Omelette aux fines herbes' },
    update: { rating: 4, prepTimeMinutes: 5, cookTimeMinutes: 10 },
    create: {
      name: 'Omelette aux fines herbes',
      style: 'pan-fried',
      rating: 4,
      prepTimeMinutes: 5,
      cookTimeMinutes: 10,
      basePortions: 4,
      seasons: everySeason,
      okLunchWeekend: false,
      okDinnerWeekend: false,
    },
  });

  const eggId = ingredients.get('Œuf');
  if (!eggId) throw new Error('L’ingrédient Œuf est absent du seed.');

  await prisma.recipeIngredient.upsert({
    where: {
      recipeId_ingredientId: {
        recipeId: omelette.id,
        ingredientId: eggId,
      },
    },
    update: { quantity: 8, unit: QuantityUnit.PIECE },
    create: {
      recipeId: omelette.id,
      ingredientId: eggId,
      quantity: 8,
      unit: QuantityUnit.PIECE,
    },
  });

  console.info(
    `Seed terminé : ${ingredients.size} ingrédients, ${aisles.size} rayons et 1 recette.`,
  );
}

main()
  .catch((error: unknown) => {
    console.error(error);
    process.exitCode = 1;
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
