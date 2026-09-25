import 'server-only';
import type { Ingredient, Recipe, RecipeIngredient, Season } from '../../generated/prisma/client';
import type { AllowedMoments, MealCandidate, Season as EngineSeason } from '@/engine';
import { canonicalPair } from '@/engine';
import type { MealSnapshot } from '@/domain/meal-snapshot';
import { getPrisma } from '@/lib/prisma';

const seasonMap: Record<Season, EngineSeason> = {
  WINTER: 'winter', SPRING: 'spring', SUMMER: 'summer', AUTUMN: 'autumn',
};

type RecipeWithIngredients = Recipe & {
  ingredients: (RecipeIngredient & { ingredient: Ingredient })[];
};
type Side = {
  id: string;
  kind: 'ingredient' | 'recipe';
  name: string;
  rating: number;
  seasons: Season[];
  moments: AllowedMoments;
  ingredientIds: string[];
  family?: string;
};

function moments(item: Pick<Ingredient, 'okLunchWeekday' | 'okDinnerWeekday' | 'okLunchWeekend' | 'okDinnerWeekend'>): AllowedMoments {
  return {
    lunchWeekday: item.okLunchWeekday,
    dinnerWeekday: item.okDinnerWeekday,
    lunchWeekend: item.okLunchWeekend,
    dinnerWeekend: item.okDinnerWeekend,
  };
}
function combineMoments(items: { moments: AllowedMoments }[]): AllowedMoments {
  return {
    lunchWeekday: items.every((item) => item.moments.lunchWeekday),
    dinnerWeekday: items.every((item) => item.moments.dinnerWeekday),
    lunchWeekend: items.every((item) => item.moments.lunchWeekend),
    dinnerWeekend: items.every((item) => item.moments.dinnerWeekend),
  };
}
function combineSeasons(items: { seasons: Season[] }[]): EngineSeason[] {
  const first = items[0];
  return first ? first.seasons.filter((season) => items.every((item) => item.seasons.includes(season))).map((season) => seasonMap[season]) : [];
}
function sideFromIngredient(item: Ingredient): Side {
  return { id: item.id, kind: 'ingredient', name: item.name, rating: item.rating,
    seasons: item.seasons, moments: moments(item), ingredientIds: [item.id], family: item.subFamily ?? undefined };
}
function sideFromRecipe(item: RecipeWithIngredients): Side {
  return { id: item.id, kind: 'recipe', name: item.name, rating: item.rating,
    seasons: item.seasons, moments: moments(item), ingredientIds: item.ingredients.map((entry) => entry.ingredientId) };
}
function sideToken(side?: Side) { return side ? `${side.kind === 'recipe' ? 'r' : 'i'}${side.id}` : '-'; }
function sideOptions(ingredients: Ingredient[], recipes: RecipeWithIngredients[], category: 'STARCH' | 'VEGETABLE', automatic: boolean): Side[] {
  const role = category === 'STARCH' ? 'SIDE_STARCH' : 'SIDE_VEGETABLE';
  return [
    ...ingredients.filter((item) => item.category === category && (!automatic || item.useInComposedMeals)).map(sideFromIngredient),
    ...recipes.filter((item) => item.role === role).map(sideFromRecipe),
  ];
}
function makeCandidate(main: Ingredient | RecipeWithIngredients, starch?: Side, vegetable?: Side): MealCandidate | null {
  const isRecipe = 'ingredients' in main;
  const items = [{ seasons: main.seasons, moments: moments(main) },
    ...[starch, vegetable].filter((item): item is Side => Boolean(item))];
  const seasons = combineSeasons(items);
  if (!seasons.length) return null;
  const allIds = isRecipe ? main.ingredients.map((entry) => entry.ingredientId) : [main.id];
  const sides = [starch, vegetable].filter((item): item is Side => Boolean(item));
  const root = isRecipe ? (main.variantOfId ?? main.id) : null;
  const signature = isRecipe
    ? `recipe:${main.id}${sides.length ? `:${sideToken(starch)}:${sideToken(vegetable)}` : ''}`
    : `composed:${main.id}:${sideToken(starch)}:${sideToken(vegetable)}`;
  return {
    kind: isRecipe ? 'recipe' : 'composed',
    signature,
    repeatKey: root ? `recipe:${root}` : undefined,
    name: [main.name, ...sides.map((side) => side.name.toLocaleLowerCase('fr-FR'))].join(' · '),
    description: sides.length ? 'Plat et accompagnements choisis pour ce repas.' : 'Recette familiale.',
    totalMinutes: isRecipe ? (main.prepTimeMinutes ?? 0) + (main.cookTimeMinutes ?? 0) : undefined,
    recipeId: isRecipe ? main.id : undefined,
    proteinId: isRecipe ? undefined : main.id,
    starchId: starch?.kind === 'ingredient' ? starch.id : undefined,
    vegetableId: vegetable?.kind === 'ingredient' ? vegetable.id : undefined,
    starchRecipeId: starch?.kind === 'recipe' ? starch.id : undefined,
    vegetableRecipeId: vegetable?.kind === 'recipe' ? vegetable.id : undefined,
    compositionType: sides.length === 2 ? 'complete' : starch ? 'starch' : vegetable ? 'vegetable' : undefined,
    ingredientIds: [...allIds, ...sides.flatMap((side) => side.ingredientIds)],
    proteinFamily: isRecipe ? main.ingredients.find((entry) => entry.ingredient.category === 'PROTEIN')?.ingredient.subFamily ?? undefined : main.subFamily ?? undefined,
    starchFamily: starch?.family,
    style: isRecipe ? main.style ?? undefined : 'composed',
    rating: Math.pow([main.rating, ...sides.map((side) => side.rating)].reduce((product, rating) => product * rating, 1), 1 / (sides.length + 1)),
    seasons,
    allowedMoments: combineMoments(items),
    isActive: true,
  };
}
async function allCandidates(automatic: boolean): Promise<MealCandidate[]> {
  const prisma = getPrisma();
  const [ingredients, recipes] = await Promise.all([
    prisma.ingredient.findMany({ where: { isActive: true } }),
    prisma.recipe.findMany({ where: { isActive: true }, include: { ingredients: { include: { ingredient: true } } } }),
  ]);
  const starches = sideOptions(ingredients, recipes, 'STARCH', automatic);
  const vegetables = sideOptions(ingredients, recipes, 'VEGETABLE', automatic);
  const result: MealCandidate[] = [];
  function add(main: Ingredient | RecipeWithIngredients, allowStarch: boolean, allowVegetable: boolean) {
    const starchChoices = allowStarch ? [undefined, ...starches] : [undefined];
    const vegetableChoices = allowVegetable ? [undefined, ...vegetables] : [undefined];
    for (const starch of starchChoices) for (const vegetable of vegetableChoices) {
      if ((allowStarch || allowVegetable) && !starch && !vegetable) continue;
      const candidate = makeCandidate(main, starch, vegetable);
      if (candidate) result.push(candidate);
    }
  }
  for (const protein of ingredients.filter((item) => item.category === 'PROTEIN' && (!automatic || item.useInComposedMeals))) {
    add(protein, true, true);
  }
  for (const recipe of recipes.filter((item) => item.role === 'MAIN')) {
    add(recipe, recipe.allowStarchSide, recipe.allowVegetableSide);
  }
  return result;
}
export function loadCandidates() { return allCandidates(true); }

