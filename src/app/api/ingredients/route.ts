import type { IngredientCategory, QuantityUnit, Season } from '../../../../generated/prisma/client';
import { apiError, readJson } from '@/lib/api';
import { ingredientInputSchema } from '@/lib/schemas';
import { createIngredient, listIngredients } from '@/services/catalog.service';

export async function GET() {
  try {
    return Response.json(await listIngredients());
  } catch (error) {
    return apiError(error);
  }
}

export async function POST(request: Request) {
  try {
    const parsed = ingredientInputSchema.parse(await readJson(request));
    return Response.json(
      await createIngredient({
        ...parsed,
        category: parsed.category as IngredientCategory,
        unit: parsed.unit as QuantityUnit | null | undefined,
        seasons: parsed.seasons as Season[],
      }),
      { status: 201 },
    );
  } catch (error) {
    return apiError(error);
  }
}
