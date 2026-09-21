import 'server-only';
import { PlanStatus, Prisma, SlotType } from '../../generated/prisma/client';
import {
  generateAlternatives,
  generateWeek,
  hasIncompatibility,
  seasonForDate,
  type AssignedMeal,
  type ConsumedMeal,
  type GenerationContext,
  type GenerationSlot,
  type ScoredCandidate,
} from '@/engine';
import { getPrisma } from '@/lib/prisma';
import {
  buildSnapshot,
  loadCandidates,
  loadIncompatibilities,
  loadManualCandidate,
} from '@/services/candidate.service';
import { rebuildShoppingList } from '@/services/shopping-list.service';
import {
  dateToIso,
  ensureWeeklyPlan,
  getPlanByStartDate,
} from '@/services/weekly-plan.service';
import type { WeeklyPlanDto } from '@/types/api';

async function loadHistory(): Promise<ConsumedMeal[]> {
  const slots = await getPrisma().mealSlot.findMany({
    where: {
      weeklyPlan: { status: PlanStatus.CONFIRMED },
      mealSignature: { not: null },
    },
    select: { mealSignature: true, mealDate: true },
  });
  return slots.flatMap((slot) =>
    slot.mealSignature
      ? [{ signature: slot.mealSignature, mealDate: dateToIso(slot.mealDate) }]
      : [],
  );
}

function slotAssignment(slot: WeeklyPlanDto['slots'][number]): AssignedMeal | null {
  return slot.assignment;
}

function asGenerationSlot(slot: WeeklyPlanDto['slots'][number]): GenerationSlot {
  const isFood = slot.slotType === 'recipe' || slot.slotType === 'composed';
  const isSpecial =
    slot.slotType === 'leftovers' ||
    slot.slotType === 'eating_out' ||
    slot.slotType === 'custom';
  return {
    slotIndex: slot.slotIndex,
    date: slot.date,
    mealTime: slot.mealTime,
    isWeekend: slot.slotIndex >= 10,
    isLocked: isFood && slot.isLocked,
    skipGeneration: isSpecial,
    current: isFood ? slotAssignment(slot) : null,
  };
}

export async function generatePersistedWeek(
  startDate: string,
  seed: string,
  expectedVersion?: number,
): Promise<WeeklyPlanDto> {
  const prisma = getPrisma();
  const plan = await ensureWeeklyPlan(startDate);
  if (plan.status !== 'draft') {
    throw new Error('Une semaine confirmée ne peut plus être régénérée.');
  }
  if (expectedVersion !== undefined && plan.version !== expectedVersion) {
    throw new Error('Cette semaine a été modifiée sur un autre appareil. Rechargez la page.');
  }
  const [candidates, incompatibilities, consumedHistory] = await Promise.all([
    loadCandidates(),
    loadIncompatibilities(),
    loadHistory(),
  ]);
  const result = generateWeek({
    candidates,
    incompatibilities,
    consumedHistory,
    slots: plan.slots.map(asGenerationSlot),
    seed,
  });
  const candidateBySignature = new Map(
    candidates.map((candidate) => [candidate.signature, candidate]),
  );
  const selectedCandidates = result.slots.flatMap((slot) => {
    if (slot.skipGeneration || !slot.assignment) return [];
    const candidate = candidateBySignature.get(slot.assignment.signature);
    return candidate ? [candidate] : [];
  });
  const uniqueCandidates = [
    ...new Map(selectedCandidates.map((candidate) => [candidate.signature, candidate])).values(),
  ];
  const snapshots = new Map(
    await Promise.all(
      uniqueCandidates.map(async (candidate) => [
        candidate.signature,
        await buildSnapshot(candidate),
      ] as const),
    ),
  );

  await prisma.$transaction(async (transaction) => {
    const claimed = await transaction.weeklyPlan.updateMany({
      where: {
        id: plan.id,
        status: PlanStatus.DRAFT,
        ...(expectedVersion === undefined ? {} : { version: expectedVersion }),
      },
      data: { version: { increment: 1 } },
    });
    if (claimed.count !== 1) {
      throw new Error('Cette semaine a été modifiée sur un autre appareil. Rechargez la page.');
    }
    for (const generated of result.slots) {
      if (generated.skipGeneration || generated.isLocked || !generated.assignment) continue;
      const dtoSlot = plan.slots.find((slot) => slot.slotIndex === generated.slotIndex);
      const candidate = candidateBySignature.get(generated.assignment.signature);
      const snapshot = snapshots.get(generated.assignment.signature);
      if (!dtoSlot || !candidate || !snapshot) continue;
      await transaction.mealSlot.update({
        where: { id: dtoSlot.id },
        data: {
          slotType: candidate.kind === 'recipe' ? SlotType.RECIPE : SlotType.COMPOSED,
          customLabel: null,
          leftoversFromSlotId: null,
          mealSignature: candidate.signature,
          mealSnapshot: snapshot as unknown as Prisma.InputJsonValue,
          recipeId: candidate.recipeId ?? null,
          proteinId: candidate.proteinId ?? null,
          starchId: candidate.starchId ?? null,
          vegetableId: candidate.vegetableId ?? null,
        },
      });
    }
    await rebuildShoppingList(plan.id, transaction);
  });
  const updated = await getPlanByStartDate(startDate);
  if (!updated) throw new Error('La semaine générée est introuvable.');
  return {
    ...updated,
    generationWarnings: result.warnings.map((warning) => warning.message),
  };
}

