import { apiError, readJson } from '@/lib/api';
import { generatePersistedWeek } from '@/services/generation.service';

export async function POST(request: Request) {
  try {
    const body = (await readJson(request)) as { startDate?: string; seed?: string; version?: number };
    if (!body.startDate) throw new Error('La date de la semaine est obligatoire.');
    const seed = body.seed?.trim() || crypto.randomUUID();
    return Response.json(await generatePersistedWeek(body.startDate, seed, body.version));
  } catch (error) {
    return apiError(error);
  }
}
