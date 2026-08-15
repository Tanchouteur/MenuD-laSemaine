import type { Metadata } from 'next';
import { connection } from 'next/server';
import { ShoppingList } from '@/components/shopping/shopping-list';
import { mondayOfCurrentWeek } from '@/lib/week';
import { getShoppingList, rebuildShoppingList } from '@/services/shopping-list.service';
import { ensureWeeklyPlan } from '@/services/weekly-plan.service';

export const metadata: Metadata = { title: 'Courses' };

export default async function ShoppingListPage() {
  await connection();
  const plan = await ensureWeeklyPlan(mondayOfCurrentWeek());
  await rebuildShoppingList(plan.id);
  const entries = await getShoppingList(plan.id);
  return <ShoppingList planId={plan.id} initialEntries={entries} />;
}
