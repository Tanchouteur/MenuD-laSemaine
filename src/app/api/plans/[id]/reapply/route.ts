import { apiError, readJson } from '@/lib/api';
import { reapplyPlan } from '@/services/weekly-plan.service';

export async function POST(
  request: Request,
  context: { params: Promise<{ id: string }> },
) {
  try {
    const body = (await readJson(request)) as { targetStartDate?: string };
    if (!body.targetStartDate) throw new Error('La semaine cible est obligatoire.');
    return Response.json(
      await reapplyPlan((await context.params).id, body.targetStartDate),
    );
  } catch (error) {
    return apiError(error);
  }
}
