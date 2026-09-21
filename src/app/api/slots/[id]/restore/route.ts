import { apiError, readJson } from '@/lib/api';
import { restoreMealSlot } from '@/services/generation.service';
import type { MealSlotDto } from '@/types/api';

export async function POST(
  request: Request,
  context: { params: Promise<{ id: string }> },
) {
  try {
    const body = (await readJson(request)) as {
      state?: MealSlotDto['restoreState'];
      version?: number;
    };
    if (!body.state) throw new Error('Aucun repas à restaurer.');
    return Response.json(await restoreMealSlot(
      (await context.params).id,
      body.state,
      body.version,
    ));
  } catch (error) {
    return apiError(error);
  }
}
