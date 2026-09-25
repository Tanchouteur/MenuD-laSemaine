import type { Metadata } from 'next';
import Link from 'next/link';
import { connection } from 'next/server';
import { PrintButton } from '@/components/print/print-button';
import { displayMeal } from '@/lib/meal-display';
import { addDays, formatDay, formatWeekRange, mondayOfCurrentWeek, mondayOfIsoDate } from '@/lib/week';
import { getPlanByStartDate } from '@/services/weekly-plan.service';

export const metadata: Metadata = { title: 'Imprimer les menus' };

export default async function PrintWeekPage({ searchParams }: { searchParams: Promise<{ week?: string | string[] }> }) {
  await connection();
  const requested = (await searchParams).week;
  const value = Array.isArray(requested) ? requested[0] : requested;
  const selectedWeek = value ? mondayOfIsoDate(value) ?? mondayOfCurrentWeek() : mondayOfCurrentWeek();
  const plan = await getPlanByStartDate(selectedWeek);
  const days = Array.from({ length: 7 }, (_, dayIndex) => ({
    date: addDays(selectedWeek, dayIndex),
    slots: plan?.slots.filter((slot) => Math.floor(slot.slotIndex / 2) === dayIndex) ?? [],
  }));

  return <main className="printPage">
    <header className="printControls">
      <Link className="printBack" href={`/?week=${selectedWeek}`}>← Retour aux menus</Link>
      <h1>Menus à imprimer</h1>
      <p>Choisissez une date de la semaine. L’impression peut aussi être enregistrée en PDF depuis votre téléphone.</p>
      <form action="/imprimer" method="get" className="printDateForm">
        <label htmlFor="print-week-date">Choisir une date</label>
        <div><input id="print-week-date" name="week" type="date" defaultValue={selectedWeek} required /><button className="secondaryButton" type="submit">Afficher</button></div>
      </form>
      <nav className="printWeekNav" aria-label="Choisir une semaine à imprimer">
        <Link href={`/imprimer?week=${addDays(selectedWeek, -7)}`}>← Précédente</Link>
        <Link href={`/imprimer?week=${mondayOfCurrentWeek()}`}>Cette semaine</Link>
        <Link href={`/imprimer?week=${addDays(selectedWeek, 7)}`}>Suivante →</Link>
      </nav>
      {plan && <PrintButton />}
    </header>

    {!plan ? <div className="printEmpty"><h2>Aucun menu pour cette semaine</h2><p>Préparez-la d’abord, puis revenez ici pour l’imprimer.</p><Link href={`/?week=${selectedWeek}`}>Ouvrir cette semaine</Link></div> : <section className="printSheet" aria-label={`Menus ${formatWeekRange(selectedWeek)}`}>
      <header className="printSheetHeader"><div><p>Menus de la semaine</p><h2>{formatWeekRange(selectedWeek)}</h2></div>{plan.status !== 'confirmed' && <span>Brouillon</span>}</header>
      <div className="printDays">{days.map(({ date, slots }) => <article className="printDay" key={date}>
        <h3><span>{formatDay(date).weekday}</span><small>{formatDay(date).date}</small></h3>
        {slots.map((slot) => <div className="printMeal" key={slot.id}>
          <strong>{slot.mealTime === 'lunch' ? 'Midi' : 'Soir'} <small>· {slot.guestCount} pers.</small></strong>
          {slot.starter && <p><span>Entrée</span> {slot.starter.name}</p>}
          <p className="printMainMeal">{displayMeal(slot, plan.slots).name}</p>
        </div>)}
      </article>)}</div>
    </section>}
  </main>;
}
