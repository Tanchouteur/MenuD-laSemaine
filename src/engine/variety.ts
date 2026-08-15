import type { GenerationContext, MealCandidate } from './types';

function proteinFactor(distance: number): number {
  if (distance <= 1) return 0.05;
  if (distance <= 3) return 0.3;
  return 0.7;
}

function starchFactor(distance: number): number {
  if (distance <= 1) return 0.1;
  if (distance <= 3) return 0.4;
  return 1;
}

export function varietyFactor(
  candidate: MealCandidate,
  slotIndex: number,
  context: GenerationContext,
): number {
  let nearestProtein: number | null = null;
  let nearestStarch: number | null = null;
  let nearestStyle: number | null = null;

  for (const [assignedIndex, assigned] of context.assignedSlots) {
    const distance = Math.abs(slotIndex - assignedIndex);
    if (distance === 0) continue;

    if (
      candidate.proteinFamily &&
      assigned.proteinFamily === candidate.proteinFamily &&
      (nearestProtein === null || distance < nearestProtein)
    ) {
      nearestProtein = distance;
    }

    if (
      candidate.starchFamily &&
      assigned.starchFamily === candidate.starchFamily &&
      (nearestStarch === null || distance < nearestStarch)
    ) {
      nearestStarch = distance;
    }

    if (
      candidate.style &&
      assigned.style === candidate.style &&
      (nearestStyle === null || distance < nearestStyle)
    ) {
      nearestStyle = distance;
    }
  }

  const protein = nearestProtein === null ? 1 : proteinFactor(nearestProtein);
  const starch = nearestStarch === null ? 1 : starchFactor(nearestStarch);
  const style = nearestStyle !== null && nearestStyle <= 2 ? 0.2 : 1;

  return Math.max(0.05, protein * starch * style);
}
