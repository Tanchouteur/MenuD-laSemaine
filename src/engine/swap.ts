import { isHardEligible } from './filters';
import { weightedPickMany } from './picker';
import { createSeededRandom } from './random';
import { scoreCandidate } from './scorer';
import type {
  GenerationContext,
  GenerationSlot,
  MealCandidate,
  ScoredCandidate,
} from './types';

type SwapInput = {
  candidates: readonly MealCandidate[];
  slot: GenerationSlot;
  context: GenerationContext;
  incompatibilities?: ReadonlySet<string>;
  currentSignature?: string;
  seed: string;
  count?: number;
};

export function generateAlternatives(input: SwapInput): ScoredCandidate[] {
  const rejected = new Set(input.context.rejectedSignatures);
  if (input.currentSignature) rejected.add(input.currentSignature);

  const context: GenerationContext = {
    ...input.context,
    rejectedSignatures: rejected,
  };
  const eligible = input.candidates.filter((candidate) =>
    isHardEligible(
      candidate,
      input.slot,
      context,
      input.incompatibilities ?? new Set(),
    ),
  );
  let scored = eligible.map((candidate) =>
    scoreCandidate(candidate, input.slot, context),
  );

  if (!scored.some((candidate) => candidate.score > 0)) {
    scored = eligible.map((candidate) =>
      scoreCandidate(candidate, input.slot, context, {
        ignoreCooling: true,
        ignoreVariety: true,
      }),
    );
  }

  const composed = scored.filter((item) => item.candidate.kind === 'composed');
  const composedTotal = composed.reduce((sum, item) => sum + Math.max(0, item.score), 0);
  const groupWeights = { complete: 0.8, starch: 0.1, vegetable: 0.1 } as const;
  const availableTypes = (Object.keys(groupWeights) as Array<keyof typeof groupWeights>)
    .filter((type) => composed.some((item) => item.candidate.compositionType === type));
  const availableWeight = availableTypes.reduce((sum, type) => sum + groupWeights[type], 0);
  if (composedTotal > 0 && availableWeight > 0) {
    scored = scored.map((item) => {
      const type = item.candidate.compositionType;
      if (item.candidate.kind !== 'composed' || !type) return item;
      const groupTotal = composed
        .filter((candidate) => candidate.candidate.compositionType === type)
        .reduce((sum, candidate) => sum + Math.max(0, candidate.score), 0);
      if (groupTotal <= 0) return item;
      return {
        ...item,
        score: item.score / groupTotal * composedTotal * groupWeights[type] / availableWeight,
      };
    });
  }

  return weightedPickMany(
    scored,
    input.count ?? 3,
    createSeededRandom(input.seed),
  );
}
