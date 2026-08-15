import 'server-only';
import type { Ingredient, Season } from '../../generated/prisma/client';
import type {
  AllowedMoments,
  MealCandidate,
  Season as EngineSeason,
} from '@/engine';
import { canonicalPair } from '@/engine';
import type { MealSnapshot } from '@/domain/meal-snapshot';
import { getPrisma } from '@/lib/prisma';

const seasonMap: Record<Season, EngineSeason> = {
  WINTER: 'winter',
  SPRING: 'spring',
  SUMMER: 'summer',
  AUTUMN: 'autumn',
};

function allowedMoments(item: {
  okLunchWeekday: boolean;
  okDinnerWeekday: boolean;
  okLunchWeekend: boolean;
  okDinnerWeekend: boolean;
}): AllowedMoments {
  return {
    lunchWeekday: item.okLunchWeekday,
    dinnerWeekday: item.okDinnerWeekday,
    lunchWeekend: item.okLunchWeekend,
    dinnerWeekend: item.okDinnerWeekend,
  };
}

function intersectSeasons(items: readonly Ingredient[]): EngineSeason[] {
  const [first, ...rest] = items;
  if (!first) return [];
  return first.seasons
    .filter((season) => rest.every((item) => item.seasons.includes(season)))
    .map((season) => seasonMap[season]);
}

function intersectMoments(items: readonly Ingredient[]): AllowedMoments {
  const keys = [
    'lunchWeekday',
    'dinnerWeekday',
    'lunchWeekend',
    'dinnerWeekend',
  ] as const;
  const mapped = items.map(allowedMoments);
  return Object.fromEntries(
    keys.map((key) => [key, mapped.every((moments) => moments[key])]),
  ) as unknown as AllowedMoments;
}

function geometricRating(items: readonly Ingredient[]): number {
  const product = items.reduce((value, item) => value * item.rating, 1);
  return Math.pow(product, 1 / items.length);
}

export async function loadCandidates(): Promise<MealCandidate[]> {
  const prisma = getPrisma();
  const [ingredients, recipes] = await Promise.all([
    prisma.ingredient.findMany({ where: { isActive: true } }),
    prisma.recipe.findMany({
      where: { isActive: true },
      include: { ingredients: { include: { ingredient: true } } },
    }),
  ]);
  const proteins = ingredients.filter((item) => item.category === 'PROTEIN');
  const starches = ingredients.filter((item) => item.category === 'STARCH');
  const vegetables = ingredients.filter((item) => item.category === 'VEGETABLE');
  const composed: MealCandidate[] = [];

  function addComposed(protein: Ingredient, starch?: Ingredient, vegetable?: Ingredient) {
    const items = [protein, starch, vegetable].filter((item): item is Ingredient => Boolean(item));
    const candidateSeasons = intersectSeasons(items);
    if (candidateSeasons.length === 0) return;
    const parts = [starch, vegetable].filter((item): item is Ingredient => Boolean(item));
    composed.push({
      kind: 'composed',
      signature: `composed:${protein.id}:${starch?.id ?? '-'}:${vegetable?.id ?? '-'}`,
      name: `${protein.name} et ${parts.map((item) => item.name.toLocaleLowerCase('fr-FR')).join(' avec ')}`,
      description: parts.length === 2 ? 'Une assiette complète avec deux accompagnements.' : 'Une assiette simple avec un accompagnement.',
      proteinId: protein.id,
      starchId: starch?.id,
      vegetableId: vegetable?.id,
      ingredientIds: items.map((item) => item.id),
      proteinFamily: protein.subFamily ?? undefined,
      starchFamily: starch?.subFamily ?? undefined,
      style: 'composed',
      rating: geometricRating(items),
      seasons: candidateSeasons,
      allowedMoments: intersectMoments(items),
      isActive: true,
    });
  }

  for (const protein of proteins) {
    for (const starch of starches) {
      addComposed(protein, starch);
      for (const vegetable of vegetables) {
        addComposed(protein, starch, vegetable);
      }
    }
    for (const vegetable of vegetables) addComposed(protein, undefined, vegetable);
  }

  const recipeCandidates: MealCandidate[] = recipes.map((recipe) => {
    const recipeIngredients = recipe.ingredients.map((item) => item.ingredient);
    const protein = recipeIngredients.find((item) => item.category === 'PROTEIN');
    const starch = recipeIngredients.find((item) => item.category === 'STARCH');
    return {
      kind: 'recipe',
      signature: `recipe:${recipe.id}`,
      recipeId: recipe.id,
      name: recipe.name,
      description: recipe.style ? `Une recette de style ${recipe.style}.` : 'Une recette familiale.',
      totalMinutes: (recipe.prepTimeMinutes ?? 0) + (recipe.cookTimeMinutes ?? 0),
      ingredientIds: recipeIngredients.map((item) => item.id),
      proteinFamily: protein?.subFamily ?? undefined,
      starchFamily: starch?.subFamily ?? undefined,
      style: recipe.style ?? undefined,
      rating: recipe.rating,
      seasons: recipe.seasons.map((season) => seasonMap[season]),
      allowedMoments: allowedMoments(recipe),
      isActive: true,
    };
  });

  return [...composed, ...recipeCandidates];
}

export async function loadIncompatibilities(): Promise<Set<string>> {
  const rows = await getPrisma().incompatibility.findMany({
    select: { ingredientId1: true, ingredientId2: true },
  });
  return new Set(
    rows.map((row) => canonicalPair(row.ingredientId1, row.ingredientId2)),
  );
}

function snapshotItem(ingredient: Ingredient & { aisle?: { name: string } | null }) {
  return {
    ingredientId: ingredient.id,
    name: ingredient.name,
    quantityPerPerson: ingredient.portionPerPerson?.toNumber() ?? null,
    unit: ingredient.unit,
    aisleId: ingredient.aisleId,
    aisleName: ingredient.aisle?.name ?? null,
  };
}

export async function buildSnapshot(candidate: MealCandidate): Promise<MealSnapshot> {
  const prisma = getPrisma();

  if (candidate.kind === 'recipe' && candidate.recipeId) {
    const recipe = await prisma.recipe.findUniqueOrThrow({
      where: { id: candidate.recipeId },
      include: {
        ingredients: { include: { ingredient: { include: { aisle: true } } } },
      },
    });
    const items = recipe.ingredients.map((entry) => ({
      ingredientId: entry.ingredient.id,
      name: entry.ingredient.name,
      quantityPerPerson: entry.quantity.toNumber() / recipe.basePortions,
      unit: entry.unit,
      aisleId: entry.ingredient.aisleId,
      aisleName: entry.ingredient.aisle?.name ?? null,
    }));
    return {
      version: 1,
      kind: candidate.kind,
      signature: candidate.signature,
      name: candidate.name,
      description: candidate.description,
      totalMinutes: candidate.totalMinutes,
      proteinFamily: candidate.proteinFamily,
      starchFamily: candidate.starchFamily,
      style: candidate.style,
      items,
    };
  }

  const ingredients = await prisma.ingredient.findMany({
    where: { id: { in: [...candidate.ingredientIds] } },
    include: { aisle: true },
  });
  return {
    version: 1,
    kind: candidate.kind,
    signature: candidate.signature,
    name: candidate.name,
    description: candidate.description,
    totalMinutes: candidate.totalMinutes,
    proteinFamily: candidate.proteinFamily,
    starchFamily: candidate.starchFamily,
    style: candidate.style,
    items: ingredients.map(snapshotItem),
  };
}
