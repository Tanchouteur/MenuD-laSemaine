import { connection } from 'next/server';
import { WeekPlanner } from '@/components/week/week-planner';
import { addDays, mondayOfCurrentWeek } from '@/lib/week';
import { ensureWeeklyPlan } from '@/services/weekly-plan.service';
import { getSettings } from '@/services/catalog.service';

export default async function HomePage({
  searchParams,
}: PageProps<'/'>) {
  await connection();
  const requestedWeek = (await searchParams).week;
  const value = Array.isArray(requestedWeek) ? requestedWeek[0] : requestedWeek;
  const monday = /^\d{4}-\d{2}-\d{2}$/.test(value ?? '')
    ? (value as string)
    : mondayOfCurrentWeek();
  const [plan, settings] = await Promise.all([ensureWeeklyPlan(monday), getSettings()]);

  return (
    <WeekPlanner
      initialPlan={plan}
      previousWeek={addDays(monday, -7)}
      nextWeek={addDays(monday, 7)}
      initialOnboardingCompleted={settings.onboardingCompleted}
    />
  );
}
