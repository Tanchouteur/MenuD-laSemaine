import { apiError } from '@/lib/api';
import { removeIncompatibility } from '@/services/catalog.service';

export async function DELETE(
  _request: Request,
  context: { params: Promise<{ id: string }> },
) {
  try {
    await removeIncompatibility((await context.params).id);
    return new Response(null, { status: 204 });
  } catch (error) {
    return apiError(error);
  }
}
