import { momentForSlot, seasonForDate } from './dates';
import type {
  GenerationContext,
  GenerationSlot,
  MealCandidate,
} from './types';

export function canonicalPair(firstId: string, secondId: string): string {
  return firstId < secondId
    ? `${firstId}::${secondId}`
    : `${secondId}::${firstId}`;
}

export function hasIncompatibility(
  ingredientIds: readonly string[],
  incompatibilities: ReadonlySet<string>,
): boolean {
  for (let first = 0; first < ingredientIds.length; first += 1) {
    for (let second = first + 1; second < ingredientIds.length; second += 1) {
      if (
        incompatibilities.has(
          canonicalPair(ingredientIds[first], ingredientIds[second]),
        )
      ) {
        return true;
      }
    }
  }

  return false;
}

export function isHardEligible(
  candidate: MealCandidate,
  slot: GenerationSlot,
  context: GenerationContext,
  incompatibilities: ReadonlySet<string>,
): boolean {
  if (!candidate.isActive || context.rejectedSignatures.has(candidate.signature)) {
    return false;
  }

  if (!candidate.seasons.includes(seasonForDate(slot.date))) {
    return false;
  }

  if (!candidate.allowedMoments[momentForSlot(slot)]) {
    return false;
  }

  for (const assigned of context.assignedSlots.values()) {
    if (assigned.signature === candidate.signature) {
      return false;
    }
  }

  return !hasIncompatibility(candidate.ingredientIds, incompatibilities);
}
