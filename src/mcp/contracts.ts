import { z } from 'zod';
import { ingredientInputSchema, recipeInputSchema } from '@/lib/schemas';

const id = z.string().min(1).max(128);
export const operationKey = z.string().min(8).max(128).regex(/^[a-zA-Z0-9_-]+$/);
export const monday = z.iso.date().refine((date) => new Date(`${date}T00:00:00Z`).getUTCDay() === 1, 'Monday required');
const selection = z.strictObject({
  recipeId: id.optional(), proteinId: id.optional(), starchId: id.optional(),
  vegetableId: id.optional(), starchRecipeId: id.optional(), vegetableRecipeId: id.optional(),
});
export const weekImport = z.strictObject({
  startDate: monday,
  expectedVersion: z.number().int().positive().nullable(),
  slots: z.array(z.strictObject({
    slotIndex: z.number().int().min(0).max(13),
    guestCount: z.number().int().min(1).max(30),
    replaceExisting: z.boolean(),
    meal: z.discriminatedUnion('type', [
      z.strictObject({ type: z.literal('catalog'), selection }),
      z.strictObject({ type: z.literal('custom'), label: z.string().trim().min(1).max(120) }),
      z.strictObject({ type: z.literal('eating_out') }),
    ]),
  })).min(1).max(14).refine((slots) => new Set(slots.map(s => s.slotIndex)).size === slots.length, 'Duplicate slotIndex'),
});
// Unknown fields (including allergies/nutrition) are rejected, never silently discarded.
export const schemas = {
  search_catalog: z.strictObject({ kind: z.enum(['ingredient', 'recipe']), query: z.string().trim().min(1).max(120), offset: z.number().int().min(0).default(0), limit: z.number().int().min(1).max(100).default(20) }),
  get_catalog_item: z.strictObject({ kind: z.enum(['ingredient', 'recipe']), id }),
  list_weeks: z.strictObject({ from: monday, to: monday, offset: z.number().int().min(0).default(0), limit: z.number().int().min(1).max(20).default(10) }),
  get_week: z.strictObject({ startDate: monday }),
  create_ingredient: z.strictObject({ operationKey, acknowledgedMatchIds: z.array(id).max(100).default([]), ingredient: ingredientInputSchema.strict().extend({ portionPerPerson: z.number().min(0.001).max(9999999.999).multipleOf(0.001).nullable().optional() }) }),
  create_recipe: z.strictObject({ operationKey, acknowledgedMatchIds: z.array(id).max(100).default([]), recipe: recipeInputSchema.strict().extend({ ingredients: z.array(z.strictObject({ ingredientId: id, quantity: z.number().min(0.001).max(9999999.999).multipleOf(0.001), unit: recipeInputSchema.shape.ingredients.element.shape.unit })).min(1).max(100) }) }),
  preview_week_import: z.strictObject({ import: weekImport }),
  apply_week_import: z.strictObject({ operationKey, previewDigest: z.string().regex(/^[a-f0-9]{64}$/), import: weekImport }),
  get_operation: z.strictObject({ operationKey }),
};
export type ToolName = keyof typeof schemas;
export type WeekImport = z.infer<typeof weekImport>;
export const writeTools = new Set<ToolName>(['create_ingredient', 'create_recipe', 'apply_week_import']);
