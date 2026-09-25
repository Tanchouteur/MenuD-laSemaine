import type { Metadata } from 'next';
import { connection } from 'next/server';
import { MoreManager } from '@/components/more/more-manager';
import { listIncompatibilities, listIngredients, getSettings } from '@/services/catalog.service';
import { listPlans } from '@/services/weekly-plan.service';

export const metadata: Metadata = { title: 'Plus' };

export default async function MorePage() {
  await connection();
  const [plans, ingredients, rawIncompatibilities, settings] = await Promise.all([
    listPlans(), listIngredients(), listIncompatibilities(), getSettings(),
  ]);
  const incompatibilities = rawIncompatibilities.map((item) => ({ id: item.id, firstId: item.ingredientId1, firstName: item.ingredient1.name, secondId: item.ingredientId2, secondName: item.ingredient2.name }));
  const calendarUrl = process.env.CALENDAR_TOKEN ? `/api/calendar?token=${encodeURIComponent(process.env.CALENDAR_TOKEN)}` : '/api/calendar';
  return <MoreManager calendarUrl={calendarUrl} plans={plans} ingredients={ingredients} incompatibilities={incompatibilities} initialSettings={{ defaultGuestsLunchWeekday: settings.defaultGuestsLunchWeekday, defaultGuestsDinnerWeekday: settings.defaultGuestsDinnerWeekday, defaultGuestsLunchWeekend: settings.defaultGuestsLunchWeekend, defaultGuestsDinnerWeekend: settings.defaultGuestsDinnerWeekend, starterTargetPerWeek: settings.starterTargetPerWeek }} />;
}