async function contextForSlot(
  plan: WeeklyPlanDto,
  targetSlotId: string,
  rejectedSignatures: readonly string[],
): Promise<GenerationContext> {
  const assignedSlots = new Map<number, AssignedMeal>();
  for (const slot of plan.slots) {
    if (slot.id !== targetSlotId && slot.assignment) {
      assignedSlots.set(slot.slotIndex, slot.assignment);
    }
  }
  return {
    assignedSlots,
    consumedHistory: await loadHistory(),
    rejectedSignatures: new Set(rejectedSignatures),
  };
}

export async function alternativesForSlot(
  slotId: string,
  rejectedSignatures: readonly string[],
  seed: string,
): Promise<ScoredCandidate[]> {
  const prisma = getPrisma();
  const dbSlot = await prisma.mealSlot.findUniqueOrThrow({
    where: { id: slotId },
    include: { weeklyPlan: { include: { slots: true } } },
  });
  const plan = (await getPlanByStartDate(dateToIso(dbSlot.weeklyPlan.startDate)))!;
  if (plan.status !== 'draft') throw new Error('La semaine est déjà confirmée.');
  const target = plan.slots.find((slot) => slot.id === slotId);
  if (!target) throw new Error('Créneau introuvable.');
  const [candidates, incompatibilities, context] = await Promise.all([
    loadCandidates(),
    loadIncompatibilities(),
    contextForSlot(plan, slotId, rejectedSignatures),
  ]);
  return generateAlternatives({
    candidates,
    incompatibilities,
    context,
    slot: asGenerationSlot({ ...target, isLocked: false }),
    currentSignature: target.assignment?.signature,
    seed,
  });
}

export async function chooseCandidateForSlot(
  slotId: string,
  signature: string,
  expectedVersion?: number,
): Promise<WeeklyPlanDto> {
  const prisma = getPrisma();
  const slot = await prisma.mealSlot.findUniqueOrThrow({
    where: { id: slotId },
    include: { weeklyPlan: true },
  });
  if (slot.weeklyPlan.status !== PlanStatus.DRAFT) {
    throw new Error('La semaine est déjà confirmée.');
  }
  if (expectedVersion !== undefined && slot.weeklyPlan.version !== expectedVersion) {
    throw new Error('Cette semaine a été modifiée sur un autre appareil. Rechargez la page.');
  }
  const candidate = (await loadCandidates()).find((item) => item.signature === signature);
  if (!candidate) throw new Error('Cette proposition n’est plus disponible.');
  const snapshot = await buildSnapshot(candidate);
  await prisma.$transaction(async (transaction) => {
    const claimed = await transaction.weeklyPlan.updateMany({
      where: {
        id: slot.weeklyPlanId,
        status: PlanStatus.DRAFT,
        ...(expectedVersion === undefined ? {} : { version: expectedVersion }),
      },
      data: { version: { increment: 1 } },
    });
    if (claimed.count !== 1) {
      throw new Error('Cette semaine a été modifiée sur un autre appareil. Rechargez la page.');
    }
    await transaction.mealSlot.update({ where: { id: slotId }, data: {
      slotType: candidate.kind === 'recipe' ? SlotType.RECIPE : SlotType.COMPOSED,
      customLabel: null,
      leftoversFromSlotId: null,
      mealSignature: candidate.signature,
      mealSnapshot: snapshot as unknown as Prisma.InputJsonValue,
      recipeId: candidate.recipeId ?? null,
      proteinId: candidate.proteinId ?? null,
      starchId: candidate.starchId ?? null,
      vegetableId: candidate.vegetableId ?? null,
      isLocked: true,
    } });
    await rebuildShoppingList(slot.weeklyPlanId, transaction);
  });
  return (await getPlanByStartDate(dateToIso(slot.weeklyPlan.startDate)))!;
}

