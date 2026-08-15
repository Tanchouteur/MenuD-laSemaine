import type { NextRequest } from 'next/server';
import { NextResponse } from 'next/server';
import { FAMILY_COOKIE, familyAuthConfigured, familyToken } from '@/lib/family-auth';

export async function proxy(request: NextRequest) {
  if (!familyAuthConfigured()) return NextResponse.next();
  const pathname = request.nextUrl.pathname;
  if (pathname === '/connexion' || pathname.startsWith('/api/auth/') || pathname === '/api/calendar') return NextResponse.next();
  const expected = await familyToken(process.env.FAMILY_PASSWORD!, process.env.AUTH_SECRET!);
  if (request.cookies.get(FAMILY_COOKIE)?.value === expected) return NextResponse.next();
  if (pathname.startsWith('/api/')) return Response.json({ error: 'Connexion requise.' }, { status: 401 });
  const login = new URL('/connexion', request.url);
  login.searchParams.set('next', `${pathname}${request.nextUrl.search}`);
  return NextResponse.redirect(login);
}

export const config = { matcher: ['/((?!_next/static|_next/image|favicon.ico|site.webmanifest).*)'] };
