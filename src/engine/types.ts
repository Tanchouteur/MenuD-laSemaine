export type Season = 'winter' | 'spring' | 'summer' | 'autumn';

export type MomentContext =
  | 'lunchWeekday'
  | 'dinnerWeekday'
  | 'lunchWeekend'
  | 'dinnerWeekend';

export type AllowedMoments = Readonly<Record<MomentContext, boolean>>;

export type MealKind = 'recipe' | 'composed';

export type MealCandidate = {
  kind: MealKind;
  signature: string;
  name: string;
  description?: string;
  totalMinutes?: number;
  recipeId?: string;
  proteinId?: string;
  starchId?: string;
  vegetableId?: string;
  ingredientIds: readonly string[];
  proteinFamily?: string;
  starchFamily?: string;
  style?: string;
  rating: number;
  seasons: readonly Season[];
  allowedMoments: AllowedMoments;
  isActive: boolean;
};

export type AssignedMeal = {
  kind: MealKind;
  signature: string;
  name: string;
  description?: string;
  totalMinutes?: number;
  proteinFamily?: string;
  starchFamily?: string;
  style?: string;
};

export type GenerationSlot = {
  slotIndex: number;
  date: string;
  mealTime: 'lunch' | 'dinner';
  isWeekend: boolean;
  isLocked?: boolean;
  skipGeneration?: boolean;
  current?: AssignedMeal | null;
};

export type ConsumedMeal = {
  signature: string;
  mealDate: string;
};

export type GenerationContext = {
  assignedSlots: Map<number, AssignedMeal>;
  consumedHistory: readonly ConsumedMeal[];
  rejectedSignatures: Set<string>;
};

export type ScoredCandidate = {
  candidate: MealCandidate;
  score: number;
  appreciationWeight: number;
  coolingWeight: number;
  varietyWeight: number;
  reasons: readonly string[];
};

export type GenerationWarningCode =
  | 'VARIETY_RELAXED'
  | 'COOLING_RELAXED'
  | 'LOCKED_SLOT_EMPTY'
  | 'LOCKED_DUPLICATE'
  | 'NO_CANDIDATE';

export type GenerationWarning = {
  slotIndex: number;
  code: GenerationWarningCode;
  message: string;
};

export type GeneratedSlot = GenerationSlot & {
  assignment: AssignedMeal | null;
};

export type GenerationResult = {
  slots: GeneratedSlot[];
  warnings: GenerationWarning[];
  seed: string;
  complete: boolean;
  quality: number;
};

export type RandomSource = {
  next(): number;
};

export type GenerationSettings = {
  attempts?: number;
};
