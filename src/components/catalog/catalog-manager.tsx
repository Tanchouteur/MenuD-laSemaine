'use client';

import { useState } from 'react';
import type { FormEvent } from 'react';
import type { IngredientDto, RecipeDto } from '@/types/api';

type Aisle = { id: string; name: string };
type RecipeLine = { ingredientId: string; quantity: number; unit: string };
const seasons = ['WINTER', 'SPRING', 'SUMMER', 'AUTUMN'];
const seasonLabels: Record<string, string> = { WINTER: 'Hiver', SPRING: 'Printemps', SUMMER: 'Été', AUTUMN: 'Automne' };
const categories: Record<string, string> = { PROTEIN: 'Protéine', STARCH: 'Féculent', VEGETABLE: 'Légume', GROCERY: 'Épicerie', DAIRY: 'Produit frais', OTHER: 'Autre' };
const units: Record<string, string> = { GRAM: 'grammes', KILOGRAM: 'kilogrammes', MILLILITER: 'millilitres', CENTILITER: 'centilitres', LITER: 'litres', PIECE: 'pièces', SLICE: 'tranches', CAN: 'boîtes' };
const defaultMoments = { okLunchWeekday: true, okDinnerWeekday: true, okLunchWeekend: true, okDinnerWeekend: true };

export function CatalogManager({ initialIngredients, initialRecipes, aisles }: { initialIngredients: IngredientDto[]; initialRecipes: RecipeDto[]; aisles: Aisle[] }) {
  const [tab, setTab] = useState<'recipes' | 'ingredients'>('recipes');
  const [ingredients, setIngredients] = useState(initialIngredients);
  const [recipes, setRecipes] = useState(initialRecipes);
  const [ingredientEdit, setIngredientEdit] = useState<IngredientDto | 'new' | null>(null);
  const [recipeEdit, setRecipeEdit] = useState<RecipeDto | 'new' | null>(null);
  const [error, setError] = useState('');

  async function archive(kind: 'ingredients' | 'recipes', id: string) {
    if (!confirm('Retirer cet élément de la bibliothèque ? Les anciennes semaines le conserveront.')) return;
    const response = await fetch(`/api/${kind}/${id}`, { method: 'DELETE' });
    if (!response.ok) return setError('Cette suppression n’a pas pu être enregistrée.');
    if (kind === 'ingredients') setIngredients((items) => items.filter((item) => item.id !== id));
    else setRecipes((items) => items.filter((item) => item.id !== id));
  }

  return (
    <main className="pageShell catalogPage">
      <header className="sectionHeader"><div><p className="eyebrow">Bibliothèque familiale</p><h1>Recettes</h1><p>Ajoutez ce que votre famille aime, avec des mots simples.</p></div><span className="familyAvatar" aria-hidden="true">⌕</span></header>
      {error && <p className="errorSummary" role="alert">{error}</p>}
      <div className="segmented" role="tablist">
        <button data-active={tab === 'recipes'} onClick={() => setTab('recipes')}>Recettes ({recipes.length})</button>
        <button data-active={tab === 'ingredients'} onClick={() => setTab('ingredients')}>Ingrédients ({ingredients.length})</button>
      </div>
      {tab === 'recipes' ? <>
        <button className="primaryButton addButton" onClick={() => setRecipeEdit('new')}>＋ Ajouter une recette</button>
        <div className="catalogList">{recipes.map((recipe) => <article className="catalogCard" key={recipe.id}><div><h2>{recipe.name}</h2><p>{recipe.ingredientCount} ingrédient(s){recipe.prepTimeMinutes !== null ? ` · ${recipe.prepTimeMinutes + (recipe.cookTimeMinutes ?? 0)} min` : ''} · {'★'.repeat(recipe.rating)}</p></div><div className="cardActions"><button onClick={() => setRecipeEdit(recipe)}>Modifier</button><button onClick={() => archive('recipes', recipe.id)}>Retirer</button></div></article>)}</div>
      </> : <>
        <button className="primaryButton addButton" onClick={() => setIngredientEdit('new')}>＋ Ajouter un ingrédient</button>
        <div className="catalogList">{ingredients.map((ingredient) => <article className="catalogCard" key={ingredient.id}><div><h2>{ingredient.name}</h2><p>{categories[ingredient.category]} · {'★'.repeat(ingredient.rating)}{ingredient.portionPerPerson ? ` · ${ingredient.portionPerPerson} ${ingredient.unit ? units[ingredient.unit] : ''}/pers.` : ''}</p></div><div className="cardActions"><button onClick={() => setIngredientEdit(ingredient)}>Modifier</button><button onClick={() => archive('ingredients', ingredient.id)}>Retirer</button></div></article>)}</div>
      </>}
      {ingredientEdit && <IngredientForm value={ingredientEdit} aisles={aisles} onClose={() => setIngredientEdit(null)} onSaved={(item) => { setIngredients((items) => ingredientEdit === 'new' ? [...items, item].sort((a,b) => a.name.localeCompare(b.name)) : items.map((old) => old.id === item.id ? item : old)); setIngredientEdit(null); }} onError={setError} />}
      {recipeEdit && <RecipeForm value={recipeEdit} ingredients={ingredients} onClose={() => setRecipeEdit(null)} onSaved={(item) => { setRecipes((items) => recipeEdit === 'new' ? [...items, item].sort((a,b) => a.name.localeCompare(b.name)) : items.map((old) => old.id === item.id ? item : old)); setRecipeEdit(null); }} onError={setError} />}
    </main>
  );
}

