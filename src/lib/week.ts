import type { GenerationSlot } from '@/engine/types';

const DAY_IN_MS = 86_400_000;

function toIsoDate(date: Date): string {
  return date.toISOString().slice(0, 10);
}

export function mondayOfCurrentWeek(
  reference = new Date(),
  timeZone = 'Europe/Paris',
): string {
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).formatToParts(reference);
  const year = Number(parts.find((part) => part.type === 'year')?.value);
  const month = Number(parts.find((part) => part.type === 'month')?.value);
  const date = Number(parts.find((part) => part.type === 'day')?.value);
  const utcDate = new Date(
    Date.UTC(year, month - 1, date),
  );
  const day = utcDate.getUTCDay();
  const offset = day === 0 ? -6 : 1 - day;
  utcDate.setUTCDate(utcDate.getUTCDate() + offset);
  return toIsoDate(utcDate);
}

export function addDays(isoDate: string, numberOfDays: number): string {
  const date = new Date(`${isoDate}T12:00:00Z`);
  return toIsoDate(new Date(date.getTime() + numberOfDays * DAY_IN_MS));
}

export function mondayOfIsoDate(value: string): string | null {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return null;
  const date = new Date(`${value}T12:00:00Z`);
  if (Number.isNaN(date.getTime()) || toIsoDate(date) !== value) return null;
  const weekday = date.getUTCDay();
  return addDays(value, weekday === 0 ? -6 : 1 - weekday);
}

export function buildWeekSlots(monday: string): GenerationSlot[] {
  return Array.from({ length: 14 }, (_, slotIndex) => {
    const dayIndex = Math.floor(slotIndex / 2);
    return {
      slotIndex,
      date: addDays(monday, dayIndex),
      mealTime: slotIndex % 2 === 0 ? 'lunch' : 'dinner',
      isWeekend: dayIndex >= 5,
    };
  });
}

export function formatWeekRange(monday: string): string {
  const formatter = new Intl.DateTimeFormat('fr-FR', {
    day: 'numeric',
    month: 'long',
    timeZone: 'UTC',
  });
  const start = formatter.format(new Date(`${monday}T12:00:00Z`));
  const end = formatter.format(new Date(`${addDays(monday, 6)}T12:00:00Z`));
  return `Du ${start} au ${end}`;
}

export function formatDay(isoDate: string): { weekday: string; date: string } {
  const date = new Date(`${isoDate}T12:00:00Z`);
  const weekday = new Intl.DateTimeFormat('fr-FR', {
    weekday: 'long',
    timeZone: 'UTC',
  }).format(date);
  const formattedDate = new Intl.DateTimeFormat('fr-FR', {
    day: 'numeric',
    month: 'short',
    timeZone: 'UTC',
  }).format(date);

  return {
    weekday: weekday.charAt(0).toUpperCase() + weekday.slice(1),
    date: formattedDate,
  };
}
