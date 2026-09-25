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

export function CatalogManager({ initialIngredients, initialRecipes, aisles }: { initialIngredients: IngredientDto[]; initialRecipes: RecipeDto[]; aisles: Aisle[] }) {
  const [tab, setTab] = useState<'recipes' | 'ingredients'>('recipes');
  const [ingredients, setIngredients] = useState(initialIngredients);
  const [recipes, setRecipes] = useState(initialRecipes);
  const [ingredientEdit, setIngredientEdit] = useState<IngredientDto | 'new' | null>(null);
  const [recipeEdit, setRecipeEdit] = useState<RecipeDto | 'new' | { source: RecipeDto } | null>(null);
  const [search, setSearch] = useState('');
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
      <label className="fieldLabel catalogSearch">Rechercher dans le catalogue<input type="search" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Nom d’un plat ou d’un produit" /></label>
      {tab === 'recipes' ? <>
        <button className="primaryButton addButton" onClick={() => setRecipeEdit('new')}>＋ Ajouter une recette</button>
        <div className="catalogList">{recipes.filter((item) => item.name.toLocaleLowerCase('fr-FR').includes(search.toLocaleLowerCase('fr-FR'))).map((recipe) => <article className="catalogCard" key={recipe.id}><div><h2>{recipe.name}</h2><p>{recipe.role === 'STARTER' ? 'Entrée' : recipe.role === 'SIDE_STARCH' ? 'Féculent préparé' : recipe.role === 'SIDE_VEGETABLE' ? 'Légume préparé' : 'Plat'}{recipe.variantOfId ? ` · Variante de ${recipes.find((item) => item.id === recipe.variantOfId)?.name ?? 'une recette'}` : ''} · {recipe.ingredientCount} ingrédient(s){recipe.prepTimeMinutes !== null ? ` · ${recipe.prepTimeMinutes + (recipe.cookTimeMinutes ?? 0)} min` : ''}</p></div><div className="cardActions"><button onClick={() => setRecipeEdit(recipe)}>Modifier</button><button onClick={() => setRecipeEdit({ source: recipe })}>Créer une variante</button><button onClick={() => archive('recipes', recipe.id)}>Retirer</button></div></article>)}</div>
      </> : <>
        <button className="primaryButton addButton" onClick={() => setIngredientEdit('new')}>＋ Ajouter un ingrédient</button>
        <div className="catalogList">{ingredients.filter((item) => item.name.toLocaleLowerCase('fr-FR').includes(search.toLocaleLowerCase('fr-FR'))).map((ingredient) => <article className="catalogCard" key={ingredient.id}><div><h2>{ingredient.name}</h2><p>{categories[ingredient.category]} · {'★'.repeat(ingredient.rating)}{ingredient.portionPerPerson ? ` · ${ingredient.portionPerPerson} ${ingredient.unit ? units[ingredient.unit] : ''}/pers.` : ''}{ingredient.useAsStarter ? ' · Entrée possible' : ''}</p></div><div className="cardActions"><button onClick={() => setIngredientEdit(ingredient)}>Modifier</button><button onClick={() => archive('ingredients', ingredient.id)}>Retirer</button></div></article>)}</div>
      </>}
      {ingredientEdit && <IngredientForm value={ingredientEdit} aisles={aisles} onClose={() => setIngredientEdit(null)} onSaved={(item) => { setIngredients((items) => ingredientEdit === 'new' ? [...items, item].sort((a,b) => a.name.localeCompare(b.name)) : items.map((old) => old.id === item.id ? item : old)); setIngredientEdit(null); }} onError={setError} />}
      {recipeEdit && <RecipeForm key={recipeEdit === 'new' ? 'new' : 'source' in recipeEdit ? `variant-${recipeEdit.source.id}` : recipeEdit.id} value={recipeEdit} recipes={recipes} ingredients={ingredients} aisles={aisles} onIngredientCreated={(item) => setIngredients((items) => [...items, item].sort((a,b) => a.name.localeCompare(b.name)))} onClose={() => setRecipeEdit(null)} onSaved={(item) => { setRecipes((items) => recipeEdit === 'new' || 'source' in recipeEdit ? [...items, item].sort((a,b) => a.name.localeCompare(b.name)) : items.map((old) => old.id === item.id ? item : old)); setRecipeEdit(null); }} onError={setError} />}
    </main>
  );
}

function SeasonFields({ selected, onChange }: { selected: string[]; onChange: (next: string[]) => void }) {
  return <fieldset><legend>Saisons</legend><div className="choiceGrid">{seasons.map((season) => <label key={season}><input type="checkbox" checked={selected.includes(season)} onChange={(event) => onChange(event.target.checked ? [...selected, season] : selected.filter((item) => item !== season))} /> {seasonLabels[season]}</label>)}</div></fieldset>;
}