function SeasonFields({ selected, onChange }: { selected: string[]; onChange: (next: string[]) => void }) {
  return <fieldset><legend>Saisons</legend><div className="choiceGrid">{seasons.map((season) => <label key={season}><input type="checkbox" checked={selected.includes(season)} onChange={(event) => onChange(event.target.checked ? [...selected, season] : selected.filter((item) => item !== season))} /> {seasonLabels[season]}</label>)}</div></fieldset>;
}

function IngredientForm({ value, aisles, onClose, onSaved, onError }: { value: IngredientDto | 'new'; aisles: Aisle[]; onClose: () => void; onSaved: (item: IngredientDto) => void; onError: (message: string) => void }) {
  const current = value === 'new' ? null : value;
  const [selectedSeasons, setSelectedSeasons] = useState(current?.seasons ?? seasons);
  const [busy, setBusy] = useState(false);
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setBusy(true);
    const data = new FormData(event.currentTarget);
    const body = { name: data.get('name'), category: data.get('category'), subFamily: data.get('subFamily') || null, rating: Number(data.get('rating')), portionPerPerson: data.get('portion') ? Number(data.get('portion')) : null, unit: data.get('unit') || null, aisleId: data.get('aisleId') || null, seasons: selectedSeasons, ...defaultMoments };
    const response = await fetch(current ? `/api/ingredients/${current.id}` : '/api/ingredients', { method: current ? 'PATCH' : 'POST', headers: { 'content-type': 'application/json' }, body: JSON.stringify(body) });
    if (response.ok) onSaved(await response.json()); else onError((await response.json()).error ?? 'Impossible d’enregistrer.'); setBusy(false);
  }
  return <Modal title={current ? 'Modifier l’ingrédient' : 'Nouvel ingrédient'} onClose={onClose}><form className="stackForm" onSubmit={submit}>
    <label>Nom<input name="name" defaultValue={current?.name} required /></label>
    <div className="formColumns"><label>Type<select name="category" defaultValue={current?.category ?? 'VEGETABLE'}>{Object.entries(categories).map(([key,label]) => <option key={key} value={key}>{label}</option>)}</select></label><label>Appréciation<select name="rating" defaultValue={current?.rating ?? 3}>{[1,2,3,4,5].map((n) => <option key={n} value={n}>{n} / 5</option>)}</select></label></div>
    <label>Famille (facultatif)<input name="subFamily" defaultValue={current?.subFamily ?? ''} placeholder="ex. poisson, pâtes…" /></label>
    <div className="formColumns"><label>Portion par personne<input name="portion" type="number" min="0.01" step="0.01" defaultValue={current?.portionPerPerson ?? ''} /></label><label>Unité<select name="unit" defaultValue={current?.unit ?? ''}><option value="">Non précisée</option>{Object.entries(units).map(([key,label]) => <option key={key} value={key}>{label}</option>)}</select></label></div>
    <label>Rayon<select name="aisleId" defaultValue={current?.aisleId ?? ''}><option value="">Autres</option>{aisles.map((aisle) => <option key={aisle.id} value={aisle.id}>{aisle.name}</option>)}</select></label>
    <SeasonFields selected={selectedSeasons} onChange={setSelectedSeasons} />
    <button className="primaryButton" disabled={busy || selectedSeasons.length === 0}>{busy ? 'Enregistrement…' : 'Enregistrer'}</button>
  </form></Modal>;
}

