import { beforeEach, describe, expect, it } from 'vitest';
import { GET as healthGet } from '@/app/api/health/route';
import { POST as ingredientPost } from '@/app/api/ingredients/route';
import { GET as plansGet, POST as plansPost } from '@/app/api/plans/route';
import { getPrisma } from '@/lib/prisma';
import {
  applyCatalogEnrichment,
  previewCatalogEnrichment,
} from '@/services/catalog-import.service';
import {
  addIncompatibility,
  completeCompositionSetup,
  createIngredient,
  createRecipe,
  listIngredients,
} from '@/services/catalog.service';
import {
  generatePersistedWeek,
  restoreMealSlot,
  updateMealSlot,
} from '@/services/generation.service';
import {
  addManualShoppingEntry,
  deleteManualShoppingEntry,
  getShoppingList,
  rebuildShoppingList,
  toggleShoppingEntry,
} from '@/services/shopping-list.service';
import {
  confirmPlan,
  ensureWeeklyPlan,
  getPlanByStartDate,
  unconfirmPlan,
} from '@/services/weekly-plan.service';

const prisma = getPrisma();

function assertTestDatabase() {
  const databaseUrl = new URL(process.env.DATABASE_URL!);
  expect(['127.0.0.1', 'localhost']).toContain(databaseUrl.hostname);
  expect(databaseUrl.pathname.toLowerCase()).toContain('test');
}

async function resetDatabase() {
  assertTestDatabase();
  await prisma.$executeRawUnsafe(`
    TRUNCATE TABLE
      "ShoppingListEntry", "Incompatibility", "RecipeIngredient", "MealSlot",
      "Recipe", "Ingredient", "Aisle", "WeeklyPlan", "AppSettings"
    RESTART IDENTITY CASCADE
  `);
}

const allMoments = {
  okLunchWeekday: true,
  okDinnerWeekday: true,
  okLunchWeekend: true,
  okDinnerWeekend: true,
};

async function ingredient(name: string, category: 'PROTEIN' | 'STARCH' | 'VEGETABLE' = 'VEGETABLE') {
  return createIngredient({
    name,
    category,
    rating: 3,
    useInComposedMeals: true,
    portionPerPerson: 100,
    unit: 'GRAM',
    seasons: ['WINTER', 'SPRING', 'SUMMER', 'AUTUMN'],
    ...allMoments,
  });
}

beforeEach(resetDatabase);

describe('frontières HTTP sur la vraie base', () => {
  it('expose un healthcheck PostgreSQL et les plans en JSON', async () => {
    const health = await healthGet();
    expect(health.status).toBe(200);
    expect(await health.json()).toEqual({ status: 'ok' });
    const empty = await plansGet();
    expect(await empty.json()).toEqual([]);
  });

  it('retourne 400 pour une requête invalide et 201 pour une création valide', async () => {
    const missingDate = await plansPost(new Request('http://test.local/api/plans', {
      method: 'POST', body: '{}',
    }));
    expect(missingDate.status).toBe(400);
    expect(await missingDate.json()).toEqual({ error: 'La date de la semaine est obligatoire.' });
    const created = await plansPost(new Request('http://test.local/api/plans', {
      method: 'POST', body: JSON.stringify({ startDate: '2026-09-21' }),
    }));
    expect(created.status).toBe(201);
    expect((await created.json()).slots).toHaveLength(14);

    const invalidIngredient = await ingredientPost(new Request('http://test.local/api/ingredients', {
      method: 'POST', body: JSON.stringify({ name: '' }),
    }));
    expect(invalidIngredient.status).toBe(400);
  });
});

