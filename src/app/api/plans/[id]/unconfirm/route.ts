import { apiError, readJson } from '@/lib/api';
import { unconfirmPlan } from '@/services/weekly-plan.service';

export async function POST(
  request: Request,
  context: { params: Promise<{ id: string }> },
) {
  try {
    const body = (await readJson(request)) as { version?: number };
    return Response.json(
      await unconfirmPlan((await context.params).id, body.version),
    );
  } catch (error) {
    return apiError(error);
  }
}