export async function loadManualCandidate(input: {
  recipeId?: string; proteinId?: string; starchId?: string; vegetableId?: string;
  starchRecipeId?: string; vegetableRecipeId?: string;
}): Promise<MealCandidate> {
  if (Boolean(input.recipeId) === Boolean(input.proteinId)) throw new Error('Choisissez un plat ou une protéine.');
  const selected = (await allCandidates(false)).find((item) =>
    item.recipeId === input.recipeId && item.proteinId === input.proteinId &&
    item.starchId === input.starchId && item.vegetableId === input.vegetableId &&
    item.starchRecipeId === input.starchRecipeId && item.vegetableRecipeId === input.vegetableRecipeId,
  );
  if (!selected) throw new Error('Cette composition est indisponible ou incomplète.');
  return selected;
}

export async function loadIncompatibilities(): Promise<Set<string>> {
  const rows = await getPrisma().incompatibility.findMany({ select: { ingredientId1: true, ingredientId2: true } });
  return new Set(rows.map((row) => canonicalPair(row.ingredientId1, row.ingredientId2)));
}
function productItem(ingredient: Ingredient & { aisle?: { name: string } | null }) {
  return { ingredientId: ingredient.id, name: ingredient.name,
    quantityPerPerson: ingredient.portionPerPerson?.toNumber() ?? null,
    unit: ingredient.unit, aisleId: ingredient.aisleId, aisleName: ingredient.aisle?.name ?? null };
}
async function recipeItems(id: string) {
  const recipe = await getPrisma().recipe.findUniqueOrThrow({
    where: { id }, include: { ingredients: { include: { ingredient: { include: { aisle: true } } } } },
  });
  return recipe.ingredients.map((entry) => ({
    ingredientId: entry.ingredient.id, name: entry.ingredient.name,
    quantityPerPerson: entry.quantity.toNumber() / recipe.basePortions, unit: entry.unit,
    aisleId: entry.ingredient.aisleId, aisleName: entry.ingredient.aisle?.name ?? null,
  }));
}
export async function buildSnapshot(candidate: MealCandidate): Promise<MealSnapshot> {
  const productIds = [candidate.proteinId, candidate.starchId, candidate.vegetableId].filter((id): id is string => Boolean(id));
  const recipeIds = [candidate.recipeId, candidate.starchRecipeId, candidate.vegetableRecipeId].filter((id): id is string => Boolean(id));
  const [products, prepared] = await Promise.all([
    getPrisma().ingredient.findMany({ where: { id: { in: productIds } }, include: { aisle: true } }),
    Promise.all(recipeIds.map(recipeItems)),
  ]);
  return {
    version: 1, kind: candidate.kind, signature: candidate.signature, repeatKey: candidate.repeatKey,
    name: candidate.name, description: candidate.description, totalMinutes: candidate.totalMinutes,
    proteinFamily: candidate.proteinFamily, starchFamily: candidate.starchFamily,
    style: candidate.style, compositionType: candidate.compositionType,
    items: [...products.map(productItem), ...prepared.flat()],
  };
}
export async function buildStarterSnapshot(input: { ingredientId?: string; recipeId?: string }): Promise<MealSnapshot> {
  if (Boolean(input.ingredientId) === Boolean(input.recipeId)) throw new Error('Choisissez une entrée.');
  if (input.ingredientId) {
    const item = await getPrisma().ingredient.findUniqueOrThrow({ where: { id: input.ingredientId }, include: { aisle: true } });
    if (!item.isActive) throw new Error('Cette entrée est indisponible.');
    return { version: 1, kind: 'composed', signature: `starter:ingredient:${item.id}`, name: item.name, items: [productItem(item)] };
  }
  const item = await getPrisma().recipe.findUniqueOrThrow({ where: { id: input.recipeId! } });
  if (!item.isActive || item.role !== 'STARTER') throw new Error('Cette entrée est indisponible.');
  return { version: 1, kind: 'recipe', signature: `starter:recipe:${item.id}`, name: item.name, items: await recipeItems(item.id) };
}
