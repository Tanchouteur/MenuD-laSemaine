import 'server-only';
import type { MealSlot, WeeklyPlan } from '../../generated/prisma/client';
import {
  MealTime,
  PlanStatus,
  SlotType,
} from '../../generated/prisma/client';
import { parseMealSnapshot, snapshotAsAssignment } from '@/domain/meal-snapshot';
import { daysBetween } from '@/engine';
import { getPrisma } from '@/lib/prisma';
import { addDays } from '@/lib/week';
import type { MealSlotDto, SlotTypeDto, WeeklyPlanDto } from '@/types/api';

type PlanWithSlots = WeeklyPlan & { slots: MealSlot[] };

function asDate(isoDate: string): Date {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(isoDate)) {
    throw new Error('La date de début de semaine est invalide.');
  }
  return new Date(`${isoDate}T00:00:00.000Z`);
}

function dateToIso(date: Date): string {
  return date.toISOString().slice(0, 10);
}

const statusMap: Record<PlanStatus, WeeklyPlanDto['status']> = {
  DRAFT: 'draft',
  CONFIRMED: 'confirmed',
  ARCHIVED: 'archived',
};

const slotTypeMap: Record<SlotType, SlotTypeDto> = {
  EMPTY: 'empty',
  COMPOSED: 'composed',
  RECIPE: 'recipe',
  LEFTOVERS: 'leftovers',
  EATING_OUT: 'eating_out',
  CUSTOM: 'custom',
};

export function planToDto(plan: PlanWithSlots): WeeklyPlanDto {
  const startDate = dateToIso(plan.startDate);
  const slots: MealSlotDto[] = [...plan.slots]
    .sort((first, second) => {
      const dateDifference = first.mealDate.getTime() - second.mealDate.getTime();
      return dateDifference || (first.mealTime === MealTime.LUNCH ? -1 : 1);
    })
    .map((slot) => {
      const snapshot = parseMealSnapshot(slot.mealSnapshot);
      return {
        id: slot.id,
        slotIndex:
          daysBetween(startDate, dateToIso(slot.mealDate)) * 2 +
          (slot.mealTime === MealTime.DINNER ? 1 : 0),
        date: dateToIso(slot.mealDate),
        mealTime: slot.mealTime === MealTime.LUNCH ? 'lunch' : 'dinner',
        guestCount: slot.guestCount,
        isLocked: slot.isLocked,
        slotType: slotTypeMap[slot.slotType],
        customLabel: slot.customLabel,
        leftoversFromSlotId: slot.leftoversFromSlotId,
        assignment: snapshot ? snapshotAsAssignment(snapshot) : null,
        restoreState: {
          slotType: slotTypeMap[slot.slotType],
          customLabel: slot.customLabel,
          leftoversFromSlotId: slot.leftoversFromSlotId,
          mealSignature: slot.mealSignature,
          mealSnapshot: slot.mealSnapshot,
          recipeId: slot.recipeId,
          proteinId: slot.proteinId,
          starchId: slot.starchId,
          vegetableId: slot.vegetableId,
          isLocked: slot.isLocked,
          guestCount: slot.guestCount,
        },
      };
    });

  return {
    id: plan.id,
    startDate,
    status: statusMap[plan.status],
    isFavorite: plan.isFavorite,
    version: plan.version,
    confirmedAt: plan.confirmedAt?.toISOString() ?? null,
    slots,
  };
}

export async function getPlanByStartDate(startDate: string): Promise<WeeklyPlanDto | null> {
  const plan = await getPrisma().weeklyPlan.findUnique({
    where: { startDate: asDate(startDate) },
    include: { slots: true },
  });
  return plan ? planToDto(plan) : null;
}

export async function ensureWeeklyPlan(startDate: string): Promise<WeeklyPlanDto> {
  const prisma = getPrisma();
  const monday = asDate(startDate);
  if (monday.getUTCDay() !== 1) {
    throw new Error('Une semaine doit commencer un lundi.');
  }

  const existing = await getPlanByStartDate(startDate);
  if (existing) return existing;

  await prisma.$transaction(async (transaction) => {
    const settings = await transaction.appSettings.upsert({
      where: { id: 'default' },
      update: {},
      create: { id: 'default' },
    });
    const plan = await transaction.weeklyPlan.create({ data: { startDate: monday } });
    await transaction.mealSlot.createMany({
      data: Array.from({ length: 14 }, (_, slotIndex) => {
        const dayIndex = Math.floor(slotIndex / 2);
        const isWeekend = dayIndex >= 5;
        const isLunch = slotIndex % 2 === 0;
        const guestCount = isWeekend
          ? isLunch
            ? settings.defaultGuestsLunchWeekend
            : settings.defaultGuestsDinnerWeekend
          : isLunch
            ? settings.defaultGuestsLunchWeekday
            : settings.defaultGuestsDinnerWeekday;
        return {
          weeklyPlanId: plan.id,
          mealDate: asDate(addDays(startDate, dayIndex)),
          mealTime: isLunch ? MealTime.LUNCH : MealTime.DINNER,
          guestCount,
        };
      }),
    });
  });

  const created = await getPlanByStartDate(startDate);
  if (!created) throw new Error('La semaine n’a pas pu être créée.');
  return created;
}

