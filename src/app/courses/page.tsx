import type { Metadata } from 'next';
import Link from 'next/link';
import { connection } from 'next/server';
import { ShoppingList } from '@/components/shopping/shopping-list';
import { formatWeekRange, mondayOfCurrentWeek } from '@/lib/week';
import { getShoppingList, rebuildShoppingList } from '@/services/shopping-list.service';
import { getPlanByStartDate } from '@/services/weekly-plan.service';
import { listAisles } from '@/services/catalog.service';

export const metadata: Metadata = { title: 'Courses' };

export default async function ShoppingListPage({ searchParams }: { searchParams: Promise<{ week?: string | string[] }> }) {
  await connection();
  const requestedWeek = (await searchParams).week;
  const week = Array.isArray(requestedWeek) ? requestedWeek[0] : requestedWeek;
  const selectedWeek = week && /^\d{4}-\d{2}-\d{2}$/.test(week) ? week : mondayOfCurrentWeek();
  const plan = await getPlanByStartDate(selectedWeek);
  if (!plan || plan.status !== 'confirmed') return <main className="pageShell catalogPage">
    <header className="sectionHeader"><div><p className="eyebrow">{formatWeekRange(selectedWeek)}</p><h1>Courses</h1></div><span className="familyAvatar" aria-hidden="true">✓</span></header>
    <div className="emptyCard"><h2>La liste sera prête après confirmation</h2><p>Terminez votre menu, puis confirmez la semaine pour voir les articles à acheter.</p><Link className="primaryButton" href={`/?week=${selectedWeek}`}>Voir la semaine</Link></div>
  </main>;
  await rebuildShoppingList(plan.id);
  const [entries, aisles] = await Promise.all([getShoppingList(plan.id), listAisles()]);
  return <ShoppingList planId={plan.id} startDate={plan.startDate} initialEntries={entries} aisles={aisles.map(({ id, name }) => ({ id, name }))} />;
}
