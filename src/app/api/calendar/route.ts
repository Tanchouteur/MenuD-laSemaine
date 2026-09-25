import { listPlans } from '@/services/weekly-plan.service';

function escapeIcal(value: string) {
  return value.replaceAll('\\', '\\\\').replaceAll('\n', '\\n').replaceAll(',', '\\,').replaceAll(';', '\\;');
}

function stamp() {
  return new Date().toISOString().replaceAll('-', '').replaceAll(':', '').replace(/\.\d{3}Z$/, 'Z');
}

export async function GET(request: Request) {
  const token = process.env.CALENDAR_TOKEN;
  if (token && new URL(request.url).searchParams.get('token') !== token) {
    return Response.json({ error: 'Lien calendrier invalide.' }, { status: 401 });
  }
  const plans = (await listPlans()).filter((plan) => plan.status === 'confirmed');
  const createdAt = stamp();
  const events = plans.flatMap((plan) => plan.slots.map((slot) => {
    const compactDate = slot.date.replaceAll('-', '');
    const start = slot.mealTime === 'lunch' ? 'T120000' : 'T190000';
    const end = slot.mealTime === 'lunch' ? 'T133000' : 'T203000';
    const specialLabels: Partial<Record<typeof slot.slotType, string>> = { leftovers: 'Restes', eating_out: 'Repas à l’extérieur', empty: 'Repas libre' };
    const title = slot.assignment?.name ?? slot.customLabel ?? specialLabels[slot.slotType] ?? 'Repas';
    const description = `${slot.starter ? `Entrée : ${slot.starter.name}\n` : ''}${slot.guestCount} personne(s)`;
    return ['BEGIN:VEVENT', `UID:${slot.id}@menu-de-la-semaine`, `DTSTAMP:${createdAt}`, `DTSTART;TZID=Europe/Paris:${compactDate}${start}`, `DTEND;TZID=Europe/Paris:${compactDate}${end}`, `SUMMARY:${escapeIcal(title)}`, `DESCRIPTION:${escapeIcal(description)}`, 'END:VEVENT'].join('\r\n');
  }));
  const calendar = ['BEGIN:VCALENDAR', 'VERSION:2.0', 'PRODID:-//Menu de la semaine//FR', 'CALSCALE:GREGORIAN', 'METHOD:PUBLISH', 'X-WR-CALNAME:Menus de la famille', 'X-WR-TIMEZONE:Europe/Paris', 'REFRESH-INTERVAL;VALUE=DURATION:PT1H', 'X-PUBLISHED-TTL:PT1H', ...events, 'END:VCALENDAR', ''].join('\r\n');
  return new Response(calendar, { headers: { 'content-type': 'text/calendar; charset=utf-8', 'content-disposition': 'inline; filename="menus-famille.ics"', 'cache-control': 'no-cache, no-store, must-revalidate' } });
}
