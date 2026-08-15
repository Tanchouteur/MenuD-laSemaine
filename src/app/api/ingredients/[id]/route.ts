import type { IngredientCategory, QuantityUnit, Season } from '../../../../../generated/prisma/client';
import { apiError, readJson } from '@/lib/api';
import { ingredientInputSchema } from '@/lib/schemas';
import { archiveIngredient, updateIngredient } from '@/services/catalog.service';

export async function PATCH(
  request: Request,
  context: { params: Promise<{ id: string }> },
) {
  try {
    const parsed = ingredientInputSchema.partial().parse(await readJson(request));
    return Response.json(
      await updateIngredient((await context.params).id, {
        ...parsed,
        category: parsed.category as IngredientCategory | undefined,
        unit: parsed.unit as QuantityUnit | null | undefined,
        seasons: parsed.seasons as Season[] | undefined,
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
    await archiveIngredient((await context.params).id);
    return new Response(null, { status: 204 });
  } catch (error) {
    return apiError(error);
  }
}
