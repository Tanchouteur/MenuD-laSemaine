import { afterEach, describe, expect, it, vi } from 'vitest';

const mocks = vi.hoisted(() => ({ listPlans: vi.fn() }));
vi.mock('@/services/weekly-plan.service', () => ({ listPlans: mocks.listPlans }));

import { GET } from '@/app/api/calendar/route';

const originalToken = process.env.CALENDAR_TOKEN;

afterEach(() => {
  mocks.listPlans.mockReset();
  if (originalToken === undefined) delete process.env.CALENDAR_TOKEN;
  else process.env.CALENDAR_TOKEN = originalToken;
});

describe('export iCalendar', () => {
  it('protège le calendrier lorsque le jeton est configuré', async () => {
    process.env.CALENDAR_TOKEN = 'secret-calendrier';
    const response = await GET(new Request('http://test.local/api/calendar?token=incorrect'));
    expect(response.status).toBe(401);
    expect(mocks.listPlans).not.toHaveBeenCalled();
  });

  it('n’exporte que les semaines confirmées et échappe les caractères iCalendar', async () => {
    delete process.env.CALENDAR_TOKEN;
    mocks.listPlans.mockResolvedValue([
      {
        status: 'draft',
        slots: [{ id: 'ignore', date: '2026-09-21', mealTime: 'lunch', slotType: 'custom', customLabel: 'Ignoré', guestCount: 2 }],
      },
      {
        status: 'confirmed',
        slots: [{
          id: 'slot-1', date: '2026-09-22', mealTime: 'dinner', slotType: 'custom',
          customLabel: 'Wok, légumes; sauce\nsoja', guestCount: 4,
        }],
      },
    ]);
    const response = await GET(new Request('http://test.local/api/calendar'));
    const body = await response.text();
    expect(response.status).toBe(200);
    expect(response.headers.get('content-type')).toContain('text/calendar');
    expect(body).toContain('DTSTART;TZID=Europe/Paris:20260922T190000');
    expect(body).toContain('SUMMARY:Wok\\, légumes\\; sauce\\nsoja');
    expect(body).not.toContain('Ignoré');
    expect(body).toMatch(/^BEGIN:VCALENDAR/);
    expect(body).toContain('END:VCALENDAR');
  });
});
