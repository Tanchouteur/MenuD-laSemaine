import { apiError, readJson } from '@/lib/api';
import { rebuildShoppingList } from '@/services/shopping-list.service';
import { confirmPlan } from '@/services/weekly-plan.service';

export async function POST(
  request: Request,
  context: { params: Promise<{ id: string }> },
) {
  try {
    const { id } = await context.params;
    const body = (await readJson(request)) as { version?: number };
    await rebuildShoppingList(id);
    return Response.json(await confirmPlan(id, body.version));
  } catch (error) {
    return apiError(error);
  }
}
