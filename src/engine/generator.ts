import { isHardEligible } from './filters';
import { weightedPick } from './picker';
import { createSeededRandom } from './random';
import { scoreCandidate } from './scorer';
import type {
  AssignedMeal,
  GeneratedSlot,
  GenerationContext,
  GenerationResult,
  GenerationSettings,
  GenerationSlot,
  GenerationWarning,
  MealCandidate,
  RandomSource,
  ScoredCandidate,
  ConsumedMeal,
} from './types';

type GenerateWeekInput = {
  candidates: readonly MealCandidate[];
  slots: readonly GenerationSlot[];
  consumedHistory?: readonly ConsumedMeal[];
  incompatibilities?: ReadonlySet<string>;
  seed: string;
  settings?: GenerationSettings;
};

type Selection = {
  scored: ScoredCandidate;
  warning?: GenerationWarning;
};

function asAssignment(candidate: MealCandidate): AssignedMeal {
  return {
    kind: candidate.kind,
    signature: candidate.signature,
    name: candidate.name,
    description: candidate.description,
    totalMinutes: candidate.totalMinutes,
    proteinFamily: candidate.proteinFamily,
    starchFamily: candidate.starchFamily,
    style: candidate.style,
  };
}

function recipeProbability(
  slot: GenerationSlot,
  context: GenerationContext,
): number {
  let probability = slot.isWeekend
    ? 0.55
    : slot.mealTime === 'lunch'
      ? 0.25
      : 0.4;
  const assigned = [...context.assignedSlots.values()];

  if (assigned.length > 4) {
    const recipeRatio =
      assigned.filter((meal) => meal.kind === 'recipe').length / assigned.length;
    if (recipeRatio > 0.5) probability *= 0.5;
    if (recipeRatio < 0.2) probability *= 1.5;
  }

  return Math.min(0.8, Math.max(0.1, probability));
}

function pickRespectingKind(
  scored: readonly ScoredCandidate[],
  slot: GenerationSlot,
  context: GenerationContext,
  random: RandomSource,
): ScoredCandidate | null {
  const recipes = scored.filter(
    (item) => item.score > 0 && item.candidate.kind === 'recipe',
  );
  const composed = scored.filter(
    (item) => item.score > 0 && item.candidate.kind === 'composed',
  );

  if (recipes.length === 0) return weightedPick(composed, random);
  if (composed.length === 0) return weightedPick(recipes, random);

  const preferred =
    random.next() < recipeProbability(slot, context) ? recipes : composed;
  const fallback = preferred === recipes ? composed : recipes;

  return weightedPick(preferred, random) ?? weightedPick(fallback, random);
}

function selectForSlot(
  candidates: readonly MealCandidate[],
  slot: GenerationSlot,
  context: GenerationContext,
  incompatibilities: ReadonlySet<string>,
  random: RandomSource,
): Selection | null {
  const hardEligible = candidates.filter((candidate) =>
    isHardEligible(candidate, slot, context, incompatibilities),
  );

  const normal = hardEligible.map((candidate) =>
    scoreCandidate(candidate, slot, context),
  );
  const normalPick = pickRespectingKind(normal, slot, context, random);
  if (normalPick) return { scored: normalPick };

  const withoutVariety = hardEligible.map((candidate) =>
    scoreCandidate(candidate, slot, context, { ignoreVariety: true }),
  );
  const varietyPick = pickRespectingKind(
    withoutVariety,
    slot,
    context,
    random,
  );
  if (varietyPick) {
    return {
      scored: varietyPick,
      warning: {
        slotIndex: slot.slotIndex,
        code: 'VARIETY_RELAXED',
        message: 'La variété a été légèrement assouplie pour compléter ce repas.',
      },
    };
  }

  const withoutCooling = hardEligible.map((candidate) =>
    scoreCandidate(candidate, slot, context, {
      ignoreCooling: true,
      ignoreVariety: true,
    }),
  );
  const coolingPick = pickRespectingKind(
    withoutCooling,
    slot,
    context,
    random,
  );
  if (coolingPick) {
    return {
      scored: coolingPick,
      warning: {
        slotIndex: slot.slotIndex,
        code: 'COOLING_RELAXED',
        message: 'Un repas récent a été autorisé faute d’alternative disponible.',
      },
    };
  }

  return null;
}

