import { apiError, readJson } from '@/lib/api';
import { chooseCandidateForSlot, chooseManualForSlot } from '@/services/generation.service';

export async function POST(
  request: Request,
  context: { params: Promise<{ id: string }> },
) {
  try {
    const body = (await readJson(request)) as {
      signature?: string;
      recipeId?: string;
      proteinId?: string;
      starchId?: string;
      vegetableId?: string;
      starchRecipeId?: string;
      vegetableRecipeId?: string;
      version?: number;
    };
    if (!body.signature && !body.recipeId && !body.proteinId) {
      throw new Error('Choisissez un repas.');
    }
    if (!body.signature) {
      return Response.json(await chooseManualForSlot(
        (await context.params).id,
        body,
        body.version,
      ));
    }
    return Response.json(
      await chooseCandidateForSlot((await context.params).id, body.signature, body.version),
    );
  } catch (error) {
    return apiError(error);
  }
}
