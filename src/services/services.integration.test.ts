import { readFileSync } from 'node:fs';
import { beforeEach, describe, expect, it } from 'vitest';
import { GET as healthGet } from '@/app/api/health/route';
import { POST as ingredientPost } from '@/app/api/ingredients/route';
import { GET as plansGet, POST as plansPost } from '@/app/api/plans/route';
import { GET as shoppingGet, POST as shoppingPost } from '@/app/api/shopping/route';
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
  chooseManualForSlot,
  generatePersistedWeek,
  restoreMealSlot,
  updateMealSlot,
} from '@/services/generation.service';
import { setStarterForSlot } from '@/services/starter.service';
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
  reapplyPlan,
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

  it('réserve les courses aux semaines confirmées, y compris après réouverture', async () => {
    const plan = await ensureWeeklyPlan('2026-09-21');
    const url = `http://test.local/api/shopping?planId=${plan.id}`;
    expect((await shoppingGet(new Request(url))).status).toBe(400);
    expect((await shoppingPost(new Request('http://test.local/api/shopping', {
      method: 'POST', body: JSON.stringify({ planId: plan.id, label: 'Pain' }),
    }))).status).toBe(400);
    await prisma.mealSlot.updateMany({ where: { weeklyPlanId: plan.id }, data: { slotType: 'CUSTOM', customLabel: 'Repas prévu' } });
    const confirmed = await confirmPlan(plan.id, plan.version);
    expect((await shoppingPost(new Request('http://test.local/api/shopping', {
      method: 'POST', body: JSON.stringify({ planId: plan.id, label: 'Pain' }),
    }))).status).toBe(201);
    const confirmedEntries = await (await shoppingGet(new Request(url))).json();
    expect(confirmedEntries).toEqual([expect.objectContaining({ label: 'Pain' })]);
    await unconfirmPlan(plan.id, confirmed.version);
    expect((await shoppingGet(new Request(url))).status).toBe(400);
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
    const corrected = await updateMealSlot(confirmed.slots[0].id, { guestCount: 6 }, confirmed.version);
    expect(corrected.slots[0].guestCount).toBe(6);
    const reopened = await unconfirmPlan(plan.id, corrected.version);
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

  it('prévisualise une conversion incomplète sans désactiver les anciens ingrédients', async () => {
    const old = await ingredient('Poulet préparé', 'PROTEIN');
    const document = { format: 'menu-de-la-semaine-enrichment', version: 2,
      ingredients: [{ name: old.name, category: 'PROTEIN', useInComposedMeals: false,
        isActive: false, portionPerPerson: null, unit: null, aisle: null }],
      recipes: [{ name: 'Poulet préparé', role: 'MAIN', basePortions: 4,
        ingredients: [{ name: old.name, quantity: null, unit: 'GRAM' }] }],
    };
    const preview = await previewCatalogEnrichment(document);
    expect(preview.summary.pendingQuantities).toEqual(['Poulet préparé : Poulet préparé']);
    expect(preview.summary.ingredientsToArchive).toEqual(['Poulet préparé']);
    await expect(applyCatalogEnrichment(document)).rejects.toThrow('Quantités à vérifier');
    expect((await prisma.ingredient.findUniqueOrThrow({ where: { id: old.id } })).isActive).toBe(true);
  });

  it('demande les portions des produits simples utilisés dans les assiettes', async () => {
    const document = { format: 'menu-de-la-semaine-enrichment', version: 2,
      ingredients: [{ name: 'Tenders Lidl', category: 'PROTEIN', useInComposedMeals: true,
        portionPerPerson: null, unit: null, aisle: null }], recipes: [] };
    const preview = await previewCatalogEnrichment(document);
    expect(preview.summary.pendingPortions).toEqual(['Tenders Lidl']);
    await expect(applyCatalogEnrichment(document)).rejects.toThrow('Quantités à vérifier');
  });

  it('applique deux fois le fichier du catalogue du 25 septembre sans doublons', async () => {
    const document = JSON.parse(readFileSync('Catalogue/enrichissement-repas-2026-09-25.json', 'utf8'));
    const existingNames = ['Tenders Lidl', 'Semoule', 'Brocolis', 'Omelette au jambon',
      'Poulet à la crème moutarde', 'Pdt. Sauté', 'Œuf', 'Jambon', 'Poulet', 'Crème',
      'Pommes de terre', 'Huile d’olive', 'Tomate', 'Carotte'];
    const byName = new Map<string, Awaited<ReturnType<typeof ingredient>>>();
    for (const name of existingNames) byName.set(name, await ingredient(name));
    await prisma.aisle.createMany({ data: ['Épicerie', 'Fruits et légumes'].map((name, sortOrder) => ({ name, sortOrder })) });
    await createRecipe({ name: 'Poulet curry coco et riz', rating: 3, basePortions: 4,
      seasons: ['WINTER', 'SPRING', 'SUMMER', 'AUTUMN'], ...allMoments,
      ingredients: [{ ingredientId: byName.get('Poulet')!.id, quantity: 600, unit: 'GRAM' }] });
    const preview = await previewCatalogEnrichment(document);
    expect(preview.summary.pendingQuantities).toEqual([]);
    expect(preview.summary.pendingPortions).toEqual([]);
    expect(preview.summary.recipesToCreate).toHaveLength(4);
    await applyCatalogEnrichment(document);
    await applyCatalogEnrichment(document);
    expect(await prisma.recipe.count()).toBe(5);
    const curry = await prisma.recipe.findUniqueOrThrow({ where: { name: 'Poulet curry coco et riz' } });
    const mustard = await prisma.recipe.findUniqueOrThrow({ where: { name: 'Poulet à la crème moutarde' } });
    expect(mustard.variantOfId).toBe(curry.id);
    expect(mustard.allowStarchSide).toBe(true);
    expect((await prisma.recipe.findUniqueOrThrow({ where: { name: 'Pommes de terre sautées' } })).role).toBe('SIDE_STARCH');
    expect((await prisma.recipe.findUniqueOrThrow({ where: { name: 'Tomates et carottes râpées' } })).role).toBe('STARTER');
    for (const name of ['Omelette au jambon', 'Poulet à la crème moutarde', 'Pdt. Sauté']) {
      expect((await prisma.ingredient.findUniqueOrThrow({ where: { name } })).isActive).toBe(false);
    }
    expect((await prisma.ingredient.findUniqueOrThrow({ where: { name: 'Semoule' } })).portionPerPerson?.toNumber()).toBe(80);
  });
});

