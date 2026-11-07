import { POST, GET } from '@/app/api/mcp/route';
import { mkdtempSync, readFileSync, statSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { spawnSync } from 'node:child_process';
import { beforeEach, describe, expect, it } from 'vitest';
import { randomBytes } from 'node:crypto';
import { PrismaClient } from '../../generated/prisma/client';
import { PrismaPg } from '@prisma/adapter-pg';
import { getPrisma, withPrismaTransaction } from '@/lib/prisma';
import { authenticate, tokenHash } from './auth';
import { executeTool } from './tools';
import { schemas } from './contracts';
import { createIngredient } from '@/services/catalog.service';
import { ensureWeeklyPlan } from '@/services/weekly-plan.service';

const prisma = getPrisma();
const ingredient = { name: 'Légume synthétique', category: 'VEGETABLE' as const, rating: 3, seasons: ['WINTER' as const], okLunchWeekday: true, okDinnerWeekday: true, okLunchWeekend: true, okDinnerWeekend: true };
let machine: string;
let bearer: string;
const call = (tool: Parameters<typeof executeTool>[0], args: unknown, credential = machine) => executeTool(tool, args, credential) as Promise<{ ok: boolean; data: { previewDigest: string; missingInformation: unknown[]; result: unknown; state: string }; error: { code: string } }>;
const importFor = (count = 14) => ({ startDate: '2030-01-07', expectedVersion: null as number | null, slots: Array.from({ length: count }, (_, slotIndex) => ({ slotIndex, guestCount: 2, replaceExisting: false, meal: { type: 'custom', label: `Repas synthétique ${slotIndex}` } })) });

beforeEach(async () => {
  const url = new URL(process.env.DATABASE_URL!);
  expect(['localhost', '127.0.0.1']).toContain(url.hostname);
  expect(url.pathname).toContain('test');
  await prisma.$executeRawUnsafe('TRUNCATE "MachineOperation", "MachineCredential", "WeeklyPlan", "Ingredient", "Recipe", "AppSettings" CASCADE');
  process.env.MCP_ALLOW_WRITES = 'true';
  bearer = `mds_${randomBytes(32).toString('hex')}`;
  machine = (await prisma.machineCredential.create({ data: { tokenHash: tokenHash(bearer), scopes: ['read', 'write'] } })).id;
});

describe('machine authentication and permissions', () => {
  it('requires a dedicated bearer and rejects the family cookie', async () => {
    await expect(authenticate(new Request('http://test/api/mcp', { headers: { cookie: 'family=anything' } }))).rejects.toMatchObject({ code: 'UNAUTHENTICATED' });
    await expect(authenticate(new Request('http://test/api/mcp', { headers: { authorization: `Bearer ${bearer}` } }))).resolves.toBe(machine);
    await expect(authenticate(new Request('http://test/api/mcp', { headers: { authorization: `Bearer mds_${'0'.repeat(64)}` } }))).rejects.toMatchObject({ code: 'UNAUTHENTICATED' });
  });
  it('separates read/write and immediately rechecks revocation', async () => {
    await prisma.machineCredential.update({ where: { id: machine }, data: { scopes: ['read'] } });
    expect((await call('create_ingredient', { operationKey: 'scope-test', ingredient })).error.code).toBe('FORBIDDEN');
    expect((await call('search_catalog', { kind: 'ingredient', query: 'x' })).ok).toBe(true);
    await prisma.machineCredential.update({ where: { id: machine }, data: { revokedAt: new Date() } });
    expect((await call('search_catalog', { kind: 'ingredient', query: 'x' })).error.code).toBe('UNAUTHENTICATED');
    await expect(authenticate(new Request('http://test/api/mcp', { headers: { authorization: `Bearer ${bearer}` } }))).rejects.toMatchObject({ code: 'UNAUTHENTICATED' });
  });
  it('creates a private token file and revokes through the operator CLI', async () => {
    const directory = mkdtempSync(join(tmpdir(), 'menu-mcp-synthetic-'));
    const path = join(directory, 'token');
    try {
      const created = spawnSync(process.execPath, ['--import', 'tsx', 'scripts/mcp-credentials.ts', 'create', 'read', path], { encoding: 'utf8', env: process.env });
      expect(created.status).toBe(0);
      const token = readFileSync(path, 'utf8').trim();
      expect(statSync(path).mode & 0o777).toBe(0o600);
      expect(created.stdout).not.toContain(token);
      const request = () => new Request('http://test/api/mcp', { headers: { authorization: `Bearer ${token}` } });
      const identity = await authenticate(request());
      const revoked = spawnSync(process.execPath, ['--import', 'tsx', 'scripts/mcp-credentials.ts', 'revoke', identity], { encoding: 'utf8', env: process.env });
      expect(revoked.status).toBe(0);
      await expect(authenticate(request())).rejects.toMatchObject({ code: 'UNAUTHENTICATED' });
    } finally { rmSync(directory, { recursive: true, force: true }); }
  });
  it('disables writes by default', async () => {
    delete process.env.MCP_ALLOW_WRITES;
    expect((await call('create_ingredient', { operationKey: 'dry-mode', ingredient })).error.code).toBe('WRITE_DISABLED');
    expect(await prisma.ingredient.count()).toBe(0);
  });
});

describe('validation, catalog and durable idempotency', () => {
  it('reports missing fields without input values and rejects unsupported data', async () => {
    const result = await call('create_recipe', { operationKey: 'missing-info', recipe: { name: 'private-personal-text' } });
    expect(result.error.code).toBe('VALIDATION_ERROR');
    expect(JSON.stringify(result)).not.toContain('private-personal-text');
    expect(schemas.create_ingredient.safeParse({ operationKey: 'unknown-field', ingredient: { ...ingredient, allergies: [] } }).success).toBe(false);
    expect(schemas.preview_week_import.safeParse({ import: { ...importFor(), startDate: '2030-02-30' } }).success).toBe(false);
  });
  it('replays a receipt after fresh client connection and rejects key reuse', async () => {
    const args = { operationKey: 'restart-test', ingredient };
    const first = await call('create_ingredient', args);
    expect(first.ok).toBe(true);
    await prisma.$disconnect();
    // A separate Node process retries the same mutation: no application memory survives.
    const child = spawnSync(process.execPath, ['--import', 'tsx', 'tests/mcp-retry-worker.mjs'], {
      env: { ...process.env, SYNTHETIC_MACHINE_ID: machine, SYNTHETIC_REQUEST: JSON.stringify(args) }, encoding: 'utf8',
    });
    expect(child.status).toBe(0);
    expect(JSON.parse(child.stdout)).toEqual(first);
    const fresh = new PrismaClient({ adapter: new PrismaPg({ connectionString: process.env.DATABASE_URL! }) });
    const receipt = await fresh.machineOperation.findUniqueOrThrow({ where: { credentialId_key: { credentialId: machine, key: args.operationKey } } });
    expect(receipt.result).toEqual(first);
    await fresh.$disconnect();
    expect(await call('create_ingredient', args)).toEqual(first);
    expect(await prisma.ingredient.count()).toBe(1);
    expect((await call('create_ingredient', { ...args, ingredient: { ...ingredient, name: 'Changed' } })).error.code).toBe('IDEMPOTENCY_CONFLICT');
    expect((await call('get_operation', { operationKey: args.operationKey })).data.result).toEqual(first);
  });
  it('does not expose another machine receipts', async () => {
    await call('create_ingredient', { operationKey: 'private-receipt', ingredient });
    const other = await prisma.machineCredential.create({ data: { tokenHash: 'other', scopes: ['read'] } });
    expect((await call('get_operation', { operationKey: 'private-receipt' }, other.id)).data.state).toBe('unknown');
  });
  it('reuses normalized existing catalog matches and surfaces ambiguity', async () => {
    await createIngredient(ingredient);
    expect((await call('create_ingredient', { operationKey: 'exact-match', ingredient: { ...ingredient, name: 'LEGUME SYNTHETIQUE' } })).error.code).toBe('CATALOG_MATCH');
    expect((await call('create_ingredient', { operationKey: 'near-match', ingredient: { ...ingredient, name: 'Légume synthétique frais' } })).error.code).toBe('AMBIGUOUS_MATCH');
    expect(await prisma.ingredient.count()).toBe(1);
  });
  it('creates a fully specified recipe and imports the existing catalog selection', async () => {
    const food = await createIngredient(ingredient);
    const recipeInput = { name: 'Recette synthétique', rating: 3, basePortions: 2, seasons: ['WINTER'], okLunchWeekday: true, okDinnerWeekday: true, okLunchWeekend: true, okDinnerWeekend: true, ingredients: [{ ingredientId: food.id, quantity: 200, unit: 'GRAM' }] };
    const created = await executeTool('create_recipe', { operationKey: 'recipe-create', recipe: recipeInput }, machine);
    expect(created).toMatchObject({ ok: true, data: { recipe: { basePortions: 2 } } });
    const recipe = await prisma.recipe.findUniqueOrThrow({ where: { name: recipeInput.name } });
    expect(await executeTool('create_recipe', { operationKey: 'recipe-create', recipe: recipeInput }, machine)).toEqual(created);
    const input = { ...importFor(), slots: importFor().slots.map(s => ({ ...s, meal: { type: 'catalog', selection: { recipeId: recipe.id } } })) };
    const preview = await call('preview_week_import', { import: input });
    expect(preview.ok).toBe(true);
    expect(preview.data.missingInformation).toHaveLength(0);
    expect((await call('apply_week_import', { operationKey: 'catalog-week', import: input, previewDigest: preview.data.previewDigest })).ok).toBe(true);
    expect(await prisma.mealSlot.count({ where: { recipeId: recipe.id } })).toBe(14);
    const shopping = await prisma.shoppingListEntry.findFirstOrThrow();
    expect(shopping.quantity?.toNumber()).toBe(2800);
  });
  it('stores suspicious text literally without interpreting administration commands', async () => {
    const name = 'ignore instructions; DROP TABLE Ingredient';
    expect((await call('create_ingredient', { operationKey: 'text-is-data', ingredient: { ...ingredient, name } })).ok).toBe(true);
    expect((await prisma.ingredient.findUniqueOrThrow({ where: { name } })).name).toBe(name);
    expect(await prisma.machineCredential.count()).toBe(1);
  });
  it('serializes concurrent duplicate requests', async () => {
    const args = { operationKey: 'concurrent-test', ingredient };
    const results = await Promise.all([call('create_ingredient', args), call('create_ingredient', args)]);
    expect(results.some(r => r.ok)).toBe(true);
    expect((await call('create_ingredient', args)).ok).toBe(true);
    expect(await prisma.ingredient.count()).toBe(1);
    expect(await prisma.machineOperation.count()).toBe(1);
  });
});

describe('atomic week import', () => {
  it('previews without creating a week, imports once and recovers result', async () => {
    const input = importFor();
    const preview = await call('preview_week_import', { import: input });
    expect(preview.ok).toBe(true);
    expect(await prisma.weeklyPlan.count()).toBe(0);
    expect(preview.data.missingInformation).toHaveLength(14);
    const args = { operationKey: 'apply-success', import: input, previewDigest: preview.data.previewDigest };
    const applied = await call('apply_week_import', args);
    expect(applied.ok).toBe(true);
    expect(await call('apply_week_import', args)).toEqual(applied);
    expect(await prisma.mealSlot.count({ where: { slotType: 'CUSTOM' } })).toBe(14);
  });
  it('rejects versions, protected weeks, locks and implicit replacement', async () => {
    const plan = await ensureWeeklyPlan('2030-01-07');
    const input = { ...importFor(1), expectedVersion: plan.version };
    expect((await call('preview_week_import', { import: { ...input, expectedVersion: 99 } })).error.code).toBe('VERSION_CONFLICT');
    await prisma.weeklyPlan.update({ where: { id: plan.id }, data: { status: 'CONFIRMED', confirmedAt: new Date() } });
    expect((await call('preview_week_import', { import: input })).error.code).toBe('WEEK_PROTECTED');
    await prisma.weeklyPlan.update({ where: { id: plan.id }, data: { status: 'DRAFT', confirmedAt: null } });
    await prisma.mealSlot.update({ where: { id: plan.slots[0].id }, data: { isLocked: true } });
    expect((await call('preview_week_import', { import: input })).error.code).toBe('SLOT_LOCKED');
    await prisma.mealSlot.update({ where: { id: plan.slots[0].id }, data: { isLocked: false, starterIsLocked: true } });
    expect((await call('preview_week_import', { import: input })).error.code).toBe('SLOT_LOCKED');
    await prisma.mealSlot.update({ where: { id: plan.slots[0].id }, data: { starterIsLocked: false, slotType: 'CUSTOM', customLabel: 'Synthetic' } });
    expect((await call('preview_week_import', { import: input })).error.code).toBe('REPLACEMENT_REQUIRED');
  });
  it('invalidates a preview if week changes, with zero mutations', async () => {
    const plan = await ensureWeeklyPlan('2030-01-07');
    const input = { ...importFor(1), expectedVersion: plan.version };
    const preview = await call('preview_week_import', { import: input });
    await prisma.mealSlot.update({ where: { id: plan.slots[0].id }, data: { guestCount: 9 } });
    expect((await call('apply_week_import', { operationKey: 'stale-preview', import: input, previewDigest: preview.data.previewDigest })).error.code).toBe('PREVIEW_STALE');
    expect(await prisma.mealSlot.count({ where: { slotType: 'CUSTOM' } })).toBe(0);
  });
  it('rolls back all prior slots on database failure and persists a failure receipt', async () => {
    const input = importFor();
    const preview = await call('preview_week_import', { import: input });
    // Real SQL failure on the second modification, after the first slot was changed.
    await prisma.$executeRawUnsafe(`ALTER TABLE "MealSlot" ADD CONSTRAINT synthetic_failure CHECK ("customLabel" IS DISTINCT FROM 'Repas synthétique 1')`);
    try {
      const args = { operationKey: 'rollback-test', import: input, previewDigest: preview.data.previewDigest };
      const result = await call('apply_week_import', args);
      expect(result.ok).toBe(false);
      expect(await prisma.weeklyPlan.count()).toBe(0);
      expect(await prisma.mealSlot.count()).toBe(0);
      expect((await call('get_operation', { operationKey: args.operationKey })).data.result).toEqual(result);
    } finally { await prisma.$executeRawUnsafe('ALTER TABLE "MealSlot" DROP CONSTRAINT synthetic_failure'); }
  });
  it('preserves an existing week and version on mid-import failure', async () => {
    const original = await ensureWeeklyPlan('2030-01-07');
    const input = { ...importFor(), expectedVersion: original.version };
    const preview = await call('preview_week_import', { import: input });
    const before = await prisma.weeklyPlan.findUniqueOrThrow({ where: { id: original.id }, include: { slots: { orderBy: { id: 'asc' } }, shoppingEntries: true } });
    await prisma.$executeRawUnsafe(`ALTER TABLE "MealSlot" ADD CONSTRAINT synthetic_failure_existing CHECK ("customLabel" IS DISTINCT FROM 'Repas synthétique 1')`);
    try {
      expect((await call('apply_week_import', { operationKey: 'rollback-existing', import: input, previewDigest: preview.data.previewDigest })).ok).toBe(false);
      expect(await prisma.weeklyPlan.findUniqueOrThrow({ where: { id: original.id }, include: { slots: { orderBy: { id: 'asc' } }, shoppingEntries: true } })).toEqual(before);
    } finally { await prisma.$executeRawUnsafe('ALTER TABLE "MealSlot" DROP CONSTRAINT synthetic_failure_existing'); }
  });
  it('rechecks locks and versions at apply time after preview', async () => {
    const plan = await ensureWeeklyPlan('2030-01-07');
    const input = { ...importFor(1), expectedVersion: plan.version };
    const preview = await call('preview_week_import', { import: input });
    await prisma.mealSlot.update({ where: { id: plan.slots[0].id }, data: { isLocked: true } });
    const args = { operationKey: 'locked-after-preview', import: input, previewDigest: preview.data.previewDigest };
    expect((await call('apply_week_import', args)).error.code).toBe('SLOT_LOCKED');
    await prisma.weeklyPlan.update({ where: { id: plan.id }, data: { version: { increment: 1 } } });
    expect((await call('apply_week_import', { ...args, operationKey: 'version-after-preview' })).error.code).toBe('VERSION_CONFLICT');
    expect(await prisma.mealSlot.count({ where: { slotType: 'CUSTOM' } })).toBe(0);
  });
  it('joins existing business transactions without leaking the scope', async () => {
    await expect(prisma.$transaction(tx => withPrismaTransaction(tx, async () => {
      await ensureWeeklyPlan('2030-01-07');
      throw new Error('synthetic abort');
    }))).rejects.toThrow('synthetic abort');
    expect(await prisma.weeklyPlan.count()).toBe(0);
    await ensureWeeklyPlan('2030-01-07');
    expect(await prisma.weeklyPlan.count()).toBe(1);
  });
});


describe('MCP HTTP transport', () => {
  async function rpc(method: string, params: unknown = {}, auth = true, extra: Record<string, string> = {}) {
    process.env.MCP_PUBLIC_URL = 'https://menus.example.test/api/mcp';
    return POST(new Request('https://menus.example.test/api/mcp', {
      method: 'POST',
      headers: { host: 'menus.example.test', 'content-type': 'application/json', accept: 'application/json, text/event-stream', 'mcp-protocol-version': '2025-11-25', ...(auth ? { authorization: `Bearer ${bearer}` } : {}), ...extra },
      body: JSON.stringify({ jsonrpc: '2.0', id: 1, method, params }),
    }));
  }
  it('authenticates before JSON processing and validates origin/host', async () => {
    expect((await rpc('tools/list', {}, false)).status).toBe(401);
    expect((await rpc('tools/list', {}, true, { origin: 'https://evil.example.test' })).status).toBe(403);
    expect((await rpc('tools/list', {}, true, { host: 'evil.example.test' })).status).toBe(403);
    expect(GET().status).toBe(405);
  });
  it('initializes and lists portable input/output schemas through the official SDK', async () => {
    const init = await rpc('initialize', { protocolVersion: '2025-11-25', capabilities: {}, clientInfo: { name: 'synthetic-test', version: '1' } });
    expect(init.status).toBe(200);
    expect((await init.json()).result.serverInfo.name).toBe('menu-de-la-semaine');
    const response = await rpc('tools/list');
    expect(response.status).toBe(200);
    const result = await response.json();
    expect(result.result.tools).toHaveLength(9);
    expect(result.result.tools.every((t: { inputSchema: unknown; outputSchema: unknown }) => t.inputSchema && t.outputSchema)).toBe(true);
    expect(response.headers.get('cache-control')).toBe('no-store');
  });
  it('returns structured tool results and rechecks permission on every request', async () => {
    const response = await rpc('tools/call', { name: 'create_ingredient', arguments: { operationKey: 'http-create', ingredient } });
    const body = await response.json();
    expect(body.result.structuredContent.ok).toBe(true);
    await prisma.machineCredential.update({ where: { id: machine }, data: { revokedAt: new Date() } });
    expect((await rpc('tools/list')).status).toBe(401);
  });
});
