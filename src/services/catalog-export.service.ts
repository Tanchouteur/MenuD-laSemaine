import 'server-only';
import { parseMealSnapshot } from '@/domain/meal-snapshot';
import { getPrisma } from '@/lib/prisma';
import { dateToIso } from '@/services/weekly-plan.service';

export async function buildCatalogExport() {
  const prisma = getPrisma();
  const [settings, aisles, ingredients, recipes, incompatibilities, plans] =
    await Promise.all([
      prisma.appSettings.upsert({
        where: { id: 'default' },
        update: {},
        create: { id: 'default' },
      }),
      prisma.aisle.findMany({ orderBy: [{ sortOrder: 'asc' }, { name: 'asc' }] }),
      prisma.ingredient.findMany({
        include: { aisle: true },
        orderBy: [{ category: 'asc' }, { name: 'asc' }],
      }),
      prisma.recipe.findMany({
        include: {
          ingredients: {
            include: { ingredient: true },
            orderBy: { ingredient: { name: 'asc' } },
          },
        },
        orderBy: { name: 'asc' },
      }),
      prisma.incompatibility.findMany({
        include: { ingredient1: true, ingredient2: true },
        orderBy: { createdAt: 'asc' },
      }),
      prisma.weeklyPlan.findMany({
        include: { slots: { orderBy: [{ mealDate: 'asc' }, { mealTime: 'asc' }] } },
        orderBy: { startDate: 'desc' },
      }),
    ]);

  return {
    format: 'menu-de-la-semaine-catalog',
    version: 2,
    exportedAt: new Date().toISOString(),
    settings: {
      timezone: settings.timezone,
      defaultGuestsLunchWeekday: settings.defaultGuestsLunchWeekday,
      defaultGuestsDinnerWeekday: settings.defaultGuestsDinnerWeekday,
      defaultGuestsLunchWeekend: settings.defaultGuestsLunchWeekend,
      defaultGuestsDinnerWeekend: settings.defaultGuestsDinnerWeekend,
      compositionSetupCompleted: settings.compositionSetupCompleted,
      starterTargetPerWeek: settings.starterTargetPerWeek,
    },
    aisles: aisles.map((aisle) => ({
      id: aisle.id,
      name: aisle.name,
      sortOrder: aisle.sortOrder,
    })),
    ingredients: ingredients.map((ingredient) => ({
      id: ingredient.id,
      name: ingredient.name,
      category: ingredient.category,
      subFamily: ingredient.subFamily,
      rating: ingredient.rating,
      isActive: ingredient.isActive,
      useInComposedMeals: ingredient.useInComposedMeals,
      useAsStarter: ingredient.useAsStarter,
      portionPerPerson: ingredient.portionPerPerson?.toNumber() ?? null,
      unit: ingredient.unit,
      aisle: ingredient.aisle?.name ?? null,
      seasons: ingredient.seasons,
      allowedMoments: {
        lunchWeekday: ingredient.okLunchWeekday,
        dinnerWeekday: ingredient.okDinnerWeekday,
        lunchWeekend: ingredient.okLunchWeekend,
        dinnerWeekend: ingredient.okDinnerWeekend,
      },
    })),
    recipes: recipes.map((recipe) => ({
      id: recipe.id,
      name: recipe.name,
      role: recipe.role,
      allowStarchSide: recipe.allowStarchSide,
      allowVegetableSide: recipe.allowVegetableSide,
      variantOfId: recipe.variantOfId,
      variantOfName: recipes.find((item) => item.id === recipe.variantOfId)?.name ?? null,
      style: recipe.style,
      rating: recipe.rating,
      isActive: recipe.isActive,
      prepTimeMinutes: recipe.prepTimeMinutes,
      cookTimeMinutes: recipe.cookTimeMinutes,
      basePortions: recipe.basePortions,
      seasons: recipe.seasons,
      allowedMoments: {
        lunchWeekday: recipe.okLunchWeekday,
        dinnerWeekday: recipe.okDinnerWeekday,
        lunchWeekend: recipe.okLunchWeekend,
        dinnerWeekend: recipe.okDinnerWeekend,
      },
      ingredients: recipe.ingredients.map((entry) => ({
        ingredientId: entry.ingredientId,
        name: entry.ingredient.name,
        quantity: entry.quantity.toNumber(),
        unit: entry.unit,
      })),
    })),
    incompatibilities: incompatibilities.map((rule) => ({
      firstIngredientId: rule.ingredientId1,
      firstIngredientName: rule.ingredient1.name,
      secondIngredientId: rule.ingredientId2,
      secondIngredientName: rule.ingredient2.name,
    })),
    plans: plans.map((plan) => ({
      startDate: dateToIso(plan.startDate),
      status: plan.status,
      isFavorite: plan.isFavorite,
      confirmedAt: plan.confirmedAt?.toISOString() ?? null,
      slots: plan.slots.map((slot) => {
        const snapshot = parseMealSnapshot(slot.mealSnapshot);
        const starter = parseMealSnapshot(slot.starterSnapshot);
        return {
          date: dateToIso(slot.mealDate),
          mealTime: slot.mealTime,
          guestCount: slot.guestCount,
          slotType: slot.slotType,
          name: snapshot?.name ?? slot.customLabel,
          signature: snapshot?.signature ?? slot.mealSignature,
          repeatKey: snapshot?.repeatKey ?? null,
          starter: starter ? { name: starter.name, signature: starter.signature,
            ingredients: starter.items.map((item) => ({ name: item.name, quantityPerPerson: item.quantityPerPerson, unit: item.unit })) } : null,
          starterIsLocked: slot.starterIsLocked,
          starchRecipeId: slot.starchRecipeId,
          vegetableRecipeId: slot.vegetableRecipeId,
          ingredients: snapshot?.items.map((item) => ({
            ingredientId: item.ingredientId,
            name: item.name,
            quantityPerPerson: item.quantityPerPerson,
            unit: item.unit,
          })) ?? [],
        };
      }),
    })),
  } as const;
}
