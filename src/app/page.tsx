import { connection } from 'next/server';
import { WeekPlanner } from '@/components/week/week-planner';
import { addDays, mondayOfCurrentWeek } from '@/lib/week';
import { ensureWeeklyPlan } from '@/services/weekly-plan.service';
import { getSettings, listIngredients, listRecipes } from '@/services/catalog.service';

export default async function HomePage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  await connection();
  const requestedWeek = (await searchParams).week;
  const value = Array.isArray(requestedWeek) ? requestedWeek[0] : requestedWeek;
  const monday = /^\d{4}-\d{2}-\d{2}$/.test(value ?? '')
    ? (value as string)
    : mondayOfCurrentWeek();
  // Settings must exist before a brand-new plan is created. Running both
  // upserts concurrently can race on the singleton `default` row.
  const settings = await getSettings();
  const [plan, ingredients, recipes] = await Promise.all([
    ensureWeeklyPlan(monday), listIngredients(), listRecipes(),
  ]);

  return (
    <WeekPlanner
      key={monday}
      initialPlan={plan}
      previousWeek={addDays(monday, -7)}
      nextWeek={addDays(monday, 7)}
      initialOnboardingCompleted={settings.onboardingCompleted}
      initialCompositionSetupCompleted={settings.compositionSetupCompleted}
      ingredients={ingredients}
      recipes={recipes}
    />
  );
}
