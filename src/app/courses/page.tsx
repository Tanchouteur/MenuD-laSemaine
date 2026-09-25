import type { Metadata } from 'next';
import Link from 'next/link';
import { connection } from 'next/server';
import { ShoppingList } from '@/components/shopping/shopping-list';
import { mondayOfCurrentWeek } from '@/lib/week';
import { getShoppingList, rebuildShoppingList } from '@/services/shopping-list.service';
import { getPlanByStartDate } from '@/services/weekly-plan.service';

export const metadata: Metadata = { title: 'Courses' };

export default async function ShoppingListPage() {
  await connection();
  const plan = await getPlanByStartDate(mondayOfCurrentWeek());
  if (!plan || plan.status !== 'confirmed') return <main className="pageShell catalogPage">
    <header className="sectionHeader"><div><p className="eyebrow">Cette semaine</p><h1>Courses</h1></div><span className="familyAvatar" aria-hidden="true">✓</span></header>
    <div className="emptyCard"><h2>La liste sera prête après confirmation</h2><p>Terminez votre menu, puis confirmez la semaine pour voir les articles à acheter.</p><Link className="primaryButton" href="/">Voir la semaine</Link></div>
  </main>;
  await rebuildShoppingList(plan.id);
  const entries = await getShoppingList(plan.id);
  return <ShoppingList planId={plan.id} initialEntries={entries} />;
}
