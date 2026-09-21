import { afterEach, describe, expect, it } from 'vitest';
import { NextRequest } from 'next/server';
import { POST as login } from '@/app/api/auth/login/route';
import { POST as logout } from '@/app/api/auth/logout/route';
import { FAMILY_COOKIE, familyToken } from '@/lib/family-auth';
import { proxy } from '@/proxy';

const original = {
  password: process.env.FAMILY_PASSWORD,
  secret: process.env.AUTH_SECRET,
  nodeEnv: process.env.NODE_ENV,
};

function restore(name: string, value: string | undefined) {
  if (value === undefined) delete process.env[name];
  else process.env[name] = value;
}

afterEach(() => {
  restore('FAMILY_PASSWORD', original.password);
  restore('AUTH_SECRET', original.secret);
  restore('NODE_ENV', original.nodeEnv);
});

describe('routes d’authentification', () => {
  it('laisse entrer lorsque la protection familiale est désactivée', async () => {
    delete process.env.FAMILY_PASSWORD;
    delete process.env.AUTH_SECRET;
    const response = await login(new Request('http://test.local/api/auth/login', {
      method: 'POST', body: JSON.stringify({ password: 'n’importe quoi' }),
    }));
    expect(response.status).toBe(200);
  });

  it('rejette le mauvais mot de passe et pose un cookie sécurisé pour le bon', async () => {
    process.env.FAMILY_PASSWORD = 'famille';
    process.env.AUTH_SECRET = 'secret';
    const wrong = await login(new Request('http://test.local/api/auth/login', {
      method: 'POST', body: JSON.stringify({ password: 'incorrect' }),
    }));
    expect(wrong.status).toBe(401);

    const correct = await login(new Request('http://test.local/api/auth/login', {
      method: 'POST', body: JSON.stringify({ password: 'famille' }),
    }));
    expect(correct.status).toBe(200);
    expect(correct.headers.get('set-cookie')).toContain(`${FAMILY_COOKIE}=`);
    expect(correct.headers.get('set-cookie')).toContain('HttpOnly');
    expect(correct.headers.get('set-cookie')).toContain('SameSite=lax');
  });

  it('supprime explicitement le cookie lors de la déconnexion', async () => {
    const response = await logout();
    expect(response.headers.get('set-cookie')).toContain(`${FAMILY_COOKIE}=`);
    expect(response.headers.get('set-cookie')).toContain('Max-Age=0');
  });
});

describe('proxy de protection', () => {
  it('retourne 401 pour une API et redirige une page en conservant la destination', async () => {
    process.env.FAMILY_PASSWORD = 'famille';
    process.env.AUTH_SECRET = 'secret';
    const api = await proxy(new NextRequest('http://test.local/api/plans'));
    expect(api.status).toBe(401);
    const page = await proxy(new NextRequest('http://test.local/courses?semaine=2026-09-21'));
    expect(page.status).toBe(307);
    expect(page.headers.get('location')).toBe('http://test.local/connexion?next=%2Fcourses%3Fsemaine%3D2026-09-21');
  });

  it('laisse passer les endpoints publics et une session valide', async () => {
    process.env.FAMILY_PASSWORD = 'famille';
    process.env.AUTH_SECRET = 'secret';
    expect((await proxy(new NextRequest('http://test.local/api/health'))).status).toBe(200);
    expect((await proxy(new NextRequest('http://test.local/api/calendar'))).status).toBe(200);

    const token = await familyToken('famille', 'secret');
    const authenticated = new NextRequest('http://test.local/api/plans', {
      headers: { cookie: `${FAMILY_COOKIE}=${token}` },
    });
    expect((await proxy(authenticated)).status).toBe(200);
  });
});
