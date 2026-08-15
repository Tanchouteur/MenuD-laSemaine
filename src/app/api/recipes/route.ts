import type { QuantityUnit, Season } from '../../../../generated/prisma/client';
import { apiError, readJson } from '@/lib/api';
import { recipeInputSchema } from '@/lib/schemas';
import { createRecipe, listRecipes } from '@/services/catalog.service';

export async function GET() {
  try {
    return Response.json(await listRecipes());
  } catch (error) {
    return apiError(error);
  }
}

export async function POST(request: Request) {
  try {
    const parsed = recipeInputSchema.parse(await readJson(request));
    return Response.json(
      await createRecipe({
        ...parsed,
        seasons: parsed.seasons as Season[],
        ingredients: parsed.ingredients.map((item) => ({
          ...item,
          unit: item.unit as QuantityUnit,
        })),
      }),
      { status: 201 },
    );
  } catch (error) {
    return apiError(error);
  }
}
