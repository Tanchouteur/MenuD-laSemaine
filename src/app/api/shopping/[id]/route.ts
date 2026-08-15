import { apiError } from '@/lib/api';
import { deleteManualShoppingEntry, toggleShoppingEntry } from '@/services/shopping-list.service';

export async function PATCH(
  _request: Request,
  context: { params: Promise<{ id: string }> },
) {
  try {
    await toggleShoppingEntry((await context.params).id);
    return new Response(null, { status: 204 });
  } catch (error) {
    return apiError(error);
  }
}

export async function DELETE(
  _request: Request,
  context: { params: Promise<{ id: string }> },
) {
  try {
    await deleteManualShoppingEntry((await context.params).id);
    return new Response(null, { status: 204 });
  } catch (error) {
    return apiError(error);
  }
}
