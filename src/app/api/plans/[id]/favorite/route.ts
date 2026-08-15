import { apiError } from '@/lib/api';
import { toggleFavorite } from '@/services/weekly-plan.service';

export async function POST(
  _request: Request,
  context: { params: Promise<{ id: string }> },
) {
  try {
    return Response.json(await toggleFavorite((await context.params).id));
  } catch (error) {
    return apiError(error);
  }
}
