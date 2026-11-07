import { z } from 'zod';
import { ingredientInputSchema, recipeInputSchema } from '@/lib/schemas';
import { weekImport, type ToolName } from './contracts';

const ingredient = ingredientInputSchema.extend({ id: z.string(), isActive: z.boolean() });
const recipe = recipeInputSchema.extend({ id: z.string(), isActive: z.boolean(), ingredientCount: z.number(), ingredients: z.array(recipeInputSchema.shape.ingredients.element.extend({ ingredientName: z.string() })) });
const item = z.union([ingredient, recipe]);
const slot = z.looseObject({ id: z.string(), slotIndex: z.number(), date: z.string(), guestCount: z.number(), isLocked: z.boolean(), starterIsLocked: z.boolean(), slotType: z.enum(['empty', 'composed', 'recipe', 'leftovers', 'eating_out', 'custom']) });
const plan = z.looseObject({ id: z.string(), startDate: z.string(), version: z.number(), status: z.enum(['draft', 'confirmed', 'archived']), slots: z.array(slot) });
const missing = z.array(z.object({ slotIndex: z.number(), fields: z.array(z.string()) }));
const dataSchemas: Record<ToolName, z.ZodType> = {
  search_catalog: z.object({ items: z.array(item), total: z.number(), ambiguous: z.boolean() }),
  get_catalog_item: z.object({ item, dietaryInformation: z.string() }),
  list_weeks: z.object({ items: z.array(z.object({ id: z.string(), startDate: z.string(), status: z.enum(['DRAFT', 'CONFIRMED', 'ARCHIVED']), version: z.number() })) }),
  get_week: z.object({ plan: plan.nullable() }),
  create_ingredient: z.object({ ingredient, missingInformation: z.array(z.string()) }),
  create_recipe: z.object({ recipe, missingInformation: z.array(z.string()) }),
  preview_week_import: z.object({ startDate: z.string(), expectedVersion: z.number().nullable(), confirmWeek: z.boolean(), previewDigest: z.string(), changes: z.array(z.object({ slotIndex: z.number(), before: slot.nullable(), after: weekImport.shape.slots.element, snapshot: z.unknown() })), missingInformation: missing, dietaryInformation: z.string() }),
  apply_week_import: z.object({ plan, missingInformation: missing, warnings: z.array(z.string()) }),
  get_operation: z.union([z.object({ state: z.literal('unknown'), guidance: z.string() }), z.object({ state: z.literal('completed'), tool: z.string(), result: z.unknown() })]),
};
// Business errors also have a valid structured result; SDK JSON-RPC errors remain standard.
function envelope(data: z.ZodType) {
  return z.object({
    ok: z.boolean(), data: data.optional(),
    error: z.object({ code: z.string(), retryable: z.boolean(), details: z.record(z.string(), z.unknown()) }).optional(),
  });
}
export const resultSchemas = Object.fromEntries(Object.entries(dataSchemas).map(([name, data]) => [name, envelope(data)])) as Record<ToolName, ReturnType<typeof envelope>>;

/** Strip internal DTO metadata before emitting the strict advertised wire schema. */
export function serializeToolResult(name: ToolName, result: unknown) {
  const parsed = resultSchemas[name].safeParse(JSON.parse(JSON.stringify(result)));
  if (parsed.success) return parsed.data as Record<string, unknown>;
  return { ok: false, error: { code: 'RESPONSE_CONTRACT_ERROR', retryable: false,
    details: { fields: parsed.error.issues.map(issue => ({ path: issue.path.join('.'), code: issue.code })) } } };
}
