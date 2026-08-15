import { apiError, readJson } from '@/lib/api';
import {
  addIncompatibility,
  listIncompatibilities,
} from '@/services/catalog.service';

export async function GET() {
  try {
    return Response.json(await listIncompatibilities());
  } catch (error) {
    return apiError(error);
  }
}

export async function POST(request: Request) {
  try {
    const body = (await readJson(request)) as { firstId?: string; secondId?: string };
    if (!body.firstId || !body.secondId) throw new Error('Choisissez deux ingrédients.');
    return Response.json(await addIncompatibility(body.firstId, body.secondId), {
      status: 201,
    });
  } catch (error) {
    return apiError(error);
  }
}
