import 'server-only';
import {
  IngredientCategory,
  QuantityUnit,
  Season,
  type Prisma,
} from '../../generated/prisma/client';
import { canonicalPair } from '@/engine';
import { getPrisma } from '@/lib/prisma';
import type { IngredientDto, RecipeDto } from '@/types/api';

export type IngredientInput = {
  name: string;
  category: IngredientCategory;
  subFamily?: string | null;
  rating: number;
  portionPerPerson?: number | null;
  unit?: QuantityUnit | null;
  aisleId?: string | null;
  seasons: Season[];
  okLunchWeekday: boolean;
  okDinnerWeekday: boolean;
  okLunchWeekend: boolean;
  okDinnerWeekend: boolean;
};

export type RecipeInput = {
  name: string;
  style?: string | null;
  rating: number;
  prepTimeMinutes?: number | null;
  cookTimeMinutes?: number | null;
  basePortions: number;
  seasons: Season[];
  okLunchWeekday: boolean;
  okDinnerWeekday: boolean;
  okLunchWeekend: boolean;
  okDinnerWeekend: boolean;
  ingredients: Array<{
    ingredientId: string;
    quantity: number;
    unit: QuantityUnit;
  }>;
};

function ingredientToDto(ingredient: {
  id: string;
  name: string;
  category: IngredientCategory;
  subFamily: string | null;
  rating: number;
  isActive: boolean;
  portionPerPerson: { toNumber(): number } | null;
  unit: QuantityUnit | null;
  aisleId: string | null;
  seasons: Season[];
  okLunchWeekday: boolean;
  okDinnerWeekday: boolean;
  okLunchWeekend: boolean;
  okDinnerWeekend: boolean;
}): IngredientDto {
  return {
    ...ingredient,
    category: ingredient.category,
    portionPerPerson: ingredient.portionPerPerson?.toNumber() ?? null,
    seasons: ingredient.seasons,
  };
}

export async function listIngredients(includeArchived = false): Promise<IngredientDto[]> {
  const ingredients = await getPrisma().ingredient.findMany({
    where: includeArchived ? {} : { isActive: true },
    orderBy: [{ category: 'asc' }, { name: 'asc' }],
  });
  return ingredients.map(ingredientToDto);
}

function validateIngredient(input: IngredientInput) {
  if (!input.name.trim()) throw new Error('Le nom est obligatoire.');
  if (input.rating < 1 || input.rating > 5) throw new Error('La note doit être entre 1 et 5.');
  if (input.seasons.length === 0) throw new Error('Choisissez au moins une saison.');
  if (input.portionPerPerson !== null && input.portionPerPerson !== undefined && input.portionPerPerson <= 0) {
    throw new Error('La portion doit être positive.');
  }
}

export async function createIngredient(input: IngredientInput): Promise<IngredientDto> {
  validateIngredient(input);
  const ingredient = await getPrisma().ingredient.create({
    data: { ...input, name: input.name.trim(), subFamily: input.subFamily?.trim() || null },
  });
  return ingredientToDto(ingredient);
}

export async function updateIngredient(
  id: string,
  input: Partial<IngredientInput> & { isActive?: boolean },
): Promise<IngredientDto> {
  const current = await getPrisma().ingredient.findUniqueOrThrow({ where: { id } });
  const merged: IngredientInput = {
    name: input.name ?? current.name,
    category: input.category ?? current.category,
    subFamily: input.subFamily === undefined ? current.subFamily : input.subFamily,
    rating: input.rating ?? current.rating,
    portionPerPerson:
      input.portionPerPerson === undefined
        ? current.portionPerPerson?.toNumber()
        : input.portionPerPerson,
    unit: input.unit === undefined ? current.unit : input.unit,
    aisleId: input.aisleId === undefined ? current.aisleId : input.aisleId,
    seasons: input.seasons ?? current.seasons,
    okLunchWeekday: input.okLunchWeekday ?? current.okLunchWeekday,
    okDinnerWeekday: input.okDinnerWeekday ?? current.okDinnerWeekday,
    okLunchWeekend: input.okLunchWeekend ?? current.okLunchWeekend,
    okDinnerWeekend: input.okDinnerWeekend ?? current.okDinnerWeekend,
  };
  validateIngredient(merged);
  const ingredient = await getPrisma().ingredient.update({
    where: { id },
    data: {
      ...merged,
      name: merged.name.trim(),
      subFamily: merged.subFamily?.trim() || null,
      isActive: input.isActive,
    },
  });
  return ingredientToDto(ingredient);
}

export async function archiveIngredient(id: string): Promise<void> {
  await getPrisma().ingredient.update({ where: { id }, data: { isActive: false } });
}

function recipeToDto(recipe: {
  id: string;
  name: string;
  style: string | null;
  rating: number;
  prepTimeMinutes: number | null;
  cookTimeMinutes: number | null;
  basePortions: number;
  isActive: boolean;
  seasons: Season[];
  okLunchWeekday: boolean;
  okDinnerWeekday: boolean;
  okLunchWeekend: boolean;
  okDinnerWeekend: boolean;
  ingredients: Array<{
    ingredientId: string;
    quantity: { toNumber(): number };
    unit: QuantityUnit;
    ingredient: { name: string };
  }>;
}): RecipeDto {
  return {
    id: recipe.id,
    name: recipe.name,
    style: recipe.style,
    rating: recipe.rating,
    prepTimeMinutes: recipe.prepTimeMinutes,
    cookTimeMinutes: recipe.cookTimeMinutes,
    basePortions: recipe.basePortions,
    isActive: recipe.isActive,
    seasons: recipe.seasons,
    ingredientCount: recipe.ingredients.length,
    okLunchWeekday: recipe.okLunchWeekday,
    okDinnerWeekday: recipe.okDinnerWeekday,
    okLunchWeekend: recipe.okLunchWeekend,
    okDinnerWeekend: recipe.okDinnerWeekend,
    ingredients: recipe.ingredients.map((item) => ({
      ingredientId: item.ingredientId,
      ingredientName: item.ingredient.name,
      quantity: item.quantity.toNumber(),
      unit: item.unit,
    })),
  };
}