export async function listPlans(): Promise<WeeklyPlanDto[]> {
  const plans = await getPrisma().weeklyPlan.findMany({
    include: { slots: true },
    orderBy: { startDate: 'desc' },
  });
  return plans.map(planToDto);
}

export async function confirmPlan(planId: string, expectedVersion?: number): Promise<WeeklyPlanDto> {
  const prisma = getPrisma();
  const plan = await prisma.weeklyPlan.findUniqueOrThrow({
    where: { id: planId },
    include: { slots: true },
  });
  if (plan.slots.length !== 14) {
    throw new Error('La semaine doit contenir quatorze créneaux.');
  }
  if (plan.status !== PlanStatus.DRAFT) throw new Error('Cette semaine est déjà confirmée.');
  if (expectedVersion !== undefined && plan.version !== expectedVersion) {
    throw new Error('Cette semaine a été modifiée sur un autre appareil. Rechargez la page.');
  }
  if (plan.slots.some((slot) => slot.slotType === SlotType.EMPTY)) {
    throw new Error('Chaque créneau doit être choisi avant de confirmer la semaine.');
  }

  const updated = await prisma.weeklyPlan.update({
    where: { id: planId },
    data: {
      status: PlanStatus.CONFIRMED,
      confirmedAt: new Date(),
      version: { increment: 1 },
    },
    include: { slots: true },
  });
  return planToDto(updated);
}

export async function unconfirmPlan(planId: string, expectedVersion?: number): Promise<WeeklyPlanDto> {
  const prisma = getPrisma();
  const plan = await prisma.weeklyPlan.findUniqueOrThrow({ where: { id: planId } });
  if (plan.status !== PlanStatus.CONFIRMED) {
    throw new Error('Seule une semaine confirmée peut être remise en modification.');
  }
  if (expectedVersion !== undefined && plan.version !== expectedVersion) {
    throw new Error('Cette semaine a été modifiée sur un autre appareil. Rechargez la page.');
  }
  const updated = await prisma.weeklyPlan.update({
    where: { id: planId },
    data: {
      status: PlanStatus.DRAFT,
      confirmedAt: null,
      version: { increment: 1 },
    },
    include: { slots: true },
  });
  return planToDto(updated);
}

export async function toggleFavorite(planId: string): Promise<WeeklyPlanDto> {
  const plan = await getPrisma().weeklyPlan.findUniqueOrThrow({
    where: { id: planId },
  });
  const updated = await getPrisma().weeklyPlan.update({
    where: { id: planId },
    data: { isFavorite: !plan.isFavorite },
    include: { slots: true },
  });
  return planToDto(updated);
}

export async function reapplyPlan(
  sourcePlanId: string,
  targetStartDate: string,
): Promise<WeeklyPlanDto> {
  const prisma = getPrisma();
  const source = await prisma.weeklyPlan.findUniqueOrThrow({
    where: { id: sourcePlanId },
    include: { slots: true },
  });
  const target = await ensureWeeklyPlan(targetStartDate);
  if (target.status !== 'draft') throw new Error('La semaine cible doit être un brouillon.');

  await prisma.$transaction([
    ...source.slots.map((sourceSlot) => {
      const sourceDay = daysBetween(dateToIso(source.startDate), dateToIso(sourceSlot.mealDate));
      const targetDate = asDate(addDays(targetStartDate, sourceDay));
      return prisma.mealSlot.update({
        where: {
          weeklyPlanId_mealDate_mealTime: {
            weeklyPlanId: target.id,
            mealDate: targetDate,
            mealTime: sourceSlot.mealTime,
          },
        },
        data: {
          guestCount: sourceSlot.guestCount,
          isLocked: sourceSlot.isLocked,
          slotType: sourceSlot.slotType,
          customLabel: sourceSlot.customLabel,
          mealSignature: sourceSlot.mealSignature,
          mealSnapshot: sourceSlot.mealSnapshot ?? undefined,
          recipeId: sourceSlot.recipeId,
          proteinId: sourceSlot.proteinId,
          starchId: sourceSlot.starchId,
          vegetableId: sourceSlot.vegetableId,
          leftoversFromSlotId: null,
        },
      });
    }),
    prisma.weeklyPlan.update({
      where: { id: target.id },
      data: { duplicatedFromId: source.id, version: { increment: 1 } },
    }),
  ]);

  const copied = await getPlanByStartDate(targetStartDate);
  if (!copied) throw new Error('La semaine favorite n’a pas pu être copiée.');
  return copied;
}

export { asDate, dateToIso };
