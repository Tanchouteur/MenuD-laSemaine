import { apiError, readJson } from '@/lib/api';
import { settingsSchema } from '@/lib/schemas';
import { getSettings, updateSettings } from '@/services/catalog.service';

export async function GET() {
  try {
    return Response.json(await getSettings());
  } catch (error) {
    return apiError(error);
  }
}

export async function PATCH(request: Request) {
  try {
    return Response.json(await updateSettings(settingsSchema.parse(await readJson(request))));
  } catch (error) {
    return apiError(error);
  }
}