function generateAttempt(
  input: GenerateWeekInput,
  attemptSeed: string,
): GenerationResult {
  const random = createSeededRandom(attemptSeed);
  const incompatibilities = input.incompatibilities ?? new Set<string>();
  const context: GenerationContext = {
    assignedSlots: new Map(),
    consumedHistory: input.consumedHistory ?? [],
    rejectedSignatures: new Set(),
  };
  const warnings: GenerationWarning[] = [];
  const assignments = new Map<number, AssignedMeal>();
  let quality = 0;

  for (const slot of input.slots) {
    if (slot.isLocked && slot.current) {
      const isDuplicate = [...context.assignedSlots.values()].some(
        (assigned) => assigned.signature === slot.current?.signature,
      );
      if (isDuplicate) {
        warnings.push({
          slotIndex: slot.slotIndex,
          code: 'LOCKED_DUPLICATE',
          message: 'Ce repas verrouillé apparaît déjà ailleurs dans la semaine.',
        });
      }
      context.assignedSlots.set(slot.slotIndex, slot.current);
      assignments.set(slot.slotIndex, slot.current);
    } else if (slot.isLocked) {
      warnings.push({
        slotIndex: slot.slotIndex,
        code: 'LOCKED_SLOT_EMPTY',
        message: 'Ce créneau est verrouillé mais ne contient aucun repas.',
      });
    }
  }

  const remaining = input.slots.filter(
    (slot) =>
      !slot.skipGeneration && !slot.isLocked && !assignments.has(slot.slotIndex),
  );

  while (remaining.length > 0) {
    remaining.sort((first, second) => {
      const firstCount = input.candidates.filter((candidate) =>
        isHardEligible(candidate, first, context, incompatibilities),
      ).length;
      const secondCount = input.candidates.filter((candidate) =>
        isHardEligible(candidate, second, context, incompatibilities),
      ).length;
      return firstCount - secondCount || first.slotIndex - second.slotIndex;
    });

    const slot = remaining.shift();
    if (!slot) break;

    const selection = selectForSlot(
      input.candidates,
      slot,
      context,
      incompatibilities,
      random,
    );

    if (!selection) {
      warnings.push({
        slotIndex: slot.slotIndex,
        code: 'NO_CANDIDATE',
        message: 'Aucun repas compatible n’a été trouvé pour ce créneau.',
      });
      continue;
    }

    const assignment = asAssignment(selection.scored.candidate);
    assignments.set(slot.slotIndex, assignment);
    context.assignedSlots.set(slot.slotIndex, assignment);
    quality += Math.log(Math.max(selection.scored.score, 0.0001));
    if (selection.warning) warnings.push(selection.warning);
  }

  const slots: GeneratedSlot[] = input.slots
    .map((slot) => ({
      ...slot,
      assignment: assignments.get(slot.slotIndex) ?? slot.current ?? null,
    }))
    .sort((first, second) => first.slotIndex - second.slotIndex);

  const hasBlockingWarning = warnings.some(
    (warning) =>
      warning.code === 'NO_CANDIDATE' ||
      warning.code === 'LOCKED_SLOT_EMPTY' ||
      warning.code === 'LOCKED_DUPLICATE',
  );

  return {
    slots,
    warnings,
    seed: attemptSeed,
    complete:
      slots.every((slot) => slot.skipGeneration || slot.assignment !== null) &&
      !hasBlockingWarning,
    quality,
  };
}

export function generateWeek(input: GenerateWeekInput): GenerationResult {
  const attemptCount = Math.min(30, Math.max(1, input.settings?.attempts ?? 8));
  const attempts = Array.from({ length: attemptCount }, (_, index) =>
    generateAttempt(input, `${input.seed}:${index}`),
  );

  attempts.sort((first, second) => {
    if (first.complete !== second.complete) return first.complete ? -1 : 1;

    const firstFilled = first.slots.filter((slot) => slot.assignment).length;
    const secondFilled = second.slots.filter((slot) => slot.assignment).length;
    return (
      secondFilled - firstFilled ||
      first.warnings.length - second.warnings.length ||
      second.quality - first.quality
    );
  });

  const topCount = Math.min(3, attempts.length);
  const random = createSeededRandom(`${input.seed}:best`);
  const selected = attempts[Math.floor(random.next() * topCount)] ?? attempts[0];

  return { ...selected, seed: input.seed };
}
