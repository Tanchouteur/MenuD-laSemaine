import { apiError, readJson } from '@/lib/api';
import { compositionSetupSchema } from '@/lib/schemas';
import { completeCompositionSetup } from '@/services/catalog.service';

export async function POST(request: Request) {
  try {
    const body = compositionSetupSchema.parse(await readJson(request));
    return Response.json(await completeCompositionSetup(body.ingredientIds));
  } catch (error) {
    return apiError(error);
  }
}
