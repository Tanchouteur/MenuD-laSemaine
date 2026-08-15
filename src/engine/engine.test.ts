import { describe, expect, it } from 'vitest';
import {
  canonicalPair,
  coolingFactor,
  createSeededRandom,
  generateAlternatives,
  generateWeek,
  hasIncompatibility,
  isHardEligible,
  momentForSlot,
  scoreCandidate,
  seasonForDate,
  varietyFactor,
  weightedPick,
} from '@/engine';
import type {
  GenerationContext,
  GenerationSlot,
  MealCandidate,
} from '@/engine';
import { buildWeekSlots, mondayOfCurrentWeek } from '@/lib/week';

const allMoments = {
  lunchWeekday: true,
  dinnerWeekday: true,
  lunchWeekend: true,
  dinnerWeekend: true,
} as const;

function candidate(index: number, overrides: Partial<MealCandidate> = {}): MealCandidate {
  return {
    kind: index % 3 === 0 ? 'recipe' : 'composed',
    signature: `meal:${index}`,
    name: `Repas ${index}`,
    ingredientIds: [`ingredient:${index}`],
    proteinFamily: `family:${index % 5}`,
    starchFamily: `starch:${index % 6}`,
    style: `style:${index % 4}`,
    rating: (index % 5) + 1,
    seasons: ['winter', 'spring', 'summer', 'autumn'],
    allowedMoments: allMoments,
    isActive: true,
    ...overrides,
  };
}

function emptyContext(): GenerationContext {
  return {
    assignedSlots: new Map(),
    consumedHistory: [],
    rejectedSignatures: new Set(),
  };
}

describe('dates et contexte temporel', () => {
  it.each([
    ['2026-01-15', 'winter'],
    ['2026-04-15', 'spring'],
    ['2026-08-15', 'summer'],
    ['2026-10-15', 'autumn'],
    ['2026-12-15', 'winter'],
  ] as const)('associe %s à %s', (date, season) => {
    expect(seasonForDate(date)).toBe(season);
  });

  it('traite samedi et dimanche comme le week-end', () => {
    const saturday: GenerationSlot = {
      slotIndex: 10,
      date: '2026-08-15',
      mealTime: 'lunch',
      isWeekend: true,
    };
    expect(momentForSlot(saturday)).toBe('lunchWeekend');
    expect(buildWeekSlots('2026-08-10').slice(10).every((slot) => slot.isWeekend)).toBe(
      true,
    );
  });

  it('calcule la semaine avec le fuseau de la famille', () => {
    const sundayNightUtc = new Date('2026-08-16T22:30:00.000Z');
    expect(mondayOfCurrentWeek(sundayNightUtc, 'Europe/Paris')).toBe('2026-08-17');
    expect(mondayOfCurrentWeek(sundayNightUtc, 'America/New_York')).toBe(
      '2026-08-10',
    );
  });

  it('réserve un aliment familial au soir lorsqu’il est interdit le midi en semaine', () => {
    const familyMeal = candidate(99, {
      name: 'Saumon',
      allowedMoments: { ...allMoments, lunchWeekday: false },
    });
    const [mondayLunch, mondayDinner] = buildWeekSlots('2026-08-10');
    expect(isHardEligible(familyMeal, mondayLunch, emptyContext(), new Set())).toBe(false);
    expect(isHardEligible(familyMeal, mondayDinner, emptyContext(), new Set())).toBe(true);
  });
});

describe('refroidissement', () => {
  it.each([
    [null, 1],
    [0, 0],
    [6, 0],
    [7, 0.15],
    [9, 0.15],
    [10, 0.4],
    [13, 0.4],
    [14, 0.7],
    [20, 0.7],
    [21, 0.9],
    [27, 0.9],
    [28, 1],
  ])('retourne %s pour %s jours', (days, expected) => {
    expect(coolingFactor(days)).toBe(expected);
  });

  it('ignore l’historique situé après la date cible', () => {
    const scored = scoreCandidate(candidate(1), buildWeekSlots('2026-08-10')[0], {
      ...emptyContext(),
      consumedHistory: [{ signature: 'meal:1', mealDate: '2026-08-20' }],
    });
    expect(scored.coolingWeight).toBe(1);
  });
});

