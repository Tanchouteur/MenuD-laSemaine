import { apiError } from '@/lib/api';
import { buildCatalogExport } from '@/services/catalog-export.service';

export async function GET() {
  try {
    const exported = await buildCatalogExport();
    const date = exported.exportedAt.slice(0, 10);
    return new Response(`${JSON.stringify(exported, null, 2)}\n`, {
      headers: {
        'content-type': 'application/json; charset=utf-8',
        'content-disposition': `attachment; filename="menu-catalogue-${date}.json"`,
        'cache-control': 'no-store',
      },
    });
  } catch (error) {
    return apiError(error);
  }
}
