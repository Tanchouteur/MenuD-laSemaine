import type { GenerationSlot, MomentContext, Season } from './types';

const DAY_IN_MS = 86_400_000;

function dateAsUtc(isoDate: string): number {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(isoDate);

  if (!match) {
    throw new Error(`Date ISO invalide : ${isoDate}`);
  }

  return Date.UTC(Number(match[1]), Number(match[2]) - 1, Number(match[3]));
}

export function seasonForDate(isoDate: string): Season {
  const month = new Date(dateAsUtc(isoDate)).getUTCMonth() + 1;

  if (month <= 2 || month === 12) return 'winter';
  if (month <= 5) return 'spring';
  if (month <= 8) return 'summer';
  return 'autumn';
}

export function daysBetween(fromDate: string, toDate: string): number {
  return Math.floor((dateAsUtc(toDate) - dateAsUtc(fromDate)) / DAY_IN_MS);
}

export function momentForSlot(slot: GenerationSlot): MomentContext {
  if (slot.mealTime === 'lunch') {
    return slot.isWeekend ? 'lunchWeekend' : 'lunchWeekday';
  }

  return slot.isWeekend ? 'dinnerWeekend' : 'dinnerWeekday';
}