function RecipeForm({ value, ingredients, onClose, onSaved, onError }: { value: RecipeDto | 'new'; ingredients: IngredientDto[]; onClose: () => void; onSaved: (item: RecipeDto) => void; onError: (message: string) => void }) {
  const current = value === 'new' ? null : value;
  const [selectedSeasons, setSelectedSeasons] = useState(current?.seasons ?? seasons);
  const [lines, setLines] = useState<RecipeLine[]>(current?.ingredients.map((line) => ({ ingredientId: line.ingredientId, quantity: line.quantity, unit: line.unit })) ?? [{ ingredientId: ingredients[0]?.id ?? '', quantity: 1, unit: 'PIECE' }]);
  const [busy, setBusy] = useState(false);
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setBusy(true); const data = new FormData(event.currentTarget);
    const body = { name: data.get('name'), style: data.get('style') || null, rating: Number(data.get('rating')), prepTimeMinutes: data.get('prep') ? Number(data.get('prep')) : null, cookTimeMinutes: data.get('cook') ? Number(data.get('cook')) : null, basePortions: Number(data.get('portions')), seasons: selectedSeasons, ...defaultMoments, ingredients: lines };
    const response = await fetch(current ? `/api/recipes/${current.id}` : '/api/recipes', { method: current ? 'PUT' : 'POST', headers: { 'content-type': 'application/json' }, body: JSON.stringify(body) });
    if (response.ok) onSaved(await response.json()); else onError((await response.json()).error ?? 'Impossible d’enregistrer.'); setBusy(false);
  }
  function updateLine(index: number, patch: Partial<RecipeLine>) { setLines((items) => items.map((item, i) => i === index ? { ...item, ...patch } : item)); }
  return <Modal title={current ? 'Modifier la recette' : 'Nouvelle recette'} onClose={onClose}><form className="stackForm" onSubmit={submit}>
    <label>Nom<input name="name" defaultValue={current?.name} required /></label><label>Style (facultatif)<input name="style" defaultValue={current?.style ?? ''} placeholder="ex. gratin, salade…" /></label>
    <div className="formColumns"><label>Préparation (min)<input name="prep" type="number" min="0" defaultValue={current?.prepTimeMinutes ?? ''} /></label><label>Cuisson (min)<input name="cook" type="number" min="0" defaultValue={current?.cookTimeMinutes ?? ''} /></label></div>
    <div className="formColumns"><label>Portions de base<input name="portions" type="number" min="1" defaultValue={current?.basePortions ?? 4} required /></label><label>Appréciation<select name="rating" defaultValue={current?.rating ?? 3}>{[1,2,3,4,5].map((n) => <option key={n} value={n}>{n} / 5</option>)}</select></label></div>
    <fieldset><legend>Ingrédients</legend><div className="recipeLines">{lines.map((line, index) => <div className="recipeLine" key={index}><select aria-label="Ingrédient" value={line.ingredientId} onChange={(e) => updateLine(index, { ingredientId: e.target.value })}>{ingredients.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select><input aria-label="Quantité" type="number" min="0.01" step="0.01" value={line.quantity} onChange={(e) => updateLine(index, { quantity: Number(e.target.value) })} /><select aria-label="Unité" value={line.unit} onChange={(e) => updateLine(index, { unit: e.target.value })}>{Object.entries(units).map(([key,label]) => <option key={key} value={key}>{label}</option>)}</select><button type="button" aria-label="Retirer la ligne" disabled={lines.length === 1} onClick={() => setLines((items) => items.filter((_, i) => i !== index))}>×</button></div>)}</div><button className="secondaryButton" type="button" onClick={() => setLines((items) => [...items, { ingredientId: ingredients[0]?.id ?? '', quantity: 1, unit: 'PIECE' }])}>＋ Ajouter un ingrédient</button></fieldset>
    <SeasonFields selected={selectedSeasons} onChange={setSelectedSeasons} />
    <button className="primaryButton" disabled={busy || selectedSeasons.length === 0 || !lines[0]?.ingredientId}>{busy ? 'Enregistrement…' : 'Enregistrer'}</button>
  </form></Modal>;
}

function Modal({ title, onClose, children }: { title: string; onClose: () => void; children: React.ReactNode }) {
  return <div className="sheetBackdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose(); }}><section className="alternativeSheet" role="dialog" aria-modal="true" aria-label={title}><div className="sheetHandle" /><header className="sheetHeader"><h2>{title}</h2><button className="closeButton" onClick={onClose} aria-label="Fermer">×</button></header>{children}</section></div>;
}
