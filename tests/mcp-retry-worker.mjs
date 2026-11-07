// Fresh-process retry harness, usable only against a local synthetic test database.
import { registerHooks } from 'node:module';
const database = new URL(process.env.DATABASE_URL ?? 'http://invalid');
if (!['localhost', '127.0.0.1'].includes(database.hostname) || !database.pathname.includes('test')) process.exit(2);
registerHooks({
  resolve(specifier, context, nextResolve) {
    if (specifier === 'server-only') return { url: new URL('./server-only.ts', import.meta.url).href, shortCircuit: true };
    return nextResolve(specifier, context);
  },
});
const { executeTool } = await import('../src/mcp/tools.ts');
const { getPrisma } = await import('../src/lib/prisma.ts');
const result = await executeTool('create_ingredient', JSON.parse(process.env.SYNTHETIC_REQUEST), process.env.SYNTHETIC_MACHINE_ID);
process.stdout.write(JSON.stringify(result));
await getPrisma().$disconnect();