describe('plans hebdomadaires sur PostgreSQL 17', () => {
  it('crée une semaine idempotente de quatorze créneaux avec les réglages réels', async () => {
    await prisma.appSettings.create({
      data: {
        id: 'default',
        defaultGuestsLunchWeekday: 2,
        defaultGuestsDinnerWeekday: 3,
        defaultGuestsLunchWeekend: 4,
        defaultGuestsDinnerWeekend: 5,
      },
    });
    const first = await ensureWeeklyPlan('2026-09-21');
    const second = await ensureWeeklyPlan('2026-09-21');
    expect(second.id).toBe(first.id);
    expect(first.slots).toHaveLength(14);
    expect(first.slots.map((slot) => slot.guestCount)).toEqual([
      2, 3, 2, 3, 2, 3, 2, 3, 2, 3, 4, 5, 4, 5,
    ]);
    await expect(ensureWeeklyPlan('2026-09-22')).rejects.toThrow('lundi');
    expect(await prisma.weeklyPlan.count()).toBe(1);
  });

  it('refuse une confirmation incomplète, protège les versions et permet une réouverture', async () => {
    const plan = await ensureWeeklyPlan('2026-09-21');
    await expect(confirmPlan(plan.id, plan.version)).rejects.toThrow('Chaque créneau');
    await prisma.mealSlot.updateMany({
      where: { weeklyPlanId: plan.id },
      data: { slotType: 'CUSTOM', customLabel: 'Repas familial' },
    });
    await expect(confirmPlan(plan.id, plan.version - 1)).rejects.toThrow('autre appareil');
    const confirmed = await confirmPlan(plan.id, plan.version);
    expect(confirmed.status).toBe('confirmed');
    expect(confirmed.confirmedAt).not.toBeNull();
    await expect(updateMealSlot(confirmed.slots[0].id, { guestCount: 6 }, confirmed.version))
      .rejects.toThrow('déjà confirmée');
    const reopened = await unconfirmPlan(plan.id, confirmed.version);
    expect(reopened.status).toBe('draft');
    expect(reopened.confirmedAt).toBeNull();
  });

  it('applique une modification atomique et rejette une version périmée sans effet partiel', async () => {
    const plan = await ensureWeeklyPlan('2026-09-21');
    const slot = plan.slots[0];
    await expect(updateMealSlot(slot.id, { slotType: 'custom', customLabel: '   ' }, plan.version))
      .rejects.toThrow('Donnez un nom');
    const updated = await updateMealSlot(
      slot.id,
      { slotType: 'custom', customLabel: '  Brunch  ', guestCount: 6, isLocked: true },
      plan.version,
    );
    expect(updated.slots[0]).toMatchObject({
      slotType: 'custom', customLabel: 'Brunch', guestCount: 6, isLocked: true,
    });
    await expect(updateMealSlot(slot.id, { guestCount: 9 }, plan.version)).rejects.toThrow('autre appareil');
    expect((await getPlanByStartDate('2026-09-21'))?.slots[0].guestCount).toBe(6);
  });

  it('n’autorise qu’une seule écriture lorsque deux appareils partent de la même version', async () => {
    const plan = await ensureWeeklyPlan('2026-09-21');
    const slot = plan.slots[0];
    const results = await Promise.allSettled([
      updateMealSlot(slot.id, { guestCount: 5 }, plan.version),
      updateMealSlot(slot.id, { guestCount: 8 }, plan.version),
    ]);
    expect(results.filter((result) => result.status === 'fulfilled')).toHaveLength(1);
    expect(results.filter((result) => result.status === 'rejected')).toHaveLength(1);
    expect([5, 8]).toContain((await getPlanByStartDate('2026-09-21'))?.slots[0].guestCount);
    expect((await getPlanByStartDate('2026-09-21'))?.version).toBe(plan.version + 1);
  });

  it('n’autorise qu’une seule confirmation concurrente pour une version donnée', async () => {
    const plan = await ensureWeeklyPlan('2026-09-21');
    await prisma.mealSlot.updateMany({
      where: { weeklyPlanId: plan.id },
      data: { slotType: 'CUSTOM', customLabel: 'Repas familial' },
    });
    const results = await Promise.allSettled([
      confirmPlan(plan.id, plan.version),
      confirmPlan(plan.id, plan.version),
    ]);
    expect(results.filter((result) => result.status === 'fulfilled')).toHaveLength(1);
    expect(results.filter((result) => result.status === 'rejected')).toHaveLength(1);
    expect((await getPlanByStartDate('2026-09-21'))?.version).toBe(plan.version + 1);
  });
});

