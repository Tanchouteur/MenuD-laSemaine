import { daysBetween } from './dates';
import type { ConsumedMeal } from './types';

export function coolingFactor(daysSinceLastUse: number | null): number {
  if (daysSinceLastUse === null || daysSinceLastUse >= 28) return 1;
  if (daysSinceLastUse >= 21) return 0.9;
  if (daysSinceLastUse >= 14) return 0.7;
  if (daysSinceLastUse >= 10) return 0.4;
  if (daysSinceLastUse >= 7) return 0.15;
  return 0;
}

export function daysSinceLastConsumption(
  signature: string,
  targetDate: string,
  history: readonly ConsumedMeal[],
): number | null {
  let nearest: number | null = null;

  for (const consumed of history) {
    if (consumed.signature !== signature) continue;

    const difference = daysBetween(consumed.mealDate, targetDate);
    if (difference < 0) continue;

    if (nearest === null || difference < nearest) {
      nearest = difference;
    }
  }

  return nearest;
}
