import 'dotenv/config';
import mariadb from 'mariadb';
import { PrismaPg } from '@prisma/adapter-pg';
import { IngredientCategory, MealTime, PlanStatus, PrismaClient, Season, SlotType } from '../generated/prisma/client';

type LegacyProduct = {
  id: number; nomProduit: string; typeProduit: string; poidsArbitraire: number;
  poidsMidiSemaine: number | null; poidsSoirSemaine: number | null;
  poidsMidiWeekend: number | null; poidsSoirWeekend: number | null;
  poidsPrintemps: number | null; poidsEte: number | null;
  poidsAutomne: number | null; poidsHiver: number | null;
};
type LegacyPair = { idProduit1: number; idProduit2: number };
type LegacyMenu = { jour: string; moment: string; plat: string; entree: string | null };

const legacyUrl = process.env.LEGACY_DATABASE_URL;
const targetUrl = process.env.DATABASE_URL;
const write = process.argv.includes('--write');
if (!legacyUrl) throw new Error('LEGACY_DATABASE_URL est obligatoire.');
if (!targetUrl) throw new Error('DATABASE_URL est obligatoire.');

function enabledSeasons(product: LegacyProduct): Season[] {
  const values: Array<[number | null, Season]> = [[product.poidsHiver, Season.WINTER], [product.poidsPrintemps, Season.SPRING], [product.poidsEte, Season.SUMMER], [product.poidsAutomne, Season.AUTUMN]];
  const enabled = values.filter(([weight]) => (weight ?? 0) > 0).map(([, season]) => season);
  return enabled.length ? enabled : [Season.WINTER, Season.SPRING, Season.SUMMER, Season.AUTUMN];
}

function rating(weight: number): number {
  if (weight <= 0) return 1;
  if (weight <= 2) return 2;
  if (weight <= 5) return 3;
  if (weight <= 8) return 4;
  return 5;
}

function asMonday(value: string): Date {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) throw new Error('LEGACY_MENU_WEEK doit être au format AAAA-MM-JJ.');
  const date = new Date(`${value}T00:00:00.000Z`);
  if (date.getUTCDay() !== 1) throw new Error('LEGACY_MENU_WEEK doit être un lundi.');
  return date;
}

async function main() {
  const legacy = await mariadb.createConnection(legacyUrl!);
  const prisma = new PrismaClient({ adapter: new PrismaPg({ connectionString: targetUrl! }) });
  try {
    const products = await legacy.query<LegacyProduct[]>('SELECT p.*, pm.poidsMidiSemaine, pm.poidsSoirSemaine, pm.poidsMidiWeekend, pm.poidsSoirWeekend, ps.poidsPrintemps, ps.poidsEte, ps.poidsAutomne, ps.poidsHiver FROM Produits p LEFT JOIN PoidsMoment pm ON p.id = pm.idProduit LEFT JOIN PoidsSaison ps ON p.id = ps.idProduit');
    const pairs = await legacy.query<LegacyPair[]>('SELECT idProduit1, idProduit2 FROM Incompatibilite');
    const menu = await legacy.query<LegacyMenu[]>('SELECT jour, moment, plat, entree FROM Menu');
    const counts = { products: products.length, ingredients: products.filter((item) => item.typeProduit !== 'platComplet').length, recipes: products.filter((item) => item.typeProduit === 'platComplet').length, incompatibilities: pairs.length, menuSlots: menu.length };
    console.info(JSON.stringify({ mode: write ? 'write' : 'dry-run', source: counts, warnings: ['Les portions et rayons doivent être complétés après import.', 'Les plats complets sont importés sans ingrédients et restent à compléter.'] }, null, 2));
    if (!write) { console.info('Aucune écriture effectuée. Relancez avec --write après vérification.'); return; }

    const idMap = new Map<number, string>();
    for (const product of products) {
      const common = {
        rating: rating(product.poidsArbitraire), seasons: enabledSeasons(product),
        okLunchWeekday: (product.poidsMidiSemaine ?? 0) > 0,
        okDinnerWeekday: (product.poidsSoirSemaine ?? 0) > 0,
        okLunchWeekend: (product.poidsMidiWeekend ?? 0) > 0,
        okDinnerWeekend: (product.poidsSoirWeekend ?? 0) > 0,
      };
      if (!Object.values(common).slice(2).some(Boolean)) Object.assign(common, { okLunchWeekday: true, okDinnerWeekday: true, okLunchWeekend: true, okDinnerWeekend: true });
      if (product.typeProduit === 'platComplet') {
        const recipe = await prisma.recipe.upsert({ where: { name: product.nomProduit }, update: { ...common, isActive: true }, create: { name: product.nomProduit, style: 'Importé · ingrédients à compléter', basePortions: 4, ...common } });
        idMap.set(product.id, recipe.id);
        continue;
      }
      const category = ({ viande: IngredientCategory.PROTEIN, feculent: IngredientCategory.STARCH, legume: IngredientCategory.VEGETABLE, entree: IngredientCategory.OTHER } as Record<string, IngredientCategory>)[product.typeProduit] ?? IngredientCategory.OTHER;
      const ingredient = await prisma.ingredient.upsert({ where: { name: product.nomProduit }, update: { category, ...common }, create: { name: product.nomProduit, category, isActive: product.typeProduit !== 'entree', ...common } });
      idMap.set(product.id, ingredient.id);
    }

    let importedPairs = 0;
    for (const pair of pairs) {
      const first = idMap.get(pair.idProduit1); const second = idMap.get(pair.idProduit2);
      if (!first || !second || first === second) continue;
      const [ingredientId1, ingredientId2] = [first, second].sort();
      const bothIngredients = await prisma.ingredient.count({ where: { id: { in: [ingredientId1, ingredientId2] } } });
      if (bothIngredients !== 2) continue;
      await prisma.incompatibility.upsert({ where: { ingredientId1_ingredientId2: { ingredientId1, ingredientId2 } }, update: {}, create: { ingredientId1, ingredientId2 } }); importedPairs++;
    }

    if (process.env.LEGACY_MENU_WEEK && menu.length) {
      const monday = asMonday(process.env.LEGACY_MENU_WEEK);
      const days: Record<string, number> = { lundi: 0, mardi: 1, mercredi: 2, jeudi: 3, vendredi: 4, samedi: 5, dimanche: 6 };
      const plan = await prisma.weeklyPlan.upsert({ where: { startDate: monday }, update: {}, create: { startDate: monday, status: PlanStatus.DRAFT } });
      for (const row of menu) {
        const day = days[row.jour.toLowerCase()]; if (day === undefined) continue;
        const mealDate = new Date(monday); mealDate.setUTCDate(mealDate.getUTCDate() + day);
        const mealTime = row.moment.toLowerCase() === 'midi' ? MealTime.LUNCH : MealTime.DINNER;
        await prisma.mealSlot.upsert({ where: { weeklyPlanId_mealDate_mealTime: { weeklyPlanId: plan.id, mealDate, mealTime } }, update: { slotType: SlotType.CUSTOM, customLabel: row.plat }, create: { weeklyPlanId: plan.id, mealDate, mealTime, slotType: SlotType.CUSTOM, customLabel: row.plat } });
      }
    }
    console.info(JSON.stringify({ imported: { products: products.length, incompatibilities: importedPairs, menuSlots: process.env.LEGACY_MENU_WEEK ? menu.length : 0 }, idMap: Object.fromEntries(idMap) }, null, 2));
  } finally {
    await legacy.end(); await prisma.$disconnect();
  }
}

main().catch((error: unknown) => { console.error(error); process.exitCode = 1; });
