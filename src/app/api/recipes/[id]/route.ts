import type { QuantityUnit, Season } from '../../../../../generated/prisma/client';
import { apiError, readJson } from '@/lib/api';
import { recipeInputSchema } from '@/lib/schemas';
import { archiveRecipe, updateRecipe } from '@/services/catalog.service';

export async function PUT(
  request: Request,
  context: { params: Promise<{ id: string }> },
) {
  try {
    const parsed = recipeInputSchema.parse(await readJson(request));
    return Response.json(
      await updateRecipe((await context.params).id, {
        ...parsed,
        seasons: parsed.seasons as Season[],
        ingredients: parsed.ingredients.map((item) => ({
          ...item,
          unit: item.unit as QuantityUnit,
        })),
      }),
    );
  } catch (error) {
    return apiError(error);
  }
}

export async function DELETE(
  _request: Request,
  context: { params: Promise<{ id: string }> },
) {
  try {
    await archiveRecipe((await context.params).id);
    return new Response(null, { status: 204 });
  } catch (error) {
    return apiError(error);
  }
}