export async function listRecipes(includeArchived = false): Promise<RecipeDto[]> {
  const recipes = await getPrisma().recipe.findMany({
    where: includeArchived ? {} : { isActive: true },
    include: { ingredients: { include: { ingredient: true } } },
    orderBy: { name: 'asc' },
  });
  return recipes.map(recipeToDto);
}

function validateRecipe(input: RecipeInput) {
  if (!input.name.trim()) throw new Error('Le nom est obligatoire.');
  if (input.rating < 1 || input.rating > 5) throw new Error('La note doit être entre 1 et 5.');
  if (input.basePortions < 1) throw new Error('Le nombre de portions doit être positif.');
  if (input.seasons.length === 0) throw new Error('Choisissez au moins une saison.');
  if (input.ingredients.length === 0) throw new Error('Ajoutez au moins un ingrédient.');
  if (new Set(input.ingredients.map((item) => item.ingredientId)).size !== input.ingredients.length) {
    throw new Error('Un ingrédient ne peut apparaître qu’une fois dans une recette.');
  }
}

export async function createRecipe(input: RecipeInput): Promise<RecipeDto> {
  validateRecipe(input);
  const recipe = await getPrisma().recipe.create({
    data: {
      name: input.name.trim(),
      style: input.style?.trim() || null,
      rating: input.rating,
      prepTimeMinutes: input.prepTimeMinutes,
      cookTimeMinutes: input.cookTimeMinutes,
      basePortions: input.basePortions,
      seasons: input.seasons,
      okLunchWeekday: input.okLunchWeekday,
      okDinnerWeekday: input.okDinnerWeekday,
      okLunchWeekend: input.okLunchWeekend,
      okDinnerWeekend: input.okDinnerWeekend,
      ingredients: { create: input.ingredients },
    },
    include: { ingredients: { include: { ingredient: true } } },
  });
  return recipeToDto(recipe);
}

export async function updateRecipe(
  id: string,
  input: RecipeInput,
): Promise<RecipeDto> {
  validateRecipe(input);
  const recipe = await getPrisma().$transaction(async (transaction) => {
    await transaction.recipeIngredient.deleteMany({ where: { recipeId: id } });
    return transaction.recipe.update({
      where: { id },
      data: {
        name: input.name.trim(),
        style: input.style?.trim() || null,
        rating: input.rating,
        prepTimeMinutes: input.prepTimeMinutes,
        cookTimeMinutes: input.cookTimeMinutes,
        basePortions: input.basePortions,
        seasons: input.seasons,
        okLunchWeekday: input.okLunchWeekday,
        okDinnerWeekday: input.okDinnerWeekday,
        okLunchWeekend: input.okLunchWeekend,
        okDinnerWeekend: input.okDinnerWeekend,
        ingredients: { create: input.ingredients },
      },
      include: { ingredients: { include: { ingredient: true } } },
    });
  });
  return recipeToDto(recipe);
}

export async function archiveRecipe(id: string): Promise<void> {
  await getPrisma().recipe.update({ where: { id }, data: { isActive: false } });
}

export async function listAisles() {
  return getPrisma().aisle.findMany({ orderBy: { sortOrder: 'asc' } });
}

export async function listIncompatibilities() {
  return getPrisma().incompatibility.findMany({
    include: { ingredient1: true, ingredient2: true },
    orderBy: { ingredient1: { name: 'asc' } },
  });
}

export async function addIncompatibility(firstId: string, secondId: string) {
  if (firstId === secondId) throw new Error('Choisissez deux ingrédients différents.');
  const [ingredientId1, ingredientId2] = canonicalPair(firstId, secondId).split('::');
  return getPrisma().incompatibility.upsert({
    where: { ingredientId1_ingredientId2: { ingredientId1, ingredientId2 } },
    update: {},
    create: { ingredientId1, ingredientId2 },
  });
}

export async function removeIncompatibility(id: string) {
  await getPrisma().incompatibility.delete({ where: { id } });
}

export async function getSettings() {
  return getPrisma().appSettings.upsert({
    where: { id: 'default' },
    update: {},
    create: { id: 'default' },
  });
}

export async function updateSettings(data: Prisma.AppSettingsUpdateInput) {
  const prisma = getPrisma();
  const settings = await prisma.appSettings.update({ where: { id: 'default' }, data });
  const emptyDraftSlots = await prisma.mealSlot.findMany({
    where: { slotType: 'EMPTY', weeklyPlan: { status: 'DRAFT' } },
    select: { id: true, mealDate: true, mealTime: true },
  });
  await prisma.$transaction(emptyDraftSlots.map((slot) => {
    const weekend = slot.mealDate.getUTCDay() === 0 || slot.mealDate.getUTCDay() === 6;
    const lunch = slot.mealTime === 'LUNCH';
    const guestCount = weekend
      ? lunch ? settings.defaultGuestsLunchWeekend : settings.defaultGuestsDinnerWeekend
      : lunch ? settings.defaultGuestsLunchWeekday : settings.defaultGuestsDinnerWeekday;
    return prisma.mealSlot.update({ where: { id: slot.id }, data: { guestCount } });
  }));
  return settings;
}