function MomentFields({ value, starter = false }: { value: IngredientDto | RecipeDto | null; starter?: boolean }) {
  return <fieldset><legend>Quand le proposer ?</legend><div className="choiceGrid">
    <label><input name="okLunchWeekday" type="checkbox" defaultChecked={starter ? false : (value?.okLunchWeekday ?? true)} disabled={starter} /> Déjeuner en semaine{starter ? ' (jamais généré)' : ''}</label>
    <label><input name="okDinnerWeekday" type="checkbox" defaultChecked={value?.okDinnerWeekday ?? true} /> Dîner en semaine</label>
    <label><input name="okLunchWeekend" type="checkbox" defaultChecked={value?.okLunchWeekend ?? true} /> Déjeuner le week-end</label>
    <label><input name="okDinnerWeekend" type="checkbox" defaultChecked={value?.okDinnerWeekend ?? true} /> Dîner le week-end</label>
  </div><p className="fieldHint">Pour un aliment coûteux ou un plat familial, décochez par exemple « Déjeuner en semaine ».</p></fieldset>;
}

function IngredientForm({ value, aisles, onClose, onSaved, onError }: { value: IngredientDto | 'new'; aisles: Aisle[]; onClose: () => void; onSaved: (item: IngredientDto) => void; onError: (message: string) => void }) {
  const current = value === 'new' ? null : value;
  const [selectedSeasons, setSelectedSeasons] = useState(current?.seasons ?? seasons);
  const [useInComposedMeals, setUseInComposedMeals] = useState(current?.useInComposedMeals ?? false);
  const [useAsStarter, setUseAsStarter] = useState(current?.useAsStarter ?? false);
  const [busy, setBusy] = useState(false);
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setBusy(true);
    const data = new FormData(event.currentTarget);
    const usesMoments = useInComposedMeals || useAsStarter;
    const body = { name: data.get('name'), category: data.get('category'), subFamily: data.get('subFamily') || null, rating: Number(data.get('rating')), useInComposedMeals, useAsStarter, portionPerPerson: data.get('portion') ? Number(data.get('portion')) : null, unit: data.get('unit') || null, aisleId: data.get('aisleId') || null, seasons: selectedSeasons, okLunchWeekday: useAsStarter && !useInComposedMeals ? false : usesMoments ? data.has('okLunchWeekday') : (current?.okLunchWeekday ?? true), okDinnerWeekday: usesMoments ? data.has('okDinnerWeekday') : (current?.okDinnerWeekday ?? true), okLunchWeekend: usesMoments ? data.has('okLunchWeekend') : (current?.okLunchWeekend ?? true), okDinnerWeekend: usesMoments ? data.has('okDinnerWeekend') : (current?.okDinnerWeekend ?? true) };
    const response = await fetch(current ? `/api/ingredients/${current.id}` : '/api/ingredients', { method: current ? 'PATCH' : 'POST', headers: { 'content-type': 'application/json' }, body: JSON.stringify(body) });
    if (response.ok) onSaved(await response.json()); else onError((await response.json()).error ?? 'Impossible d’enregistrer.'); setBusy(false);
  }
  return <Modal title={current ? 'Modifier l’ingrédient' : 'Nouvel ingrédient'} onClose={onClose}><form className="stackForm" onSubmit={submit}>
    <label>Nom<input name="name" defaultValue={current?.name} required /></label>
    <div className="formColumns"><label>Type<select name="category" defaultValue={current?.category ?? 'VEGETABLE'}>{Object.entries(categories).map(([key,label]) => <option key={key} value={key}>{label}</option>)}</select></label><label>Appréciation<select name="rating" defaultValue={current?.rating ?? 3}>{[1,2,3,4,5].map((n) => <option key={n} value={n}>{n} / 5</option>)}</select></label></div>
    <label>Famille (facultatif)<input name="subFamily" defaultValue={current?.subFamily ?? ''} placeholder="ex. poisson, pâtes…" /></label>
    <div className="formColumns"><label>Portion par personne<input name="portion" type="number" min="0.01" step="0.01" defaultValue={current?.portionPerPerson ?? ''} /></label><label>Unité<select name="unit" defaultValue={current?.unit ?? ''}><option value="">Non précisée</option>{Object.entries(units).map(([key,label]) => <option key={key} value={key}>{label}</option>)}</select></label></div>
    <label>Rayon<select name="aisleId" defaultValue={current?.aisleId ?? ''}><option value="">Autres</option>{aisles.map((aisle) => <option key={aisle.id} value={aisle.id}>{aisle.name}</option>)}</select></label>
    <label className="checkboxRow"><input name="useInComposedMeals" type="checkbox" checked={useInComposedMeals} onChange={(event) => setUseInComposedMeals(event.target.checked)} /><span><strong>Utiliser dans les assiettes automatiques</strong><small>Sans effet sur les recettes et la liste de courses.</small></span></label>
    <label className="checkboxRow"><input type="checkbox" checked={useAsStarter} onChange={(event) => setUseAsStarter(event.target.checked)} /><span><strong>Proposer comme entrée</strong><small>Pour un produit prêt à manger, par exemple des cœurs de palmiers.</small></span></label>
    <div className="conditionalFields" hidden={!useInComposedMeals && !useAsStarter}>
      <SeasonFields selected={selectedSeasons} onChange={setSelectedSeasons} />
      <MomentFields value={current} starter={useAsStarter && !useInComposedMeals} />
    </div>
    <button className="primaryButton" disabled={busy || selectedSeasons.length === 0}>{busy ? 'Enregistrement…' : 'Enregistrer'}</button>
  </form></Modal>;
}

