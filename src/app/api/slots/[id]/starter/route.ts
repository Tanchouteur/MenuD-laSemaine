import { apiError, readJson } from '@/lib/api';
import { setStarterForSlot } from '@/services/starter.service';

export async function PUT(request: Request, context: { params: Promise<{ id: string }> }) {
  try {
    const body = await readJson(request) as {
      ingredientId?: string | null; recipeId?: string | null; automatic?: boolean; version?: number;
    };
    return Response.json(await setStarterForSlot((await context.params).id, body, body.version));
  } catch (error) {
    return apiError(error);
  }
}
