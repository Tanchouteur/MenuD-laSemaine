import { apiError, readJson } from '@/lib/api';
import {
  addManualShoppingEntry,
  getShoppingList,
  rebuildShoppingList,
  requireConfirmedShoppingPlan,
  uncheckAllShoppingEntries,
} from '@/services/shopping-list.service';

export async function GET(request: Request) {
  try {
    const planId = new URL(request.url).searchParams.get('planId');
    if (!planId) throw new Error('La semaine est obligatoire.');
    await requireConfirmedShoppingPlan(planId);
    await rebuildShoppingList(planId);
    return Response.json(await getShoppingList(planId));
  } catch (error) {
    return apiError(error);
  }
}

export async function PATCH(request: Request) {
  try {
    const body = (await readJson(request)) as { planId?: string };
    if (!body.planId) throw new Error('La semaine est obligatoire.');
    await requireConfirmedShoppingPlan(body.planId);
    await uncheckAllShoppingEntries(body.planId);
    return Response.json(await getShoppingList(body.planId));
  } catch (error) {
    return apiError(error);
  }
}

export async function POST(request: Request) {
  try {
    const body = (await readJson(request)) as { planId?: string; label?: string };
    if (!body.planId || !body.label) throw new Error('Le libellé est obligatoire.');
    await requireConfirmedShoppingPlan(body.planId);
    await addManualShoppingEntry(body.planId, body.label);
    return Response.json(await getShoppingList(body.planId), { status: 201 });
  } catch (error) {
    return apiError(error);
  }
}
