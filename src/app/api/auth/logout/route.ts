import { NextResponse } from 'next/server';
import { FAMILY_COOKIE } from '@/lib/family-auth';

export async function POST() {
  const response = NextResponse.json({ ok: true });
  response.cookies.set(FAMILY_COOKIE, '', { httpOnly: true, maxAge: 0, path: '/' });
  return response;
}
