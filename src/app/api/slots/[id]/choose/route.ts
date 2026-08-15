import { apiError, readJson } from '@/lib/api';
import { chooseCandidateForSlot } from '@/services/generation.service';

export async function POST(
  request: Request,
  context: { params: Promise<{ id: string }> },
) {
  try {
    const body = (await readJson(request)) as { signature?: string; version?: number };
    if (!body.signature) throw new Error('Choisissez une proposition.');
    return Response.json(
      await chooseCandidateForSlot((await context.params).id, body.signature, body.version),
    );
  } catch (error) {
    return apiError(error);
  }
}
