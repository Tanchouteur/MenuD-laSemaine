import { describe, expect, it } from 'vitest';
import {
  candidateAsAssignment,
  parseMealSnapshot,
  snapshotAsAssignment,
} from '@/domain/meal-snapshot';
import type { MealCandidate } from '@/engine';

const snapshot = {
  version: 1 as const,
  kind: 'recipe' as const,
  signature: 'recipe:1',
  name: 'Lasagnes',
  totalMinutes: 50,
  style: 'italien',
  items: [{
    ingredientId: 'tomate',
    name: 'Tomate',
    quantityPerPerson: 150,
    unit: 'GRAM',
    aisleId: null,
    aisleName: null,
  }],
};

describe('instantané de repas', () => {
  it('valide un instantané versionné et refuse les données historiques corrompues', () => {
    expect(parseMealSnapshot(snapshot)).toEqual(snapshot);
    expect(parseMealSnapshot({ ...snapshot, version: 2 })).toBeNull();
    expect(parseMealSnapshot({ ...snapshot, items: [{ ...snapshot.items[0], quantityPerPerson: -1 }] })).toBeNull();
    expect(parseMealSnapshot(null)).toBeNull();
  });

  it('ne transporte dans une affectation que les données d’affichage stables', () => {
    expect(snapshotAsAssignment(snapshot)).toEqual({
      kind: 'recipe', signature: 'recipe:1', name: 'Lasagnes', totalMinutes: 50, style: 'italien',
    });
    const candidate: MealCandidate = {
      kind: 'composed', signature: 'composed:1', name: 'Assiette', ingredientIds: [],
      rating: 3, seasons: ['autumn'], allowedMoments: {
        lunchWeekday: true, dinnerWeekday: true, lunchWeekend: true, dinnerWeekend: true,
      }, isActive: true, compositionType: 'complete', proteinFamily: 'volaille',
    };
    expect(candidateAsAssignment(candidate)).toEqual({
      kind: 'composed', signature: 'composed:1', name: 'Assiette',
      compositionType: 'complete', proteinFamily: 'volaille',
    });
  });
});
