import { z } from 'zod';
import type { AssignedMeal, MealCandidate } from '@/engine';

export const snapshotItemSchema = z.object({
  ingredientId: z.string(),
  name: z.string(),
  quantityPerPerson: z.number().positive().nullable(),
  unit: z.string().nullable(),
  aisleId: z.string().nullable(),
  aisleName: z.string().nullable(),
});

export const mealSnapshotSchema = z.object({
  version: z.literal(1),
  kind: z.enum(['recipe', 'composed']),
  signature: z.string(),
  repeatKey: z.string().optional(),
  name: z.string(),
  description: z.string().optional(),
  totalMinutes: z.number().int().nonnegative().optional(),
  proteinFamily: z.string().optional(),
  starchFamily: z.string().optional(),
  style: z.string().optional(),
  compositionType: z.enum(['complete', 'starch', 'vegetable']).optional(),
  items: z.array(snapshotItemSchema),
});

export type MealSnapshot = z.infer<typeof mealSnapshotSchema>;

export function parseMealSnapshot(value: unknown): MealSnapshot | null {
  const result = mealSnapshotSchema.safeParse(value);
  return result.success ? result.data : null;
}

export function snapshotAsAssignment(snapshot: MealSnapshot): AssignedMeal {
  return {
    kind: snapshot.kind,
    signature: snapshot.signature,
    repeatKey: snapshot.repeatKey,
    name: snapshot.name,
    description: snapshot.description,
    totalMinutes: snapshot.totalMinutes,
    proteinFamily: snapshot.proteinFamily,
    starchFamily: snapshot.starchFamily,
    style: snapshot.style,
    compositionType: snapshot.compositionType,
  };
}

export function candidateAsAssignment(candidate: MealCandidate): AssignedMeal {
  return {
    kind: candidate.kind,
    signature: candidate.signature,
    repeatKey: candidate.repeatKey,
    name: candidate.name,
    description: candidate.description,
    totalMinutes: candidate.totalMinutes,
    proteinFamily: candidate.proteinFamily,
    starchFamily: candidate.starchFamily,
    style: candidate.style,
    compositionType: candidate.compositionType,
  };
}
