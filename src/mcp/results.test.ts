import { describe, expect, it } from 'vitest';
import { serializeToolResult } from './results';

const ingredient = {
  id: 'synthetic-ingredient', name: 'Légume fictif', category: 'VEGETABLE', rating: 3,
  seasons: ['WINTER'], okLunchWeekday: true, okDinnerWeekday: true,
  okLunchWeekend: true, okDinnerWeekend: true, isActive: true,
  portionPerPerson: null, unit: null,
  createdAt: new Date('2030-01-01T00:00:00Z'), updatedAt: new Date('2030-01-02T00:00:00Z'),
};

describe('strict MCP wire results', () => {
  it('removes internal ORM fields that strict clients reject, while preserving nullable portions', () => {
    const result = serializeToolResult('search_catalog', { ok: true, data: { items: [ingredient], total: 1, ambiguous: false } });
    expect(result.ok).toBe(true);
    const item = (result.data as { items: Record<string, unknown>[] }).items[0];
    expect(item.id).toBe(ingredient.id);
    expect(item.portionPerPerson).toBeNull();
    expect(item).not.toHaveProperty('createdAt');
    expect(item).not.toHaveProperty('updatedAt');
  });
  it('normalizes catalog item and creation envelopes as well', () => {
    expect(serializeToolResult('get_catalog_item', { ok: true, data: { item: ingredient, dietaryInformation: 'unknown' } }).ok).toBe(true);
    expect(serializeToolResult('create_ingredient', { ok: true, data: { ingredient, missingInformation: [] } }).ok).toBe(true);
  });
  it('reports an invalid response without raw values and keeps business errors intact', () => {
    const invalid = serializeToolResult('get_week', { ok: true, data: { plan: { private: 'synthetic-private-value' } } });
    expect(invalid.ok).toBe(false);
    expect(JSON.stringify(invalid)).toContain('RESPONSE_CONTRACT_ERROR');
    expect(JSON.stringify(invalid)).not.toContain('synthetic-private-value');
    const error = { ok: false, error: { code: 'SLOT_LOCKED', retryable: false, details: { slotIndex: 1 } } };
    expect(serializeToolResult('preview_week_import', error)).toEqual(error);
  });
});
