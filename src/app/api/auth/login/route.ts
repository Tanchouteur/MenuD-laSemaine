import { NextResponse } from 'next/server';
import { FAMILY_COOKIE, familyAuthConfigured, familyToken } from '@/lib/family-auth';
import { readJson } from '@/lib/api';

export async function POST(request: Request) {
  if (!familyAuthConfigured()) return Response.json({ ok: true });
  const body = (await readJson(request)) as { password?: string };
  const supplied = await familyToken(body.password ?? '', process.env.AUTH_SECRET!);
  const expected = await familyToken(process.env.FAMILY_PASSWORD!, process.env.AUTH_SECRET!);
  if (supplied !== expected) return Response.json({ error: 'Le mot de passe n’est pas le bon.' }, { status: 401 });
  const response = NextResponse.json({ ok: true });
  response.cookies.set(FAMILY_COOKIE, expected, { httpOnly: true, sameSite: 'lax', secure: process.env.NODE_ENV === 'production', maxAge: 60 * 60 * 24 * 90, path: '/' });
  return response;
}
