import { Prisma } from '../../generated/prisma/client';
import { getPrisma, withPrismaTransaction } from '@/lib/prisma';
import { createIngredient, createRecipe, listIngredients, listRecipes } from '@/services/catalog.service';
import { getPlanByStartDate } from '@/services/weekly-plan.service';
import { applyWeekImport, digest, listWeeks, previewWeekImport } from '@/services/week-import.service';
import { requireScope } from './auth';
import { failure, isConcurrentError, McpFault } from './errors';
import { schemas, writeTools, type ToolName } from './contracts';

const normalize = (s: string) => s.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('fr').replace(/[^\p{L}\p{N}]+/gu, ' ').trim();
async function catalog(kind: 'ingredient' | 'recipe') { return kind === 'ingredient' ? listIngredients(true) : listRecipes(true); }
async function checkMatches(kind: 'ingredient' | 'recipe', name: string, acknowledged: string[]) {
  const items = await catalog(kind);
  const normalized = normalize(name);
  const exact = items.filter(i => normalize(i.name) === normalized);
  if (exact.length) throw new McpFault('CATALOG_MATCH', { candidates: exact });
  const similar = items.filter(i => normalize(i.name).includes(normalized) || normalized.includes(normalize(i.name)));
  if (similar.some(i => !acknowledged.includes(i.id))) throw new McpFault('AMBIGUOUS_MATCH', { candidates: similar });
}
async function dispatch(tool: ToolName, raw: unknown, credentialId: string): Promise<unknown> {
  // Parse again here to retain the tool-specific static types at the business boundary.
  switch (tool) {
    case 'search_catalog': {
      const a = schemas[tool].parse(raw);
      const all = (await catalog(a.kind)).filter(i => normalize(i.name).includes(normalize(a.query)));
      return { items: all.slice(a.offset, a.offset + a.limit), total: all.length, ambiguous: all.length > 1 };
    }
    case 'get_catalog_item': {
      const a = schemas[tool].parse(raw);
      const item = (await catalog(a.kind)).find(i => i.id === a.id);
      if (!item) throw new McpFault('NOT_FOUND');
      return { item, dietaryInformation: 'Allergies and nutrition unknown: not stored.' };
    }
    case 'list_weeks': { const a = schemas[tool].parse(raw); return { items: await listWeeks(a.from, a.to, a.offset, a.limit) }; }
    case 'get_week': { const a = schemas[tool].parse(raw); return { plan: await getPlanByStartDate(a.startDate) }; }
    case 'preview_week_import': return previewWeekImport(schemas[tool].parse(raw).import);
    case 'apply_week_import': { const a = schemas[tool].parse(raw); return applyWeekImport(a.import, a.previewDigest); }
    case 'create_ingredient': {
      const a = schemas[tool].parse(raw);
      await checkMatches('ingredient', a.ingredient.name, a.acknowledgedMatchIds);
      return { ingredient: await createIngredient(a.ingredient), missingInformation: ['allergies/nutrition: not stored', ...(a.ingredient.portionPerPerson == null ? ['portionPerPerson'] : []), ...(a.ingredient.unit == null ? ['unit'] : [])] };
    }
    case 'create_recipe': {
      const a = schemas[tool].parse(raw);
      await checkMatches('recipe', a.recipe.name, a.acknowledgedMatchIds);
      const ids = a.recipe.ingredients.map(i => i.ingredientId);
      if (await getPrisma().ingredient.count({ where: { id: { in: ids }, isActive: true } }) !== ids.length) throw new McpFault('INVALID_REFERENCE', { fields: ['ingredients'] });
      return { recipe: await createRecipe(a.recipe), missingInformation: ['allergies/nutrition: not stored'] };
    }
    case 'get_operation': {
      const a = schemas[tool].parse(raw);
      const receipt = await getPrisma().machineOperation.findUnique({ where: { credentialId_key: { credentialId, key: a.operationKey } } });
      return receipt ? { state: 'completed', tool: receipt.tool, result: receipt.result } : { state: 'unknown', guidance: 'No committed receipt. Retry the exact request with the same operationKey.' };
    }
  }
}

export async function executeTool(tool: ToolName, raw: unknown, credentialId: string) {
  try {
    await requireScope(credentialId, writeTools.has(tool) ? 'write' : 'read');
    const args = schemas[tool].parse(raw);
    if (!writeTools.has(tool)) return { ok: true, data: await dispatch(tool, args, credentialId) };
    if (process.env.MCP_ALLOW_WRITES !== 'true') throw new McpFault('WRITE_DISABLED');
    const key = (args as { operationKey: string }).operationKey;
    const requestHash = digest({ tool, args });
    return await getPrisma().$transaction(transaction => withPrismaTransaction(transaction, async () => {
      // Serializes revocation against a mutation; revoke waits for already-running work.
      await transaction.$queryRaw`SELECT id FROM "MachineCredential" WHERE id = ${credentialId} FOR SHARE`;
      await requireScope(credentialId, 'write');
      const existing = await transaction.machineOperation.findUnique({ where: { credentialId_key: { credentialId, key } } });
      if (existing) {
        if (existing.requestHash !== requestHash) throw new McpFault('IDEMPOTENCY_CONFLICT');
        return existing.result;
      }
      await transaction.$executeRawUnsafe('SAVEPOINT machine_work');
      let result;
      try {
        result = { ok: true, data: await dispatch(tool, args, credentialId) };
      } catch (error) {
        // Serialization failures must abort the transaction and can be safely retried.
        if (isConcurrentError(error)) throw error;
        await transaction.$executeRawUnsafe('ROLLBACK TO SAVEPOINT machine_work');
        result = failure(error);
      }
      const json = JSON.parse(JSON.stringify(result)) as Prisma.InputJsonValue;
      await transaction.machineOperation.create({ data: { credentialId, key, tool, requestHash, result: json } });
      return json;
    }), { isolationLevel: Prisma.TransactionIsolationLevel.Serializable, timeout: 30_000 });
  } catch (error) { return failure(error); }
}