describe('génération, instantanés et liste de courses', () => {
  it('rejoue les quatre repas du week-end avec leurs vraies courses', async () => {
    const make = async (name: string, category: 'PROTEIN' | 'STARCH' | 'VEGETABLE') => ingredient(name, category);
    const [egg, ham, potato, oil, tournedos, pasta, tenders, rice, broccoli,
      chicken, cream, mustard, semolina, beans, asparagus, palm, tomato, carrot] = await Promise.all([
      make('Œuf', 'PROTEIN'), make('Jambon', 'PROTEIN'), make('Pommes de terre', 'STARCH'),
      make('Huile', 'VEGETABLE'), make('Tournedos', 'PROTEIN'), make('Pâtes', 'STARCH'),
      make('Tenders Lidl', 'PROTEIN'), make('Riz', 'STARCH'), make('Brocolis', 'VEGETABLE'),
      make('Poulet', 'PROTEIN'), make('Crème', 'VEGETABLE'), make('Moutarde', 'VEGETABLE'),
      make('Semoule', 'STARCH'), make('Haricots verts', 'VEGETABLE'), make('Asperges', 'VEGETABLE'),
      make('Cœurs de palmiers', 'VEGETABLE'), make('Tomate', 'VEGETABLE'), make('Carotte', 'VEGETABLE'),
    ]);
    const common = { rating: 3, basePortions: 4,
      seasons: ['WINTER', 'SPRING', 'SUMMER', 'AUTUMN'] as ('WINTER' | 'SPRING' | 'SUMMER' | 'AUTUMN')[], ...allMoments };
    const omelette = await createRecipe({ ...common, name: 'Omelette et jambon grillé', allowStarchSide: true,
      ingredients: [{ ingredientId: egg.id, quantity: 8, unit: 'PIECE' }, { ingredientId: ham.id, quantity: 240, unit: 'GRAM' }] });
    const sauteed = await createRecipe({ ...common, name: 'Pommes de terre sautées', role: 'SIDE_STARCH',
      ingredients: [{ ingredientId: potato.id, quantity: 1000, unit: 'GRAM' }, { ingredientId: oil.id, quantity: 40, unit: 'GRAM' }] });
    const mustardChicken = await createRecipe({ ...common, name: 'Poulet à la crème moutarde', allowStarchSide: true, allowVegetableSide: true,
      ingredients: [{ ingredientId: chicken.id, quantity: 600, unit: 'GRAM' }, { ingredientId: cream.id, quantity: 200, unit: 'GRAM' }, { ingredientId: mustard.id, quantity: 40, unit: 'GRAM' }] });
    const tomatoCarrot = await createRecipe({ ...common, name: 'Tomates et carottes râpées', role: 'STARTER',
      ingredients: [{ ingredientId: tomato.id, quantity: 300, unit: 'GRAM' }, { ingredientId: carrot.id, quantity: 300, unit: 'GRAM' }] });
    const plan = await ensureWeeklyPlan('2026-09-21');
    let version = plan.version;
    const choices = [
      { recipeId: omelette.id, starchRecipeId: sauteed.id },
      { proteinId: tournedos.id, starchId: pasta.id },
      { proteinId: tenders.id, starchId: rice.id, vegetableId: broccoli.id },
      { recipeId: mustardChicken.id, starchId: semolina.id, vegetableId: beans.id },
    ];
    for (let offset = 0; offset < 4; offset += 1) {
      version = (await chooseManualForSlot(plan.slots[10 + offset].id, choices[offset], version)).version;
    }
    version = (await setStarterForSlot(plan.slots[11].id, { ingredientId: asparagus.id }, version)).version;
    version = (await setStarterForSlot(plan.slots[12].id, { ingredientId: palm.id }, version)).version;
    const result = await setStarterForSlot(plan.slots[13].id, { recipeId: tomatoCarrot.id }, version);
    expect(result.slots.slice(10).map((slot) => slot.assignment?.name)).toEqual([
      'Omelette et jambon grillé · pommes de terre sautées', 'Tournedos · pâtes',
      'Tenders Lidl · riz · brocolis', 'Poulet à la crème moutarde · semoule · haricots verts',
    ]);
    expect(result.slots.slice(10).map((slot) => slot.starter?.name ?? null)).toEqual([
      null, 'Asperges', 'Cœurs de palmiers', 'Tomates et carottes râpées',
    ]);
    const labels = (await getShoppingList(plan.id)).map((entry) => entry.label);
    expect(labels).toContain('Moutarde');
    expect(labels).toContain('Pommes de terre');
    expect(labels).not.toContain('Poulet à la crème moutarde');
  });

  it('associe recette, accompagnement préparé et entrée puis recalcule une semaine confirmée', async () => {
    const egg = await ingredient('Œuf', 'PROTEIN');
    const potatoes = await ingredient('Pommes de terre', 'STARCH');
    const asparagus = await createIngredient({ name: 'Asperges', category: 'VEGETABLE', rating: 3,
      useAsStarter: true, portionPerPerson: 100, unit: 'GRAM', seasons: ['WINTER', 'SPRING', 'SUMMER', 'AUTUMN'], ...allMoments });
    const omelette = await createRecipe({ name: 'Omelette', rating: 3, basePortions: 4,
      allowStarchSide: true, seasons: ['WINTER', 'SPRING', 'SUMMER', 'AUTUMN'],
      ingredients: [{ ingredientId: egg.id, quantity: 8, unit: 'PIECE' }], ...allMoments });
    const sauteed = await createRecipe({ name: 'Pommes de terre sautées', role: 'SIDE_STARCH', rating: 3,
      basePortions: 4, seasons: ['WINTER', 'SPRING', 'SUMMER', 'AUTUMN'],
      ingredients: [{ ingredientId: potatoes.id, quantity: 1000, unit: 'GRAM' }], ...allMoments });
    const plan = await ensureWeeklyPlan('2026-09-21');
    const sunday = plan.slots[13];
    const chosen = await chooseManualForSlot(sunday.id, { recipeId: omelette.id, starchRecipeId: sauteed.id }, plan.version);
    const withStarter = await setStarterForSlot(sunday.id, { ingredientId: asparagus.id }, chosen.version);
    expect(withStarter.slots[13].starter?.name).toBe('Asperges');
    expect((await getShoppingList(plan.id)).map((item) => item.label).sort()).toEqual(['Asperges', 'Pommes de terre', 'Œuf']);
    await prisma.mealSlot.updateMany({ where: { weeklyPlanId: plan.id, slotType: 'EMPTY' }, data: { slotType: 'CUSTOM', customLabel: 'Autre repas' } });
    const confirmed = await confirmPlan(plan.id, withStarter.version);
    const corrected = await updateMealSlot(sunday.id, { guestCount: 5 }, confirmed.version);
    expect(corrected.status).toBe('confirmed');
    expect((await getShoppingList(plan.id)).find((item) => item.label === 'Pommes de terre')?.quantity).toBe(1250);
  });

  it('respecte la fréquence réglée des entrées et conserve un choix manuel', async () => {
    const tomato = await ingredient('Tomate');
    const starter = await createIngredient({ name: 'Cœurs de palmiers', category: 'VEGETABLE', rating: 3,
      useAsStarter: true, portionPerPerson: 100, unit: 'GRAM', seasons: ['WINTER', 'SPRING', 'SUMMER', 'AUTUMN'], ...allMoments });
    for (let index = 0; index < 20; index += 1) await createRecipe({ name: `Plat ${index}`, rating: 3,
      basePortions: 2, seasons: ['WINTER', 'SPRING', 'SUMMER', 'AUTUMN'],
      ingredients: [{ ingredientId: tomato.id, quantity: 200, unit: 'GRAM' }], ...allMoments });
    const initial = await ensureWeeklyPlan('2026-09-21');
    const generated = await generatePersistedWeek('2026-09-21', 'starters-3', initial.version);
    expect(generated.slots.filter((slot) => slot.starter).length).toBe(3);
    expect(generated.slots.filter((slot) => slot.starter && slot.slotIndex < 10 && slot.mealTime === 'lunch')).toHaveLength(0);
    await prisma.appSettings.update({ where: { id: 'default' }, data: { starterTargetPerWeek: 0 } });
    const none = await generatePersistedWeek('2026-09-21', 'starters-0', generated.version);
    expect(none.slots.filter((slot) => slot.starter)).toHaveLength(0);
    const manual = await setStarterForSlot(none.slots[13].id, { ingredientId: starter.id }, none.version);
    await prisma.appSettings.update({ where: { id: 'default' }, data: { starterTargetPerWeek: 2 } });
    const two = await generatePersistedWeek('2026-09-21', 'starters-2', manual.version);
    expect(two.slots.filter((slot) => slot.starter)).toHaveLength(2);
    expect(two.slots[13].starter?.name).toBe('Cœurs de palmiers');
    await prisma.appSettings.update({ where: { id: 'default' }, data: { starterTargetPerWeek: 4 } });
    const four = await generatePersistedWeek('2026-09-21', 'starters-4', two.version);
    expect(four.slots.filter((slot) => slot.starter)).toHaveLength(4);
  });
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
    const aisle = await prisma.aisle.create({ data: { name: 'Épicerie', sortOrder: 1 } });
    await addManualShoppingEntry(plan.id, '  Papier cuisson  ', { quantity: 2, unit: 'PIECE', aisleId: aisle.id });
    await prisma.mealSlot.update({ where: { id: second.id }, data: { guestCount: 4 } });
    await rebuildShoppingList(plan.id);
    entries = await getShoppingList(plan.id);
    expect(entries.find((entry) => !entry.isManual)).toMatchObject({ quantity: 600, isChecked: true });
    const manual = entries.find((entry) => entry.isManual)!;
    expect(manual).toMatchObject({ label: 'Papier cuisson', quantity: 2, unit: 'PIECE', aisleName: 'Épicerie' });
    await expect(addManualShoppingEntry(plan.id, 'Erreur', { quantity: -1 })).rejects.toThrow('quantité');
    await expect(deleteManualShoppingEntry(entries.find((entry) => !entry.isManual)!.id))
      .rejects.toThrow('ajouts manuels');
    await deleteManualShoppingEntry(manual.id);
    expect((await getShoppingList(plan.id)).filter((entry) => entry.isManual)).toHaveLength(0);
  });

  it('préserve un brouillon lors de la réutilisation et relie ses restes copiés', async () => {
    const source = await ensureWeeklyPlan('2026-09-21');
    await prisma.mealSlot.updateMany({ where: { weeklyPlanId: source.id }, data: { slotType: 'CUSTOM', customLabel: 'Repas prévu' } });
    await prisma.mealSlot.update({ where: { id: source.slots[1].id }, data: { slotType: 'LEFTOVERS', customLabel: null, leftoversFromSlotId: source.slots[0].id } });
    await confirmPlan(source.id, source.version);
    const target = await ensureWeeklyPlan('2026-09-28');
    await prisma.mealSlot.update({ where: { id: target.slots[0].id }, data: { slotType: 'CUSTOM', customLabel: 'À conserver' } });
    await expect(reapplyPlan(source.id, '2026-09-28')).rejects.toThrow('remplacement');
    expect((await getPlanByStartDate('2026-09-28'))?.slots[0].customLabel).toBe('À conserver');
    await prisma.weeklyPlan.update({ where: { id: target.id }, data: { version: { increment: 1 } } });
    await expect(reapplyPlan(source.id, '2026-09-28', true, target.version)).rejects.toThrow('autre appareil');
    const copied = await reapplyPlan(source.id, '2026-09-28', true, target.version + 1);
    expect(copied.slots[0].customLabel).toBe('Repas prévu');
    expect(copied.slots[1]).toMatchObject({ slotType: 'leftovers', leftoversFromSlotId: copied.slots[0].id });
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