function RecipeForm({ value, recipes, ingredients, aisles, onIngredientCreated, onClose, onSaved, onError }: { value: RecipeDto | 'new' | { source: RecipeDto }; recipes: RecipeDto[]; ingredients: IngredientDto[]; aisles: Aisle[]; onIngredientCreated: (item: IngredientDto) => void; onClose: () => void; onSaved: (item: RecipeDto) => void; onError: (message: string) => void }) {
  const creating = value === 'new' || 'source' in value;
  const current = value === 'new' ? null : 'source' in value ? value.source : value;
  const [selectedSeasons, setSelectedSeasons] = useState(current?.seasons ?? seasons);
  const [role, setRole] = useState(current?.role ?? 'MAIN');
  const [allowStarchSide, setAllowStarchSide] = useState(current?.allowStarchSide ?? false);
  const [allowVegetableSide, setAllowVegetableSide] = useState(current?.allowVegetableSide ?? false);
  const [lineSearch, setLineSearch] = useState<string[]>([]);
  const [lines, setLines] = useState<RecipeLine[]>(current?.ingredients.map((line) => ({ ingredientId: line.ingredientId, quantity: line.quantity, unit: line.unit })) ?? [{ ingredientId: '', quantity: 1, unit: 'PIECE' }]);
  const [busy, setBusy] = useState(false);
  const [showIngredientForm, setShowIngredientForm] = useState(false);
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setBusy(true); const data = new FormData(event.currentTarget);
    const body = { name: data.get('name'), role, allowStarchSide: role === 'MAIN' && allowStarchSide, allowVegetableSide: role === 'MAIN' && allowVegetableSide, variantOfId: data.get('variantOfId') || null, style: data.get('style') || null, rating: Number(data.get('rating')), prepTimeMinutes: data.get('prep') ? Number(data.get('prep')) : null, cookTimeMinutes: data.get('cook') ? Number(data.get('cook')) : null, basePortions: Number(data.get('portions')), seasons: selectedSeasons, okLunchWeekday: role === 'STARTER' ? false : data.has('okLunchWeekday'), okDinnerWeekday: data.has('okDinnerWeekday'), okLunchWeekend: data.has('okLunchWeekend'), okDinnerWeekend: data.has('okDinnerWeekend'), ingredients: lines };
    const response = await fetch(creating ? '/api/recipes' : `/api/recipes/${current!.id}`, { method: creating ? 'POST' : 'PUT', headers: { 'content-type': 'application/json' }, body: JSON.stringify(body) });
    if (response.ok) onSaved(await response.json()); else onError((await response.json()).error ?? 'Impossible d’enregistrer.'); setBusy(false);
  }
  function updateLine(index: number, patch: Partial<RecipeLine>) { setLines((items) => items.map((item, i) => i === index ? { ...item, ...patch } : item)); }
  function updateLineSearch(index: number, query: string) { setLineSearch((items) => { const next = [...items]; next[index] = query; return next; }); }
  return <Modal title={creating ? 'Nouvelle recette' : 'Modifier la recette'} onClose={onClose}><form className="stackForm" onSubmit={submit}>
    <label>Nom<input name="name" defaultValue={creating && current ? `${current.name} — variante` : current?.name} required /></label>
    <label>Cette recette est un<select value={role} onChange={(event) => setRole(event.target.value as RecipeDto['role'])}><option value="MAIN">Plat</option><option value="STARTER">Entrée</option><option value="SIDE_STARCH">Féculent préparé</option><option value="SIDE_VEGETABLE">Légume préparé</option></select></label>
    <label>Portions de base<input name="portions" type="number" min="1" defaultValue={current?.basePortions ?? 4} required /></label>
    <fieldset><legend>Produits utilisés</legend><div className="recipeLines">{lines.map((line, index) => <div className="recipeLine" key={index}><input type="search" aria-label="Rechercher un produit" value={lineSearch[index] ?? ''} onChange={(event) => updateLineSearch(index, event.target.value)} placeholder="Rechercher…" /><select aria-label="Produit" value={line.ingredientId} onChange={(e) => updateLine(index, { ingredientId: e.target.value })}><option value="">Choisir un produit…</option>{ingredients.filter((item) => item.id === line.ingredientId || item.name.toLocaleLowerCase('fr-FR').includes((lineSearch[index] ?? '').toLocaleLowerCase('fr-FR'))).map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select><input aria-label="Quantité" type="number" min="0.01" step="0.01" value={line.quantity} onChange={(e) => updateLine(index, { quantity: Number(e.target.value) })} /><select aria-label="Unité" value={line.unit} onChange={(e) => updateLine(index, { unit: e.target.value })}>{Object.entries(units).map(([key,label]) => <option key={key} value={key}>{label}</option>)}</select><button type="button" aria-label="Retirer la ligne" disabled={lines.length === 1} onClick={() => setLines((items) => items.filter((_, i) => i !== index))}>×</button></div>)}</div><div className="formButtonRow"><button className="secondaryButton" type="button" onClick={() => setLines((items) => [...items, { ingredientId: '', quantity: 1, unit: 'PIECE' }])}>＋ Ajouter une ligne</button><button className="secondaryButton" type="button" onClick={() => setShowIngredientForm(true)}>＋ Nouveau produit</button></div></fieldset>
    <details className="recipeAdvanced"><summary>Options de proposition et variantes</summary><div className="recipeAdvancedFields">
      {role === 'MAIN' && <fieldset><legend>Recette à accompagner</legend><div className="choiceGrid"><label><input type="checkbox" checked={allowStarchSide} onChange={(event) => setAllowStarchSide(event.target.checked)} /> Féculent à choisir</label><label><input type="checkbox" checked={allowVegetableSide} onChange={(event) => setAllowVegetableSide(event.target.checked)} /> Légume à choisir</label></div><p className="fieldHint">Laissez les deux cases vides si la recette est déjà complète.</p></fieldset>}
      <label>Variante de (facultatif)<select name="variantOfId" defaultValue={creating && current ? (current.variantOfId ?? current.id) : current?.variantOfId ?? ''}><option value="">Aucune</option>{recipes.filter((item) => item.id !== (creating ? '' : current?.id) && item.role === role).map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label>
      <label>Style (facultatif)<input name="style" defaultValue={current?.style ?? ''} placeholder="ex. gratin, salade…" /></label>
      <div className="formColumns"><label>Préparation (min)<input name="prep" type="number" min="0" defaultValue={current?.prepTimeMinutes ?? ''} /></label><label>Cuisson (min)<input name="cook" type="number" min="0" defaultValue={current?.cookTimeMinutes ?? ''} /></label></div>
      <label>Appréciation<select name="rating" defaultValue={current?.rating ?? 3}>{[1,2,3,4,5].map((n) => <option key={n} value={n}>{n} / 5</option>)}</select></label>
      <SeasonFields selected={selectedSeasons} onChange={setSelectedSeasons} />
      <MomentFields value={current} starter={role === 'STARTER'} />
    </div></details>
    <button className="primaryButton" disabled={busy || selectedSeasons.length === 0 || lines.some((line) => !line.ingredientId)}>{busy ? 'Enregistrement…' : 'Enregistrer'}</button>
  </form>{showIngredientForm && <IngredientForm value="new" aisles={aisles} onClose={() => setShowIngredientForm(false)} onSaved={(item) => { onIngredientCreated(item); setLines((items) => items.map((line, index) => index === items.length - 1 && !line.ingredientId ? { ...line, ingredientId: item.id } : line)); setShowIngredientForm(false); }} onError={onError} />}</Modal>;
}

function Modal({ title, onClose, children }: { title: string; onClose: () => void; children: React.ReactNode }) {
  return <div className="sheetBackdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose(); }}><section className="alternativeSheet" role="dialog" aria-modal="true" aria-label={title}><div className="sheetHandle" /><header className="sheetHeader"><h2>{title}</h2><button className="closeButton" onClick={onClose} aria-label="Fermer">×</button></header>{children}</section></div>;
}
