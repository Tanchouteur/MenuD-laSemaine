import type { AssignedMeal } from '@/engine';

export type SlotTypeDto =
  | 'empty'
  | 'composed'
  | 'recipe'
  | 'leftovers'
  | 'eating_out'
  | 'custom';

export type MealSlotDto = {
  id: string;
  slotIndex: number;
  date: string;
  mealTime: 'lunch' | 'dinner';
  guestCount: number;
  isLocked: boolean;
  slotType: SlotTypeDto;
  customLabel: string | null;
  leftoversFromSlotId: string | null;
  assignment: AssignedMeal | null;
};

export type WeeklyPlanDto = {
  id: string;
  startDate: string;
  status: 'draft' | 'confirmed' | 'archived';
  isFavorite: boolean;
  version: number;
  confirmedAt: string | null;
  slots: MealSlotDto[];
};

export type IngredientDto = {
  id: string;
  name: string;
  category: string;
  subFamily: string | null;
  rating: number;
  isActive: boolean;
  portionPerPerson: number | null;
  unit: string | null;
  aisleId: string | null;
  seasons: string[];
  okLunchWeekday: boolean;
  okDinnerWeekday: boolean;
  okLunchWeekend: boolean;
  okDinnerWeekend: boolean;
};

export type RecipeDto = {
  id: string;
  name: string;
  style: string | null;
  rating: number;
  prepTimeMinutes: number | null;
  cookTimeMinutes: number | null;
  basePortions: number;
  isActive: boolean;
  seasons: string[];
  ingredientCount: number;
  okLunchWeekday: boolean;
  okDinnerWeekday: boolean;
  okLunchWeekend: boolean;
  okDinnerWeekend: boolean;
  ingredients: Array<{
    ingredientId: string;
    ingredientName: string;
    quantity: number;
    unit: string;
  }>;
};

export type ShoppingEntryDto = {
  id: string;
  stableKey: string;
  label: string;
  quantity: number | null;
  unit: string | null;
  aisleName: string;
  isChecked: boolean;
  isManual: boolean;
  sourceCount: number;
};