export async function chooseManualForSlot(
  slotId: string,
  selection: {
    recipeId?: string;
    proteinId?: string;
    starchId?: string;
    vegetableId?: string;
  },
  expectedVersion?: number,
): Promise<WeeklyPlanDto> {
  const prisma = getPrisma();
  const slot = await prisma.mealSlot.findUniqueOrThrow({
    where: { id: slotId },
    include: { weeklyPlan: true },
  });
  const candidate = await loadManualCandidate(selection);
  const snapshot = await buildSnapshot(candidate);
  await prisma.$transaction(async (transaction) => {
    const updatedPlan = await transaction.weeklyPlan.updateMany({
      where: {
        id: slot.weeklyPlanId,
        status: PlanStatus.DRAFT,
        ...(expectedVersion === undefined ? {} : { version: expectedVersion }),
      },
      data: { version: { increment: 1 } },
    });
    if (updatedPlan.count !== 1) {
      throw new Error('Cette semaine a été modifiée sur un autre appareil. Rechargez la page.');
    }
    await transaction.mealSlot.updateMany({
      where: { id: slotId, weeklyPlanId: slot.weeklyPlanId },
      data: {
        slotType: candidate.kind === 'recipe' ? SlotType.RECIPE : SlotType.COMPOSED,
        customLabel: null,
        leftoversFromSlotId: null,
        mealSignature: candidate.signature,
        mealSnapshot: snapshot as unknown as Prisma.InputJsonValue,
        recipeId: candidate.recipeId ?? null,
        proteinId: candidate.proteinId ?? null,
        starchId: candidate.starchId ?? null,
        vegetableId: candidate.vegetableId ?? null,
        isLocked: true,
      },
    });
    await rebuildShoppingList(slot.weeklyPlanId, transaction);
  });
  const updated = (await getPlanByStartDate(dateToIso(slot.weeklyPlan.startDate)))!;
  const warnings: string[] = [];
  const momentKey = slot.mealTime === 'LUNCH'
    ? slot.mealDate.getUTCDay() >= 6 || slot.mealDate.getUTCDay() === 0
      ? 'lunchWeekend' : 'lunchWeekday'
    : slot.mealDate.getUTCDay() >= 6 || slot.mealDate.getUTCDay() === 0
      ? 'dinnerWeekend' : 'dinnerWeekday';
  if (!candidate.allowedMoments[momentKey]) {
    warnings.push('Ce repas sort de vos moments habituels, mais votre choix est conservé.');
  }
  if (!candidate.seasons.includes(seasonForDate(dateToIso(slot.mealDate)))) {
    warnings.push('Ce repas est hors saison selon votre catalogue, mais votre choix est conservé.');
  }
  const duplicate = await prisma.mealSlot.count({
    where: {
      weeklyPlanId: slot.weeklyPlanId,
      id: { not: slotId },
      mealSignature: candidate.signature,
    },
  });
  if (duplicate > 0) {
    warnings.push('Ce repas apparaît déjà cette semaine, mais votre choix est conservé.');
  }
  if (
    candidate.kind === 'composed' &&
    hasIncompatibility(candidate.ingredientIds, await loadIncompatibilities())
  ) {
    warnings.push('Cette association figure parmi les aliments à éviter, mais votre choix est conservé.');
  }
  return { ...updated, generationWarnings: warnings };
}

type SlotUpdate = {
  isLocked?: boolean;
  guestCount?: number;
  slotType?: 'empty' | 'leftovers' | 'eating_out' | 'custom';
  customLabel?: string | null;
  leftoversFromSlotId?: string | null;
};

export async function restoreMealSlot(
  slotId: string,
  state: WeeklyPlanDto['slots'][number]['restoreState'],
  expectedVersion?: number,
): Promise<WeeklyPlanDto> {
  const prisma = getPrisma();
  const slot = await prisma.mealSlot.findUniqueOrThrow({
    where: { id: slotId },
    include: { weeklyPlan: true },
  });
  const typeMap = {
    empty: SlotType.EMPTY,
    composed: SlotType.COMPOSED,
    recipe: SlotType.RECIPE,
    leftovers: SlotType.LEFTOVERS,
    eating_out: SlotType.EATING_OUT,
    custom: SlotType.CUSTOM,
  } as const;
  if (!typeMap[state.slotType] || state.guestCount < 1 || state.guestCount > 30) {
    throw new Error('L’état précédent du repas est invalide.');
  }
  if ((state.slotType === 'recipe' || state.slotType === 'composed') && !state.mealSnapshot) {
    throw new Error('L’instantané du repas à restaurer est absent.');
  }
  await prisma.$transaction(async (transaction) => {
    const claimed = await transaction.weeklyPlan.updateMany({
      where: {
        id: slot.weeklyPlanId,
        status: PlanStatus.DRAFT,
        ...(expectedVersion === undefined ? {} : { version: expectedVersion }),
      },
      data: { version: { increment: 1 } },
    });
    if (claimed.count !== 1) {
      throw new Error('Cette semaine a été modifiée sur un autre appareil. Rechargez la page.');
    }
    await transaction.mealSlot.updateMany({
      where: { id: slotId, weeklyPlanId: slot.weeklyPlanId },
      data: {
        slotType: typeMap[state.slotType],
        customLabel: state.customLabel,
        leftoversFromSlotId: state.leftoversFromSlotId,
        mealSignature: state.mealSignature,
        mealSnapshot: state.mealSnapshot === null
          ? Prisma.JsonNull
          : state.mealSnapshot as Prisma.InputJsonValue,
        recipeId: state.recipeId,
        proteinId: state.proteinId,
        starchId: state.starchId,
        vegetableId: state.vegetableId,
        isLocked: state.isLocked,
        guestCount: state.guestCount,
      },
    });
    await rebuildShoppingList(slot.weeklyPlanId, transaction);
  });
  return (await getPlanByStartDate(dateToIso(slot.weeklyPlan.startDate)))!;
}

