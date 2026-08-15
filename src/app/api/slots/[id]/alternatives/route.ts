import { apiError, readJson } from '@/lib/api';
import { alternativesForSlot } from '@/services/generation.service';

export async function POST(
  request: Request,
  context: { params: Promise<{ id: string }> },
) {
  try {
    const body = (await readJson(request)) as {
      rejectedSignatures?: string[];
      seed?: string;
    };
    const alternatives = await alternativesForSlot(
      (await context.params).id,
      body.rejectedSignatures ?? [],
      body.seed ?? crypto.randomUUID(),
    );
    return Response.json(
      alternatives.map((item) => ({
        signature: item.candidate.signature,
        name: item.candidate.name,
        description: item.candidate.description,
        totalMinutes: item.candidate.totalMinutes,
        reasons: item.reasons,
      })),
    );
  } catch (error) {
    return apiError(error);
  }
}
