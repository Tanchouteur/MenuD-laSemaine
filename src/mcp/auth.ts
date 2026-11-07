import { createHash } from 'node:crypto';
import { getPrisma } from '@/lib/prisma';
import { McpFault } from './errors';
export const tokenHash = (token: string) => createHash('sha256').update(token).digest('hex');
export async function authenticate(request: Request) {
  const match = /^Bearer (mds_[a-f0-9]{64})$/.exec(request.headers.get('authorization') ?? '');
  if (!match) throw new McpFault('UNAUTHENTICATED');
  const credential = await getPrisma().machineCredential.findUnique({ where: { tokenHash: tokenHash(match[1]) } });
  if (!credential || credential.revokedAt) throw new McpFault('UNAUTHENTICATED');
  return credential.id;
}
export async function requireScope(credentialId: string, scope: 'read' | 'write') {
  const credential = await getPrisma().machineCredential.findUnique({ where: { id: credentialId } });
  if (!credential || credential.revokedAt) throw new McpFault('UNAUTHENTICATED');
  if (!credential.scopes.includes(scope)) throw new McpFault('FORBIDDEN', { requiredScope: scope });
}
