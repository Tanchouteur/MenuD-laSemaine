import { afterEach, describe, expect, it } from 'vitest';
import { apiError, readJson } from '@/lib/api';
import { FAMILY_COOKIE, familyAuthConfigured, familyToken } from '@/lib/family-auth';
import {
  compositionSetupSchema,
  ingredientInputSchema,
  recipeInputSchema,
  settingsSchema,
} from '@/lib/schemas';
import { guestCountSchema, isoDateSchema } from '@/lib/validation';
import { addDays, buildWeekSlots, formatWeekRange, mondayOfCurrentWeek } from '@/lib/week';

describe('utilitaires HTTP', () => {
  it('traduit les absences en 404 et les autres erreurs en 400', async () => {
    expect(apiError(new Error('Créneau introuvable.')).status).toBe(404);
    expect(apiError(new Error('Entrée invalide.')).status).toBe(400);
    expect(apiError('erreur opaque').status).toBe(400);
  });

  it('lit le JSON et rejette un corps mal formé avec un message stable', async () => {
    await expect(readJson(new Request('http://test.local', {
      method: 'POST',
      body: JSON.stringify({ ok: true }),
    }))).resolves.toEqual({ ok: true });
    await expect(readJson(new Request('http://test.local', {
      method: 'POST',
      body: '{',
    }))).rejects.toThrow('Le contenu de la requête est invalide.');
  });
});

describe('authentification familiale', () => {
  const originalPassword = process.env.FAMILY_PASSWORD;
  const originalSecret = process.env.AUTH_SECRET;

  afterEach(() => {
    if (originalPassword === undefined) delete process.env.FAMILY_PASSWORD;
    else process.env.FAMILY_PASSWORD = originalPassword;
    if (originalSecret === undefined) delete process.env.AUTH_SECRET;
    else process.env.AUTH_SECRET = originalSecret;
  });

  it('produit un jeton déterministe sans exposer le mot de passe', async () => {
    const first = await familyToken('mot-de-passe', 'secret');
    expect(first).toBe(await familyToken('mot-de-passe', 'secret'));
    expect(first).toMatch(/^[a-f0-9]{64}$/);
    expect(first).not.toContain('mot-de-passe');
    expect(FAMILY_COOKIE).toBe('menu_family_session');
  });

  it('n’active la protection que lorsque les deux secrets sont présents', () => {
    delete process.env.FAMILY_PASSWORD;
    delete process.env.AUTH_SECRET;
    expect(familyAuthConfigured()).toBe(false);
    process.env.FAMILY_PASSWORD = 'famille';
    expect(familyAuthConfigured()).toBe(false);
    process.env.AUTH_SECRET = 'secret';
    expect(familyAuthConfigured()).toBe(true);
  });
});

describe('validation des entrées', () => {
  const moments = {
    okLunchWeekday: true,
    okDinnerWeekday: true,
    okLunchWeekend: true,
    okDinnerWeekend: true,
  };

  it('accepte un ingrédient complet et applique la valeur par défaut de composition', () => {
    const parsed = ingredientInputSchema.parse({
      name: ' Carotte ',
      category: 'VEGETABLE',
      rating: 4,
      seasons: ['SPRING'],
      ...moments,
    });
    expect(parsed.name).toBe('Carotte');
    expect(parsed.useInComposedMeals).toBe(false);
  });

  it.each([
    [{ name: '', category: 'VEGETABLE', rating: 4, seasons: ['SPRING'], ...moments }],
    [{ name: 'Carotte', category: 'VEGETABLE', rating: 0, seasons: ['SPRING'], ...moments }],
    [{ name: 'Carotte', category: 'VEGETABLE', rating: 4, seasons: [], ...moments }],
  ])('rejette un ingrédient invalide', (input) => {
    expect(ingredientInputSchema.safeParse(input).success).toBe(false);
  });

  it('borne recettes, réglages, composition, dates et convives', () => {
    expect(recipeInputSchema.safeParse({
      name: 'Soupe', rating: 3, basePortions: 0, seasons: ['WINTER'],
      ingredients: [], ...moments,
    }).success).toBe(false);
    expect(settingsSchema.safeParse({
      defaultGuestsLunchWeekday: 0,
      defaultGuestsDinnerWeekday: 4,
      defaultGuestsLunchWeekend: 4,
      defaultGuestsDinnerWeekend: 4,
    }).success).toBe(false);
    expect(compositionSetupSchema.safeParse({ ingredientIds: Array(501).fill('x') }).success).toBe(false);
    expect(guestCountSchema.safeParse(31).success).toBe(false);
    expect(isoDateSchema.safeParse('2026-02-30').success).toBe(false);
    expect(isoDateSchema.safeParse('2026-02-28').success).toBe(true);
  });
});

describe('semaines et fuseaux', () => {
  it('construit exactement quatorze créneaux ordonnés', () => {
    const slots = buildWeekSlots('2026-09-21');
    expect(slots).toHaveLength(14);
    expect(slots[0]).toMatchObject({ date: '2026-09-21', mealTime: 'lunch', isWeekend: false });
    expect(slots[13]).toMatchObject({ date: '2026-09-27', mealTime: 'dinner', isWeekend: true });
    expect(new Set(slots.map((slot) => slot.slotIndex)).size).toBe(14);
  });

  it('gère les changements de mois et formate la plage en français', () => {
    expect(addDays('2026-01-31', 1)).toBe('2026-02-01');
    expect(formatWeekRange('2026-09-28')).toBe('Du 28 septembre au 4 octobre');
  });

  it('signale explicitement un fuseau invalide', () => {
    const date = new Date('2026-09-23T12:00:00Z');
    expect(() => mondayOfCurrentWeek(date, 'Fuseau/Invalide')).toThrow(RangeError);
  });
});
