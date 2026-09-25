import { coolingFactor, daysSinceLastConsumption } from './cooling';
import { varietyFactor } from './variety';
import type {
  GenerationContext,
  GenerationSlot,
  MealCandidate,
  ScoredCandidate,
} from './types';

type ScoreOptions = {
  ignoreCooling?: boolean;
  ignoreVariety?: boolean;
};

export function scoreCandidate(
  candidate: MealCandidate,
  slot: GenerationSlot,
  context: GenerationContext,
  options: ScoreOptions = {},
): ScoredCandidate {
  const appreciationWeight = Math.min(5, Math.max(1, candidate.rating));
  const elapsedDays = daysSinceLastConsumption(
    candidate.repeatKey ?? candidate.signature,
    slot.date,
    context.consumedHistory,
  );
  const nearestInWeek = [...context.assignedSlots.entries()]
    .filter(([, assigned]) => (assigned.repeatKey ?? assigned.signature) === (candidate.repeatKey ?? candidate.signature))
    .map(([index]) => Math.floor(Math.abs(index - slot.slotIndex) / 2))
    .sort((first, second) => first - second)[0];
  const coolingWeight = options.ignoreCooling ? 1 : coolingFactor(
    nearestInWeek === undefined ? elapsedDays : Math.min(elapsedDays ?? Infinity, nearestInWeek),
  );
  const varietyWeight = options.ignoreVariety
    ? 1
    : varietyFactor(candidate, slot.slotIndex, context);
  const reasons: string[] = ['De saison'];

  if (elapsedDays === null || elapsedDays >= 28) {
    reasons.push('Pas mangé récemment');
  }
  if (candidate.totalMinutes !== undefined && candidate.totalMinutes <= 25) {
    reasons.push('Rapide à préparer');
  }

  return {
    candidate,
    score: appreciationWeight * coolingWeight * varietyWeight,
    appreciationWeight,
    coolingWeight,
    varietyWeight,
    reasons,
  };
}
