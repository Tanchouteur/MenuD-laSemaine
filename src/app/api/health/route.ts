import { getPrisma } from '@/lib/prisma';

export async function GET() {
  try {
    await getPrisma().$queryRaw`SELECT 1`;
    return Response.json({ status: 'ok' });
  } catch {
    return Response.json({ status: 'error' }, { status: 503 });
  }
}
