/** Local operator only. No credential management tools are exposed over MCP. */
import { randomBytes, createHash } from 'node:crypto';
import { writeFileSync } from 'node:fs';
import { PrismaClient } from '../generated/prisma/client';
import { PrismaPg } from '@prisma/adapter-pg';

const connectionString = process.env.DATABASE_URL;
if (!connectionString) { process.stderr.write('DATABASE_URL is required.\n'); process.exit(1); }
const prisma = new PrismaClient({ adapter: new PrismaPg({ connectionString }) });
const tokenHash = (token: string) => createHash('sha256').update(token).digest('hex');

async function main() {
  const [action, argument, output] = process.argv.slice(2);
  if (action === 'create') {
    if (!['read', 'write', 'read,write'].includes(argument) || !output?.startsWith('/')) throw new Error('Usage: create read|write|read,write /absolute/private/token-file');
    const token = `mds_${randomBytes(32).toString('hex')}`;
    // Refuse overwrites; the raw token is written once to a private file, never stdout.
    writeFileSync(output, token + '\n', { flag: 'wx', mode: 0o600 });
    const credential = await prisma.machineCredential.create({ data: { tokenHash: tokenHash(token), scopes: argument.split(',') } });
    process.stdout.write(`Credential ID: ${credential.id}\nToken saved to the requested private file.\n`);
  } else if (action === 'revoke' && argument) {
    await prisma.machineCredential.update({ where: { id: argument }, data: { revokedAt: new Date() } });
    process.stdout.write('Credential revoked.\n');
  } else throw new Error('Usage: create <scopes> <private-file> | revoke <credential-id>');
}
main().catch(() => { process.stderr.write('Credential command failed. Check arguments, private path and database access.\n'); process.exitCode = 1; }).finally(async () => { await prisma.$disconnect(); });
