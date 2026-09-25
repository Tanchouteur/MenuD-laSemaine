import 'server-only';
import { PlanStatus, type Prisma } from '../../generated/prisma/client';
import { parseMealSnapshot } from '@/domain/meal-snapshot';
import { getPrisma } from '@/lib/prisma';
import type { ShoppingEntryDto } from '@/types/api';

type Aggregate = {
  stableKey: string;
  label: string;
  quantity: number | null;
  unit: string | null;
  aisleId: string | null;
  sourceSlotIds: string[];
};

export async function requireConfirmedShoppingPlan(planId: string): Promise<void> {
  const plan = await getPrisma().weeklyPlan.findUniqueOrThrow({ where: { id: planId }, select: { status: true } });
  if (plan.status !== PlanStatus.CONFIRMED) throw new Error('Confirmez la semaine pour accéder à sa liste de courses.');
}

export async function requireConfirmedShoppingEntry(entryId: string): Promise<void> {
  const entry = await getPrisma().shoppingListEntry.findUniqueOrThrow({
    where: { id: entryId }, select: { weeklyPlanId: true },
  });
  await requireConfirmedShoppingPlan(entry.weeklyPlanId);
}

export async function rebuildShoppingList(
  planId: string,
  transaction?: Prisma.TransactionClient,
): Promise<void> {
  const prisma = transaction ?? getPrisma();
  const plan = await prisma.weeklyPlan.findUniqueOrThrow({
    where: { id: planId },
    include: { slots: true },
  });
  const aggregates = new Map<string, Aggregate>();

  for (const slot of plan.slots) {
    if (slot.slotType === 'LEFTOVERS' || slot.slotType === 'EATING_OUT') continue;
    const snapshots = [parseMealSnapshot(slot.mealSnapshot), parseMealSnapshot(slot.starterSnapshot)].filter(
      (item): item is NonNullable<typeof item> => item !== null,
    );
    for (const item of snapshots.flatMap((snapshot) => snapshot.items)) {
      const stableKey = `ingredient:${item.ingredientId}:${item.unit ?? 'unknown'}`;
      const current = aggregates.get(stableKey);
      const amount =
        item.quantityPerPerson === null
          ? null
          : item.quantityPerPerson * slot.guestCount;
      aggregates.set(stableKey, {
        stableKey,
        label: item.name,
        quantity:
          amount === null || current?.quantity === null
            ? null
            : (current?.quantity ?? 0) + amount,
        unit: item.unit,
        aisleId: item.aisleId,
        sourceSlotIds: [...new Set([...(current?.sourceSlotIds ?? []), slot.id])],
      });
    }
  }

  const keys = [...aggregates.keys()];
  const persist = async (database: Prisma.TransactionClient) => {
    await database.shoppingListEntry.deleteMany({
      where: {
        weeklyPlanId: planId,
        isManual: false,
        ...(keys.length > 0 ? { stableKey: { notIn: keys } } : {}),
      },
    });
    for (const item of aggregates.values()) {
      await database.shoppingListEntry.upsert({
        where: {
          weeklyPlanId_stableKey: { weeklyPlanId: planId, stableKey: item.stableKey },
        },
        update: {
          label: item.label,
          quantity: item.quantity,
          unit: item.unit as never,
          aisleId: item.aisleId,
          sourceSlotIds: item.sourceSlotIds as Prisma.InputJsonValue,
        },
        create: {
          weeklyPlanId: planId,
          stableKey: item.stableKey,
          label: item.label,
          quantity: item.quantity,
          unit: item.unit as never,
          aisleId: item.aisleId,
          sourceSlotIds: item.sourceSlotIds as Prisma.InputJsonValue,
        },
      });
    }
  };
  if (transaction) await persist(transaction);
  else await getPrisma().$transaction(persist);
}

export async function getShoppingList(planId: string): Promise<ShoppingEntryDto[]> {
  const entries = await getPrisma().shoppingListEntry.findMany({
    where: { weeklyPlanId: planId },
    include: { aisle: true },
    orderBy: [{ aisle: { sortOrder: 'asc' } }, { label: 'asc' }],
  });
  return entries.map((entry) => ({
    id: entry.id,
    stableKey: entry.stableKey,
    label: entry.label,
    quantity: entry.quantity?.toNumber() ?? null,
    unit: entry.unit,
    aisleName: entry.aisle?.name ?? 'Autres',
    isChecked: entry.isChecked,
    isManual: entry.isManual,
    sourceCount: Array.isArray(entry.sourceSlotIds) ? entry.sourceSlotIds.length : 0,
  }));
}

export async function toggleShoppingEntry(entryId: string): Promise<void> {
  const prisma = getPrisma();
  const entry = await prisma.shoppingListEntry.findUniqueOrThrow({
    where: { id: entryId },
  });
  await prisma.shoppingListEntry.update({
    where: { id: entryId },
    data: { isChecked: !entry.isChecked },
  });
}

export async function addManualShoppingEntry(
  planId: string,
  label: string,
): Promise<void> {
  const trimmed = label.trim();
  if (!trimmed) throw new Error('Le libellé est obligatoire.');
  await getPrisma().shoppingListEntry.create({
    data: {
      weeklyPlanId: planId,
      stableKey: `manual:${crypto.randomUUID()}`,
      label: trimmed,
      isManual: true,
    },
  });
}

export async function deleteManualShoppingEntry(entryId: string): Promise<void> {
  const entry = await getPrisma().shoppingListEntry.findUniqueOrThrow({ where: { id: entryId } });
  if (!entry.isManual) throw new Error('Seuls les ajouts manuels peuvent être retirés.');
  await getPrisma().shoppingListEntry.delete({ where: { id: entryId } });
}

export async function uncheckAllShoppingEntries(planId: string): Promise<void> {
  await getPrisma().shoppingListEntry.updateMany({ where: { weeklyPlanId: planId }, data: { isChecked: false } });
}
