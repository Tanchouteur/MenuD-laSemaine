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

  return weightedPickMany(
    scored,
    input.count ?? 3,
    createSeededRandom(input.seed),
  );
}
