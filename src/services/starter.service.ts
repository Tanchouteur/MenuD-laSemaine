import 'server-only';
import { PlanStatus, Prisma, SlotType } from '../../generated/prisma/client';
import { createSeededRandom, momentForSlot, seasonForDate } from '@/engine';
import { buildStarterSnapshot } from '@/services/candidate.service';
import { getPrisma } from '@/lib/prisma';
import { rebuildShoppingList } from '@/services/shopping-list.service';
import { dateToIso, getPlanByStartDate } from '@/services/weekly-plan.service';
import type { WeeklyPlanDto } from '@/types/api';

const foodTypes: SlotType[] = [SlotType.RECIPE, SlotType.COMPOSED, SlotType.CUSTOM];

export async function generateStarters(planId: string, seed: string, transaction: Prisma.TransactionClient): Promise<string[]> {
  const [settings, slots, ingredients, recipes] = await Promise.all([
    transaction.appSettings.findUniqueOrThrow({ where: { id: 'default' } }),
    transaction.mealSlot.findMany({ where: { weeklyPlanId: planId }, orderBy: [{ mealDate: 'asc' }, { mealTime: 'asc' }] }),
    transaction.ingredient.findMany({ where: { isActive: true, useAsStarter: true } }),
    transaction.recipe.findMany({ where: { isActive: true, role: 'STARTER' } }),
  ]);
  const random = createSeededRandom(`${seed}:starters`);
  const manualCount = slots.filter((slot) => slot.starterIsLocked && slot.starterSnapshot && foodTypes.includes(slot.slotType)).length;
  const available = slots.filter((slot) => !slot.starterIsLocked && foodTypes.includes(slot.slotType) &&
    !(slot.mealTime === 'LUNCH' && ![0, 6].includes(slot.mealDate.getUTCDay())));
  function choicesFor(slot: (typeof slots)[number]) {
    const date = dateToIso(slot.mealDate);
    const weekend = [0, 6].includes(slot.mealDate.getUTCDay());
    const moment = momentForSlot({ slotIndex: 0, date,
      mealTime: slot.mealTime === 'LUNCH' ? 'lunch' : 'dinner', isWeekend: weekend });
    const season = seasonForDate(date);
    return [
      ...ingredients.filter((item) => item.seasons.some((value) => value.toLowerCase() === season) &&
        item[`ok${moment[0].toUpperCase()}${moment.slice(1)}` as keyof typeof item] === true)
        .map((item) => ({ key: `i:${item.id}`, ingredientId: item.id, recipeId: undefined as string | undefined })),
      ...recipes.filter((item) => item.seasons.some((value) => value.toLowerCase() === season) &&
        item[`ok${moment[0].toUpperCase()}${moment.slice(1)}` as keyof typeof item] === true)
        .map((item) => ({ key: `r:${item.id}`, ingredientId: undefined as string | undefined, recipeId: item.id })),
    ];
  }
  const shuffled = available.filter((slot) => choicesFor(slot).length > 0);
  for (let index = shuffled.length - 1; index > 0; index -= 1) {
    const other = Math.floor(random.next() * (index + 1));
    [shuffled[index], shuffled[other]] = [shuffled[other], shuffled[index]];
  }
  const selected = new Set(shuffled.slice(0, Math.max(0, settings.starterTargetPerWeek - manualCount)).map((slot) => slot.id));
  const used = new Set(slots.filter((slot) => slot.starterIsLocked).map((slot) => {
    if (slot.starterIngredientId) return `i:${slot.starterIngredientId}`;
    if (slot.starterRecipeId) return `r:${slot.starterRecipeId}`;
    return '';
  }));
  const warnings: string[] = [];
  if (selected.size < Math.max(0, settings.starterTargetPerWeek - manualCount)) {
    warnings.push('Le catalogue ne contient pas assez d’entrées compatibles pour atteindre la fréquence choisie.');
  }
  for (const slot of slots) {
    if (slot.starterIsLocked) continue;
    if (!selected.has(slot.id)) {
      await transaction.mealSlot.update({ where: { id: slot.id }, data: {
        starterIngredientId: null, starterRecipeId: null, starterSnapshot: Prisma.JsonNull,
      } });
      continue;
    }
    const choices = choicesFor(slot);
    const fresh = choices.filter((choice) => !used.has(choice.key));
    const pool = fresh.length ? fresh : choices;
    if (!pool.length) {
      warnings.push(`Aucune entrée compatible pour le ${dateToIso(slot.mealDate)}.`);
      await transaction.mealSlot.update({ where: { id: slot.id }, data: {
        starterIngredientId: null, starterRecipeId: null, starterSnapshot: Prisma.JsonNull,
      } });
      continue;
    }
    const chosen = pool[Math.floor(random.next() * pool.length)];
    used.add(chosen.key);
    const snapshot = await buildStarterSnapshot(chosen);
    await transaction.mealSlot.update({ where: { id: slot.id }, data: {
      starterIngredientId: chosen.ingredientId ?? null, starterRecipeId: chosen.recipeId ?? null,
      starterSnapshot: snapshot as unknown as Prisma.InputJsonValue,
    } });
  }
  return warnings;
}

export async function setStarterForSlot(slotId: string, input: {
  ingredientId?: string | null; recipeId?: string | null; automatic?: boolean;
}, expectedVersion?: number): Promise<WeeklyPlanDto> {
  const prisma = getPrisma();
  const slot = await prisma.mealSlot.findUniqueOrThrow({ where: { id: slotId }, include: { weeklyPlan: true } });
  if (slot.weeklyPlan.status === PlanStatus.ARCHIVED) throw new Error('Cette semaine est archivée.');
  if (!foodTypes.includes(slot.slotType)) throw new Error('Choisissez d’abord un plat.');
  if (input.ingredientId && input.recipeId) throw new Error('Choisissez une seule entrée.');
  const snapshot = input.ingredientId || input.recipeId ? await buildStarterSnapshot({
    ingredientId: input.ingredientId ?? undefined, recipeId: input.recipeId ?? undefined,
  }) : null;
  await prisma.$transaction(async (transaction) => {
    const claimed = await transaction.weeklyPlan.updateMany({ where: {
      id: slot.weeklyPlanId, status: { in: [PlanStatus.DRAFT, PlanStatus.CONFIRMED] },
      ...(expectedVersion === undefined ? {} : { version: expectedVersion }),
    }, data: { version: { increment: 1 } } });
    if (claimed.count !== 1) throw new Error('Cette semaine a été modifiée sur un autre appareil. Rechargez la page.');
    await transaction.mealSlot.update({ where: { id: slotId }, data: {
      starterIngredientId: input.ingredientId ?? null, starterRecipeId: input.recipeId ?? null,
      starterSnapshot: snapshot ? snapshot as unknown as Prisma.InputJsonValue : Prisma.JsonNull,
      starterIsLocked: !input.automatic,
    } });
    await rebuildShoppingList(slot.weeklyPlanId, transaction);
  });
  return (await getPlanByStartDate(dateToIso(slot.weeklyPlan.startDate)))!;
}