export async function updateMealSlot(
  slotId: string,
  update: SlotUpdate,
  expectedVersion?: number,
): Promise<WeeklyPlanDto> {
  const prisma = getPrisma();
  const slot = await prisma.mealSlot.findUniqueOrThrow({
    where: { id: slotId },
    include: { weeklyPlan: true },
  });
  if (slot.weeklyPlan.status !== PlanStatus.DRAFT) {
    throw new Error('La semaine est déjà confirmée.');
  }
  if (expectedVersion !== undefined && slot.weeklyPlan.version !== expectedVersion) {
    throw new Error('Cette semaine a été modifiée sur un autre appareil. Rechargez la page.');
  }
  if (update.guestCount !== undefined && (update.guestCount < 1 || update.guestCount > 30)) {
    throw new Error('Le nombre de personnes doit être compris entre 1 et 30.');
  }
  const data: Prisma.MealSlotUpdateInput = {};
  if (update.isLocked !== undefined) data.isLocked = update.isLocked;
  if (update.guestCount !== undefined) data.guestCount = update.guestCount;

  if (update.slotType) {
    const typeMap = {
      empty: SlotType.EMPTY,
      leftovers: SlotType.LEFTOVERS,
      eating_out: SlotType.EATING_OUT,
      custom: SlotType.CUSTOM,
    } as const;
    const nextSlotType = typeMap[update.slotType];
    if (update.slotType === 'custom' && !update.customLabel?.trim()) {
      throw new Error('Donnez un nom au repas libre.');
    }
    // updateMany écrit les clés étrangères et le nouveau type en une seule
    // instruction SQL. C’est nécessaire car la contrainte de cohérence du
    // créneau refuse tout état intermédiaire (ancien repas + nouveau type).
    await prisma.$transaction(async (transaction) => {
      const claimed = await transaction.weeklyPlan.updateMany({ where: {
        id: slot.weeklyPlanId,
        status: PlanStatus.DRAFT,
        ...(expectedVersion === undefined ? {} : { version: expectedVersion }),
      }, data: { version: { increment: 1 } } });
      if (claimed.count !== 1) throw new Error('Cette semaine a été modifiée sur un autre appareil. Rechargez la page.');
      await transaction.mealSlot.updateMany({ where: { id: slotId }, data: {
        slotType: nextSlotType,
        customLabel: update.customLabel?.trim() || null,
        leftoversFromSlotId: update.leftoversFromSlotId ?? null,
        mealSignature: null,
        mealSnapshot: Prisma.JsonNull,
        recipeId: null,
        proteinId: null,
        starchId: null,
        vegetableId: null,
        isLocked: update.isLocked,
        guestCount: update.guestCount,
      } });
      await rebuildShoppingList(slot.weeklyPlanId, transaction);
    });
    return (await getPlanByStartDate(dateToIso(slot.weeklyPlan.startDate)))!;
  }

  await prisma.$transaction(async (transaction) => {
    const claimed = await transaction.weeklyPlan.updateMany({ where: {
      id: slot.weeklyPlanId,
      status: PlanStatus.DRAFT,
      ...(expectedVersion === undefined ? {} : { version: expectedVersion }),
    }, data: { version: { increment: 1 } } });
    if (claimed.count !== 1) throw new Error('Cette semaine a été modifiée sur un autre appareil. Rechargez la page.');
    await transaction.mealSlot.update({ where: { id: slotId }, data });
    await rebuildShoppingList(slot.weeklyPlanId, transaction);
  });
  return (await getPlanByStartDate(dateToIso(slot.weeklyPlan.startDate)))!;
}
