import { apiError, readJson } from '@/lib/api';
import { applyCatalogEnrichment, previewCatalogEnrichment } from '@/services/catalog-import.service';

export async function POST(request: Request) {
  try {
    const body = await readJson(request) as { mode?: unknown; document?: unknown };
    if (body.mode === 'preview') {
      const { summary } = await previewCatalogEnrichment(body.document);
      return Response.json(summary);
    }
    if (body.mode === 'apply') {
      return Response.json(await applyCatalogEnrichment(body.document));
    }
    throw new Error('Choisissez une prévisualisation ou une application.');
  } catch (error) {
    return apiError(error);
  }
}