describe('catalogue, contraintes et import transactionnel', () => {
  it('valide les entités, normalise les incompatibilités et sécurise la sélection automatique', async () => {
    const carrot = await ingredient('Carotte');
    const rice = await ingredient('Riz', 'STARCH');
    await expect(ingredient('   ')).rejects.toThrow('nom');
    const incompatibility = await addIncompatibility(rice.id, carrot.id);
    const same = await addIncompatibility(carrot.id, rice.id);
    expect(same.id).toBe(incompatibility.id);
    await expect(addIncompatibility(carrot.id, carrot.id)).rejects.toThrow('différents');

    await completeCompositionSetup([carrot.id, carrot.id, rice.id]);
    expect((await listIngredients()).filter((item) => item.useInComposedMeals)).toHaveLength(2);
    const archived = await ingredient('Archivé');
    await prisma.ingredient.update({ where: { id: archived.id }, data: { isActive: false } });
    await expect(completeCompositionSetup([archived.id])).rejects.toThrow('indisponible');
  });

  it('crée une recette réelle et refuse les ingrédients dupliqués', async () => {
    const tomato = await ingredient('Tomate');
    const recipe = await createRecipe({
      name: '  Tomates rôties  ', rating: 4, basePortions: 2,
      seasons: ['SUMMER'], ingredients: [{ ingredientId: tomato.id, quantity: 300, unit: 'GRAM' }],
      ...allMoments,
    });
    expect(recipe.name).toBe('Tomates rôties');
    expect(recipe.ingredients[0]).toMatchObject({ ingredientName: 'Tomate', quantity: 300 });
    await expect(createRecipe({
      name: 'Doublon', rating: 3, basePortions: 2, seasons: ['SUMMER'],
      ingredients: [
        { ingredientId: tomato.id, quantity: 100, unit: 'GRAM' },
        { ingredientId: tomato.id, quantity: 200, unit: 'GRAM' },
      ],
      ...allMoments,
    })).rejects.toThrow('qu’une fois');
  });

  it('prévisualise puis applique un enrichissement sans écriture en cas de référence invalide', async () => {
    await prisma.aisle.create({ data: { name: 'Légumes', sortOrder: 1 } });
    const invalid = {
      format: 'menu-de-la-semaine-enrichment', version: 1,
      ingredients: [],
      recipes: [{
        name: 'Soupe fantôme', basePortions: 4,
        ingredients: [{ name: 'Absent', quantity: 1, unit: 'PIECE' }],
      }],
    };
    await expect(previewCatalogEnrichment(invalid)).rejects.toThrow('Ingrédients absents');
    expect(await prisma.recipe.count()).toBe(0);

    const document = {
      format: 'menu-de-la-semaine-enrichment', version: 1,
      ingredients: [{
        name: 'Courgette', category: 'VEGETABLE', useInComposedMeals: true,
        portionPerPerson: 150, unit: 'GRAM', aisle: 'Légumes',
      }],
      recipes: [{
        name: 'Courgettes poêlées', basePortions: 2,
        ingredients: [{ name: 'Courgette', quantity: 300, unit: 'GRAM' }],
      }],
    } as const;
    const preview = await previewCatalogEnrichment(document);
    expect(preview.summary.ingredientsToCreate).toEqual(['Courgette']);
    await applyCatalogEnrichment(document);
    expect(await prisma.ingredient.count()).toBe(1);
    expect(await prisma.recipeIngredient.count()).toBe(1);
    const second = await applyCatalogEnrichment(document);
    expect(second.ingredientsToUpdate).toEqual(['Courgette']);
    expect(await prisma.ingredient.count()).toBe(1);
    expect(await prisma.recipe.count()).toBe(1);
  });
});

