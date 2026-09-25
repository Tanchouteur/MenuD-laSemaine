import { apiError } from '@/lib/api';
import { deleteManualShoppingEntry, requireConfirmedShoppingEntry, toggleShoppingEntry } from '@/services/shopping-list.service';

export async function PATCH(
  _request: Request,
  context: { params: Promise<{ id: string }> },
) {
  try {
    const { id } = await context.params;
    await requireConfirmedShoppingEntry(id);
    await toggleShoppingEntry(id);
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
    const { id } = await context.params;
    await requireConfirmedShoppingEntry(id);
    await deleteManualShoppingEntry(id);
    return new Response(null, { status: 204 });
  } catch (error) {
    return apiError(error);
  }
}