describe('compatibilités et variété', () => {
  it('normalise une incompatibilité dans les deux sens', () => {
    expect(canonicalPair('b', 'a')).toBe(canonicalPair('a', 'b'));
    expect(
      hasIncompatibility(['a', 'b', 'c'], new Set([canonicalPair('b', 'a')])),
    ).toBe(true);
  });

  it('pénalise fortement une famille répétée au repas voisin', () => {
    const context = emptyContext();
    context.assignedSlots.set(0, {
      kind: 'composed',
      signature: 'previous',
      name: 'Poulet précédent',
      proteinFamily: 'poultry',
    });
    expect(
      varietyFactor(candidate(2, { proteinFamily: 'poultry' }), 1, context),
    ).toBe(0.05);
  });
});

describe('tirage et génération', () => {
  it('reproduit le même tirage avec la même seed', () => {
    const scored = [
      { ...scoreCandidate(candidate(1), buildWeekSlots('2026-08-10')[0], emptyContext()) },
      { ...scoreCandidate(candidate(2), buildWeekSlots('2026-08-10')[0], emptyContext()) },
    ];
    const first = weightedPick(scored, createSeededRandom('stable'));
    const second = weightedPick(scored, createSeededRandom('stable'));
    expect(first?.candidate.signature).toBe(second?.candidate.signature);
  });

  it('génère quatorze repas sans doublon exact', () => {
    const result = generateWeek({
      candidates: Array.from({ length: 30 }, (_, index) => candidate(index)),
      slots: buildWeekSlots('2026-08-10'),
      seed: 'complete-week',
    });
    const signatures = result.slots.map((slot) => slot.assignment?.signature);

    expect(result.complete).toBe(true);
    expect(signatures).not.toContain(undefined);
    expect(new Set(signatures).size).toBe(14);
  });

  it('préserve un repas verrouillé pendant une régénération', () => {
    const locked = {
      kind: 'recipe' as const,
      signature: 'locked-meal',
      name: 'Le repas préféré',
    };
    const slots = buildWeekSlots('2026-08-10');
    slots[5] = { ...slots[5], isLocked: true, current: locked };

    const result = generateWeek({
      candidates: Array.from({ length: 30 }, (_, index) => candidate(index)),
      slots,
      seed: 'locked-week',
    });

    expect(result.slots[5].assignment).toEqual(locked);
    expect(result.slots.filter((slot) => slot.assignment).length).toBe(14);
  });

  it('signale deux repas verrouillés identiques', () => {
    const locked = {
      kind: 'recipe' as const,
      signature: 'locked-meal',
      name: 'Le même repas',
    };
    const slots = buildWeekSlots('2026-08-10');
    slots[0] = { ...slots[0], isLocked: true, current: locked };
    slots[1] = { ...slots[1], isLocked: true, current: locked };

    const result = generateWeek({
      candidates: Array.from({ length: 30 }, (_, index) => candidate(index)),
      slots,
      seed: 'duplicate-locked',
    });

    expect(result.complete).toBe(false);
    expect(result.warnings.some((warning) => warning.code === 'LOCKED_DUPLICATE')).toBe(
      true,
    );
  });

  it('propose trois alternatives distinctes sans le repas courant', () => {
    const alternatives = generateAlternatives({
      candidates: Array.from({ length: 10 }, (_, index) => candidate(index)),
      slot: buildWeekSlots('2026-08-10')[0],
      context: emptyContext(),
      currentSignature: 'meal:0',
      seed: 'swap',
    });
    const signatures = alternatives.map((item) => item.candidate.signature);

    expect(signatures).toHaveLength(3);
    expect(signatures).not.toContain('meal:0');
    expect(new Set(signatures).size).toBe(3);
  });
});
