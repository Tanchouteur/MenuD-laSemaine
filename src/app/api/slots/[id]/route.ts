import { apiError, readJson } from '@/lib/api';
import { updateMealSlot } from '@/services/generation.service';

export async function PATCH(
  request: Request,
  context: { params: Promise<{ id: string }> },
) {
  try {
    const body = (await readJson(request)) as Parameters<typeof updateMealSlot>[1] & { version?: number };
    const { version, ...update } = body;
    return Response.json(await updateMealSlot((await context.params).id, update, version));
  } catch (error) {
    return apiError(error);
  }
}
