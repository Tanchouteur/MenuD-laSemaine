export const FAMILY_COOKIE = 'menu_family_session';

export async function familyToken(password: string, secret: string): Promise<string> {
  const bytes = new TextEncoder().encode(`menu-family-v1:${password}:${secret}`);
  const digest = await crypto.subtle.digest('SHA-256', bytes);
  return Array.from(new Uint8Array(digest), (byte) => byte.toString(16).padStart(2, '0')).join('');
}

export function familyAuthConfigured(): boolean {
  return Boolean(process.env.FAMILY_PASSWORD && process.env.AUTH_SECRET);
}
