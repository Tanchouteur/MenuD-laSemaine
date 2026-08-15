import type { Metadata } from 'next';
import { connection } from 'next/server';
import { CatalogManager } from '@/components/catalog/catalog-manager';
import { listAisles, listIngredients, listRecipes } from '@/services/catalog.service';

export const metadata: Metadata = { title: 'Recettes' };

export default async function RecipesPage() {
  await connection();
  const [ingredients, recipes, aisles] = await Promise.all([
    listIngredients(), listRecipes(), listAisles(),
  ]);
  return <CatalogManager initialIngredients={ingredients} initialRecipes={recipes} aisles={aisles} />;
}