describe('génération, instantanés et liste de courses', () => {
  it('génère et persiste une semaine complète puis reconstruit les courses', async () => {
    const tomato = await ingredient('Tomate');
    for (let index = 0; index < 20; index += 1) {
      await createRecipe({
        name: `Recette ${index}`, rating: (index % 5) + 1, basePortions: 2,
        seasons: ['WINTER', 'SPRING', 'SUMMER', 'AUTUMN'],
        ingredients: [{ ingredientId: tomato.id, quantity: 200 + index, unit: 'GRAM' }],
        ...allMoments,
      });
    }
    const initial = await ensureWeeklyPlan('2026-09-21');
    const generated = await generatePersistedWeek('2026-09-21', 'integration-stable', initial.version);
    expect(generated.version).toBe(initial.version + 1);
    expect(generated.slots.every((slot) => slot.assignment !== null)).toBe(true);
    expect(new Set(generated.slots.map((slot) => slot.assignment?.signature)).size).toBe(14);
    const entries = await getShoppingList(generated.id);
    expect(entries).toHaveLength(1);
    expect(entries[0].label).toBe('Tomate');
    expect(entries[0].quantity).toBeGreaterThan(0);
    await expect(generatePersistedWeek('2026-09-21', 'stale', initial.version))
      .rejects.toThrow('autre appareil');
  });

  it('agrège les portions, conserve les coches et protège les ajouts manuels', async () => {
    const plan = await ensureWeeklyPlan('2026-09-21');
    const [first, second] = plan.slots;
    const tomato = await ingredient('Tomate');
    const recipe = await createRecipe({
      name: 'Tomates test', rating: 3, basePortions: 1,
      seasons: ['WINTER', 'SPRING', 'SUMMER', 'AUTUMN'],
      ingredients: [{ ingredientId: tomato.id, quantity: 100, unit: 'GRAM' }],
      ...allMoments,
    });
    const mealSnapshot = {
      version: 1, kind: 'recipe', signature: `recipe:${recipe.id}`, name: 'Tomates',
      items: [{
        ingredientId: tomato.id, name: 'Tomate', quantityPerPerson: 100,
        unit: 'GRAM', aisleId: null, aisleName: null,
      }],
    };
    await prisma.mealSlot.update({ where: { id: first.id }, data: {
      slotType: 'RECIPE', guestCount: 2, recipeId: recipe.id,
      mealSignature: `recipe:${recipe.id}:first`, mealSnapshot,
    } });
    await prisma.mealSlot.update({ where: { id: second.id }, data: {
      slotType: 'RECIPE', guestCount: 3, recipeId: recipe.id,
      mealSignature: `recipe:${recipe.id}:second`, mealSnapshot,
    } });
    await rebuildShoppingList(plan.id);
    let entries = await getShoppingList(plan.id);
    expect(entries).toHaveLength(1);
    expect(entries[0]).toMatchObject({ quantity: 500, unit: 'GRAM', sourceCount: 2, isChecked: false });
    await toggleShoppingEntry(entries[0].id);
    await addManualShoppingEntry(plan.id, '  Papier cuisson  ');
    await prisma.mealSlot.update({ where: { id: second.id }, data: { guestCount: 4 } });
    await rebuildShoppingList(plan.id);
    entries = await getShoppingList(plan.id);
    expect(entries.find((entry) => !entry.isManual)).toMatchObject({ quantity: 600, isChecked: true });
    const manual = entries.find((entry) => entry.isManual)!;
    expect(manual.label).toBe('Papier cuisson');
    await expect(deleteManualShoppingEntry(entries.find((entry) => !entry.isManual)!.id))
      .rejects.toThrow('ajouts manuels');
    await deleteManualShoppingEntry(manual.id);
    expect((await getShoppingList(plan.id)).filter((entry) => entry.isManual)).toHaveLength(0);
  });

  it('restaure exactement un instantané valide et refuse un état incohérent', async () => {
    const plan = await ensureWeeklyPlan('2026-09-21');
    const slot = plan.slots[0];
    await expect(restoreMealSlot(slot.id, {
      ...slot.restoreState,
      slotType: 'recipe',
      mealSnapshot: null,
    }, plan.version)).rejects.toThrow('instantané');

    const restored = await restoreMealSlot(slot.id, {
      slotType: 'custom', customLabel: 'Souvenir', leftoversFromSlotId: null,
      mealSignature: null, mealSnapshot: null, recipeId: null, proteinId: null,
      starchId: null, vegetableId: null, isLocked: true, guestCount: 7,
    }, plan.version);
    expect(restored.slots[0]).toMatchObject({
      slotType: 'custom', customLabel: 'Souvenir', isLocked: true, guestCount: 7,
    });
  });
});
