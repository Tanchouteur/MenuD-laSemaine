import 'server-only';
import { createHash } from 'node:crypto';
import { getPrisma } from '@/lib/prisma';
import { McpFault } from '@/mcp/errors';
import type { WeekImport } from '@/mcp/contracts';
import { loadManualCandidate, buildSnapshot } from './candidate.service';
import { chooseManualForSlot, updateMealSlot } from './generation.service';
import { confirmPlan, ensureWeeklyPlan, getPlanByStartDate } from './weekly-plan.service';

export function canonical(value: unknown): string {
  if (Array.isArray(value)) return `[${value.map(canonical).join(',')}]`;
  if (value && typeof value === 'object') return `{${Object.entries(value).sort(([a], [b]) => a.localeCompare(b)).map(([key, val]) => `${JSON.stringify(key)}:${canonical(val)}`).join(',')}}`;
  return JSON.stringify(value);
}
export const digest = (value: unknown) => createHash('sha256').update(canonical(value)).digest('hex');

// Pure read: no draft creation, settings upsert, lock modification or reservation.
export async function previewWeekImport(input: WeekImport) {
  const plan = await getPlanByStartDate(input.startDate);
  if ((plan?.version ?? null) !== input.expectedVersion) throw new McpFault('VERSION_CONFLICT', { currentVersion: plan?.version ?? null });
  if (plan && plan.status !== 'draft') throw new McpFault('WEEK_PROTECTED', { status: plan.status });
  if (!plan && input.slots.length !== 14) throw new McpFault('MISSING_INFORMATION', { fields: ['slots: all 14 slots with guestCount for a new week'] });
  if (input.confirmWeek && plan?.slots.some(slot => slot.slotType === 'empty' && !input.slots.some(entry => entry.slotIndex === slot.slotIndex))) {
    throw new McpFault('MISSING_INFORMATION', { fields: ['all empty slots must be filled before confirmation'] });
  }
  const changes = [];
  const missingInformation: Array<{ slotIndex: number; fields: string[] }> = [];
  for (const entry of input.slots) {
    const current = plan?.slots.find(s => s.slotIndex === entry.slotIndex);
    if (current?.isLocked || current?.starterIsLocked) throw new McpFault('SLOT_LOCKED', { slotIndex: entry.slotIndex });
    if (current && (current.slotType !== 'empty' || current.starter !== null) && !entry.replaceExisting) throw new McpFault('REPLACEMENT_REQUIRED', { slotIndex: entry.slotIndex });
    let snapshot = null;
    if (entry.meal.type === 'catalog') {
      try {
        snapshot = await buildSnapshot(await loadManualCandidate(entry.meal.selection));
      } catch { throw new McpFault('INVALID_SELECTION', { slotIndex: entry.slotIndex }); }
      const unknown = snapshot.items.filter(i => i.quantityPerPerson === null || i.unit === null);
      if (unknown.length) missingInformation.push({ slotIndex: entry.slotIndex, fields: unknown.map(i => `ingredient:${i.ingredientId}:portionPerPerson/unit`) });
    } else if (entry.meal.type === 'custom') {
      missingInformation.push({ slotIndex: entry.slotIndex, fields: ['catalog reference', 'ingredients/quantities (shopping unavailable for this meal)'] });
    }
    changes.push({ slotIndex: entry.slotIndex, before: current ?? null, after: entry, snapshot });
  }
  const preview = { startDate: input.startDate, expectedVersion: input.expectedVersion, confirmWeek: input.confirmWeek === true, changes, missingInformation, dietaryInformation: 'Allergies and nutrition are not represented in this catalog; safety is unknown.' };
  return { ...preview, previewDigest: digest({ input, preview }) };
}

/** Called inside the same serializable transaction as the operation receipt. */
export async function applyWeekImport(input: WeekImport, previewDigest: string) {
  const preview = await previewWeekImport(input);
  if (preview.previewDigest !== previewDigest) throw new McpFault('PREVIEW_STALE');
  let plan = await ensureWeeklyPlan(input.startDate);
  const warnings: string[] = [];
  for (const entry of input.slots) {
    const slot = plan.slots.find(s => s.slotIndex === entry.slotIndex)!;
    // Reuse manual selection, snapshots, version claims and shopping rebuilds.
    if (entry.meal.type === 'catalog') {
      plan = await updateMealSlot(slot.id, { guestCount: entry.guestCount }, plan.version);
      plan = await chooseManualForSlot(slot.id, entry.meal.selection, plan.version);
      warnings.push(...(plan.generationWarnings ?? []));
    } else {
      plan = await updateMealSlot(slot.id, {
        guestCount: entry.guestCount,
        slotType: entry.meal.type === 'custom' ? 'custom' : 'eating_out',
        customLabel: entry.meal.type === 'custom' ? entry.meal.label : null,
      }, plan.version);
    }
  }
  if (input.confirmWeek) plan = await confirmPlan(plan.id, plan.version);
  return { plan, missingInformation: preview.missingInformation, warnings };
}

export async function listWeeks(from: string, to: string, skip: number, take: number) {
  if (from > to) throw new McpFault('VALIDATION_ERROR', { fields: ['to must be >= from'] });
  return getPrisma().weeklyPlan.findMany({
    where: { startDate: { gte: new Date(from), lte: new Date(to) } },
    orderBy: { startDate: 'desc' }, skip, take,
    select: { id: true, startDate: true, status: true, version: true },
  });
}
