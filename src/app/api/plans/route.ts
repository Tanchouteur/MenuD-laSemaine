import { apiError, readJson } from '@/lib/api';
import { ensureWeeklyPlan, listPlans } from '@/services/weekly-plan.service';

export async function GET() {
  try {
    return Response.json(await listPlans());
  } catch (error) {
    return apiError(error);
  }
}

export async function POST(request: Request) {
  try {
    const body = (await readJson(request)) as { startDate?: string };
    if (!body.startDate) throw new Error('La date de la semaine est obligatoire.');
    return Response.json(await ensureWeeklyPlan(body.startDate), { status: 201 });
  } catch (error) {
    return apiError(error);
  }
}
