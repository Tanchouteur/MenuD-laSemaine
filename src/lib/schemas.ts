import { z } from 'zod';

const seasons = ['WINTER', 'SPRING', 'SUMMER', 'AUTUMN'] as const;
const units = [
  'GRAM',
  'KILOGRAM',
  'MILLILITER',
  'CENTILITER',
  'LITER',
  'PIECE',
  'SLICE',
  'CAN',
] as const;
const categories = ['PROTEIN', 'STARCH', 'VEGETABLE', 'GROCERY', 'DAIRY', 'OTHER'] as const;

export const ingredientInputSchema = z.object({
  name: z.string().trim().min(1).max(100),
  category: z.enum(categories),
  subFamily: z.string().trim().max(60).nullable().optional(),
  rating: z.number().int().min(1).max(5),
  useInComposedMeals: z.boolean().optional().default(false),
  portionPerPerson: z.number().positive().nullable().optional(),
  unit: z.enum(units).nullable().optional(),
  aisleId: z.string().nullable().optional(),
  seasons: z.array(z.enum(seasons)).min(1),
  okLunchWeekday: z.boolean(),
  okDinnerWeekday: z.boolean(),
  okLunchWeekend: z.boolean(),
  okDinnerWeekend: z.boolean(),
});

export const recipeInputSchema = z.object({
  name: z.string().trim().min(1).max(120),
  style: z.string().trim().max(60).nullable().optional(),
  rating: z.number().int().min(1).max(5),
  prepTimeMinutes: z.number().int().nonnegative().nullable().optional(),
  cookTimeMinutes: z.number().int().nonnegative().nullable().optional(),
  basePortions: z.number().int().min(1).max(30),
  seasons: z.array(z.enum(seasons)).min(1),
  okLunchWeekday: z.boolean(),
  okDinnerWeekday: z.boolean(),
  okLunchWeekend: z.boolean(),
  okDinnerWeekend: z.boolean(),
  ingredients: z
    .array(
      z.object({
        ingredientId: z.string(),
        quantity: z.number().positive(),
        unit: z.enum(units),
      }),
    )
    .min(1),
});

export const settingsSchema = z.object({
  defaultGuestsLunchWeekday: z.number().int().min(1).max(30),
  defaultGuestsDinnerWeekday: z.number().int().min(1).max(30),
  defaultGuestsLunchWeekend: z.number().int().min(1).max(30),
  defaultGuestsDinnerWeekend: z.number().int().min(1).max(30),
  onboardingCompleted: z.boolean().optional(),
});

export const compositionSetupSchema = z.object({
  ingredientIds: z.array(z.string()).max(500),
});
