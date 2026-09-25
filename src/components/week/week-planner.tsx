'use client';

import Link from 'next/link';
import { useEffect, useMemo, useState } from 'react';
import { formatDay, formatWeekRange } from '@/lib/week';
import type { IngredientDto, MealSlotDto, RecipeDto, WeeklyPlanDto } from '@/types/api';

type AlternativeDto = {
  signature: string;
  name: string;
  description?: string;
  totalMinutes?: number;
  reasons: string[];
};

type AlternativeSheet = {
  slotId: string;
  items: AlternativeDto[];
  rejected: Set<string>;
  page: number;
};

type WeekPlannerProps = {
  initialPlan: WeeklyPlanDto;
  previousWeek: string;
  nextWeek: string;
  initialOnboardingCompleted: boolean;
  initialCompositionSetupCompleted: boolean;
  ingredients: IngredientDto[];
  recipes: RecipeDto[];
};

async function apiRequest<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...init?.headers },
  });
  const body = response.status === 204 ? null : await response.json();
  if (!response.ok) {
    throw new Error((body as { error?: string } | null)?.error ?? 'Une erreur est survenue.');
  }
  return body as T;
}

function displayMeal(slot: MealSlotDto, allSlots: MealSlotDto[]) {
  if (slot.assignment) {
    return {
      name: slot.assignment.name,
      description: slot.assignment.description ?? 'Repas choisi pour la famille.',
      minutes: slot.assignment.totalMinutes,
    };
  }
  if (slot.slotType === 'eating_out') {
    return { name: slot.customLabel || 'Repas à l’extérieur', description: 'Aucune course à prévoir.' };
  }
  if (slot.slotType === 'custom') {
    return { name: slot.customLabel || 'Repas libre', description: 'Choisi directement par la famille.' };
  }
  if (slot.slotType === 'leftovers') {
    const source = allSlots.find((item) => item.id === slot.leftoversFromSlotId);
    return {
      name: `Restes${source?.assignment ? ` de ${source.assignment.name}` : ''}`,
      description: 'Les quantités ne sont pas ajoutées une seconde fois aux courses.',
    };
  }
  return { name: 'À choisir', description: 'Ce créneau sera rempli à la prochaine génération.' };
}

export function WeekPlanner({
  initialPlan,
  previousWeek,
  nextWeek,
  initialOnboardingCompleted,
  initialCompositionSetupCompleted,
  ingredients,
  recipes,
}: WeekPlannerProps) {
  const [plan, setPlan] = useState(initialPlan);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [alternativeSheet, setAlternativeSheet] = useState<AlternativeSheet | null>(null);
  const [chooserTab, setChooserTab] = useState<'suggestions' | 'recipes' | 'compose' | 'custom'>('suggestions');
  const [recipeSearch, setRecipeSearch] = useState('');
  const [starterSearch, setStarterSearch] = useState('');
  const [starterSlotId, setStarterSlotId] = useState<string | null>(null);
  const [selectedRecipeId, setSelectedRecipeId] = useState('');
  const [proteinSearch, setProteinSearch] = useState('');
  const [starchSearch, setStarchSearch] = useState('');
  const [vegetableSearch, setVegetableSearch] = useState('');
  const [proteinId, setProteinId] = useState('');
  const [starchId, setStarchId] = useState('');
  const [vegetableId, setVegetableId] = useState('');
  const [actionSlot, setActionSlot] = useState<MealSlotDto | null>(null);
  const [customLabel, setCustomLabel] = useState('');
  const [showOnboarding, setShowOnboarding] = useState(!initialOnboardingCompleted);
  const [showCompositionSetup, setShowCompositionSetup] = useState(!initialCompositionSetupCompleted);
  const [compositionSetupCompleted, setCompositionSetupCompleted] = useState(initialCompositionSetupCompleted);
  const [warnings, setWarnings] = useState<string[]>([]);
  const [undoSlot, setUndoSlot] = useState<MealSlotDto | null>(null);
  const isDraft = plan.status === 'draft';
  const proteins = useMemo(() => ingredients.filter((item) => item.category === 'PROTEIN'), [ingredients]);
  const starches = useMemo(() => ingredients.filter((item) => item.category === 'STARCH'), [ingredients]);
  const vegetables = useMemo(() => ingredients.filter((item) => item.category === 'VEGETABLE'), [ingredients]);
  const starterIngredients = useMemo(() => ingredients.filter((item) => item.useAsStarter), [ingredients]);
  const starterRecipes = useMemo(() => recipes.filter((item) => item.role === 'STARTER'), [recipes]);
  const starchRecipes = useMemo(() => recipes.filter((item) => item.role === 'SIDE_STARCH'), [recipes]);
  const vegetableRecipes = useMemo(() => recipes.filter((item) => item.role === 'SIDE_VEGETABLE'), [recipes]);
  const mainRecipes = useMemo(() => recipes.filter((item) => item.role === 'MAIN'), [recipes]);

  function sideSelection() {
    return {
      ...(starchId.startsWith('r:') ? { starchRecipeId: starchId.slice(2) } : starchId ? { starchId } : {}),
      ...(vegetableId.startsWith('r:') ? { vegetableRecipeId: vegetableId.slice(2) } : vegetableId ? { vegetableId } : {}),
    };
  }

  useEffect(() => {
    function onKeyDown(event: KeyboardEvent) {
      if (event.key !== 'Escape') return;
      if (alternativeSheet) setAlternativeSheet(null);
      else if (starterSlotId) setStarterSlotId(null);
      else if (actionSlot) setActionSlot(null);
    }
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [alternativeSheet, starterSlotId, actionSlot]);

  async function run(action: () => Promise<WeeklyPlanDto>, success: string) {
    setBusy(true);
    setError(null);
    try {
      const updated = await action();
      setPlan(updated);
      setWarnings(updated.generationWarnings ?? []);
      setMessage(success);
      return true;
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Une erreur est survenue.');
      return false;
    } finally {
      setBusy(false);
    }
  }

  function regenerate() {
    void run(
      () =>
        apiRequest('/api/plans/generate', {
          method: 'POST',
          body: JSON.stringify({
            startDate: plan.startDate,
            seed: `${plan.startDate}:version-${plan.version + 1}`,
            version: plan.version,
          }),
        }),
      'La semaine a été préparée.',
    );
  }

  async function patchSlot(slotId: string, update: object, success: string) {
    const previous = plan.slots.find((slot) => slot.id === slotId) ?? null;
    const succeeded = await run(
      () =>
        apiRequest(`/api/slots/${slotId}`, {
          method: 'PATCH',
          body: JSON.stringify({ ...update, version: plan.version }),
        }),
      success,
    );
    if (succeeded && previous) setUndoSlot(previous);
    return succeeded;
  }

  async function showAlternatives(
    slotId: string,
    rejected = new Set<string>(),
    page = 0,
  ) {
    setBusy(true);
    setError(null);
    try {
      const items = plan.status === 'confirmed' ? [] : await apiRequest<AlternativeDto[]>(`/api/slots/${slotId}/alternatives`, {
        method: 'POST',
        body: JSON.stringify({
          rejectedSignatures: [...rejected],
          seed: `${slotId}:page-${page}:rejected-${rejected.size}`,
        }),
      });
      setChooserTab(plan.status === 'confirmed' ? 'recipes' : 'suggestions');
      setSelectedRecipeId('');
      setAlternativeSheet({ slotId, items, rejected, page });
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Impossible de proposer des idées.');
    } finally {
      setBusy(false);
    }
  }

  function showMoreAlternatives() {
    if (!alternativeSheet) return;
    const rejected = new Set(alternativeSheet.rejected);
    alternativeSheet.items.forEach((item) => rejected.add(item.signature));
    void showAlternatives(alternativeSheet.slotId, rejected, alternativeSheet.page + 1);
  }

  function chooseAlternative(item: AlternativeDto) {
    if (!alternativeSheet) return;
    const slotId = alternativeSheet.slotId;
    const previous = plan.slots.find((slot) => slot.id === slotId) ?? null;
    void run(
      () =>
        apiRequest(`/api/slots/${slotId}/choose`, {
          method: 'POST',
          body: JSON.stringify({ signature: item.signature, version: plan.version }),
        }),
      'Le repas a été remplacé.',
    ).then((succeeded) => {
      if (!succeeded) return;
      setAlternativeSheet(null);
      if (previous) setUndoSlot(previous);
    });
  }

  async function chooseCustom() {
    if (!alternativeSheet || !customLabel.trim()) return;
    const succeeded = await patchSlot(
      alternativeSheet.slotId,
      { slotType: 'custom', customLabel: customLabel.trim(), isLocked: true },
      'Votre idée est enregistrée et sera conservée.',
    );
    if (succeeded) {
      setCustomLabel('');
      setAlternativeSheet(null);
    }
  }

  function chooseManual(selection: object) {
    if (!alternativeSheet) return;
    const slotId = alternativeSheet.slotId;
    const previous = plan.slots.find((slot) => slot.id === slotId) ?? null;
    void run(
      () => apiRequest(`/api/slots/${slotId}/choose`, {
        method: 'POST',
        body: JSON.stringify({ ...selection, version: plan.version }),
      }),
      'Votre repas est choisi et sera conservé.',
    ).then((succeeded) => {
      if (!succeeded) return;
      setAlternativeSheet(null);
      if (previous) setUndoSlot(previous);
    });
  }

  async function chooseStarter(ingredientId?: string, recipeId?: string, automatic = false) {
    if (!starterSlotId) return;
    const previous = plan.slots.find((slot) => slot.id === starterSlotId) ?? null;
    const succeeded = await run(() => apiRequest(`/api/slots/${starterSlotId}/starter`, {
      method: 'PUT', body: JSON.stringify({ ingredientId, recipeId, automatic, version: plan.version }),
    }), 'Entrée mise à jour.');
    if (succeeded) { setStarterSlotId(null); if (previous) setUndoSlot(previous); }
  }

  async function undoLastSlotChange() {
    if (!undoSlot) return;
    const previous = undoSlot;
    setUndoSlot(null);
    setBusy(true);
    setError(null);
    try {
      const restored = await apiRequest<WeeklyPlanDto>(`/api/slots/${previous.id}/restore`, {
        method: 'POST',
        body: JSON.stringify({ state: previous.restoreState, version: plan.version }),
      });
      setPlan(restored);
      setMessage('La dernière modification a été annulée.');
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Impossible d’annuler.');
    } finally { setBusy(false); }
  }

  function confirmWeek() {
    void run(
      () => apiRequest(`/api/plans/${plan.id}/confirm`, { method: 'POST', body: JSON.stringify({ version: plan.version }) }),
      'La semaine est confirmée et ajoutée à l’historique.',
    );
  }

  function unconfirmWeek() {
    if (!confirm('Remettre cette semaine en modification ? Elle ne comptera plus dans l’historique tant qu’elle ne sera pas confirmée à nouveau.')) return;
    void run(
      () => apiRequest(`/api/plans/${plan.id}/unconfirm`, {
        method: 'POST',
        body: JSON.stringify({ version: plan.version }),
      }),
      'La confirmation a été annulée. Vous pouvez corriger la semaine.',
    );
  }

  function toggleFavorite() {
    void run(
      () => apiRequest(`/api/plans/${plan.id}/favorite`, { method: 'POST' }),
      plan.isFavorite ? 'Retirée des favorites.' : 'Semaine ajoutée aux favorites.',
    );
  }

  function markLeftovers(slot: MealSlotDto) {
    const source = [...plan.slots]
      .filter((item) => item.slotIndex < slot.slotIndex && item.assignment)
      .sort((first, second) => second.slotIndex - first.slotIndex)[0];
    if (!source) {
      setError('Choisissez d’abord un repas précédent pour prévoir ses restes.');
      return;
    }
    setActionSlot(null);
    void patchSlot(
      slot.id,
      { slotType: 'leftovers', leftoversFromSlotId: source.id },
      'Les restes sont prévus.',
    );
  }

  const days = Array.from({ length: 7 }, (_, dayIndex) => ({
    dayIndex,
    slots: plan.slots.filter((slot) => Math.floor(slot.slotIndex / 2) === dayIndex),
  }));
  const filledCount = plan.slots.filter((slot) => slot.slotType !== 'empty').length;

  return (
    <>
      <main className="pageShell weekPage" aria-busy={busy}>
        <header className="weekHeader">
          <div>
            <p className="eyebrow">Notre menu</p>
            <h1>Cette semaine</h1>
            <p className="weekRange">{formatWeekRange(plan.startDate)}</p>
          </div>
          <div className="familyAvatar" aria-label="Menu de la famille">
            <span aria-hidden="true">M</span>
          </div>
        </header>

        <nav className="weekSwitcher" aria-label="Changer de semaine">
          <Link href={`/?week=${previousWeek}`} prefetch={false} aria-label="Semaine précédente">←</Link>
          <span>{plan.status === 'confirmed' ? 'Semaine confirmée' : `${filledCount}/14 repas choisis`}</span>
          <Link href={`/?week=${nextWeek}`} prefetch={false} aria-label="Semaine suivante">→</Link>
        </nav>

        <section className="weekIntro" aria-labelledby="week-intro-title">
          <div>
            <p className="introKicker">{isDraft ? 'Menu en préparation' : 'Menu enregistré'}</p>
            <h2 id="week-intro-title">
              {isDraft ? 'Une semaine variée, sans prise de tête.' : 'Votre semaine est prête.'}
            </h2>
            <p>
              {isDraft
                ? 'Gardez vos idées préférées, puis régénérez seulement le reste.'
                : 'Elle compte désormais dans l’historique des repas de la famille.'}
            </p>
          </div>
          {isDraft ? (
            <button className="primaryButton" type="button" onClick={regenerate} disabled={busy}>
              <span aria-hidden="true">✦</span>
              {filledCount === 0 ? 'Préparer ma semaine' : 'Régénérer les repas libres'}
            </button>
          ) : (
            <div className="confirmedActions">
              <button className="primaryButton" type="button" onClick={toggleFavorite} disabled={busy}>
                <span aria-hidden="true">★</span>
                {plan.isFavorite ? 'Retirer des favorites' : 'Garder comme favorite'}
              </button>
              <button className="confirmedEditButton" type="button" onClick={unconfirmWeek} disabled={busy}>
                Annuler la confirmation
              </button>
            </div>
          )}
        </section>

        {!compositionSetupCompleted && <section className="setupBanner"><div><strong>Choisissez les aliments des assiettes automatiques</strong><p>Vos recettes restent disponibles. Tant que cette sélection n’est pas faite, seules les recettes alimentent les suggestions.</p></div><button className="secondaryButton" type="button" onClick={() => setShowCompositionSetup(true)}>Configurer</button></section>}

        {message && <p className="successSummary undoSummary" role="status"><span>{message}</span>{undoSlot && <button type="button" onClick={() => void undoLastSlotChange()}>Annuler</button>}</p>}
        {warnings.length > 0 && <div className="warningSummary" role="status">{warnings.map((warning, index) => <p key={`${index}:${warning}`}>{warning}</p>)}</div>}
        {error && <p className="errorSummary" role="alert">{error}</p>}

        <section className="daysList" aria-label="Repas de la semaine">
          {days.map(({ dayIndex, slots }) => {
            const day = formatDay(slots[0].date);
            return (
              <article className="dayCard" key={dayIndex}>
                <header className="dayHeader">
                  <h2>{day.weekday}</h2>
                  <time dateTime={slots[0].date}>{day.date}</time>
                </header>
                <div className="dayMeals">
                  {slots.map((slot) => {
                    const meal = displayMeal(slot, plan.slots);
                    const canAddStarter = slot.slotType !== 'empty' && slot.slotType !== 'leftovers' && slot.slotType !== 'eating_out';
                    const canKeep = isDraft && Boolean(slot.assignment);
                    return (
                      <section className="mealCard" key={slot.id}>
                        <div className="mealMeta">
                          <span className="mealTime">
                            {slot.mealTime === 'lunch' ? 'Déjeuner' : 'Dîner'}
                          </span>
                          <span>{meal.minutes ? `${meal.minutes} min · ` : ''}{slot.guestCount} pers.</span>
                        </div>
                        <h3>{meal.name}</h3>
                        <p>{meal.description}</p>
                        {slot.starter && <p className="starterSummary"><strong>Entrée :</strong> {slot.starter.name}</p>}
                        {plan.status !== 'archived' && (
                          <div className="mealActions">
                            <div className={`mealActionsRow${canAddStarter ? ' split' : ''}`}>
                            <button
                              className="secondaryButton"
                              type="button"
                              disabled={busy}
                              onClick={() => void showAlternatives(slot.id)}
                            >
                              {slot.slotType === 'empty' ? 'Choisir' : 'Modifier'}
                            </button>
                            {canAddStarter && <button className="secondaryButton" type="button" disabled={busy} onClick={() => setStarterSlotId(slot.id)}>{slot.starter ? 'Changer l’entrée' : 'Ajouter une entrée'}</button>}
                            </div>
                            <div className={`mealActionsRow${canKeep ? ' split' : ''}`}>
                            {canKeep && (
                            <button
                              className="lockButton"
                              data-locked={slot.isLocked}
                              type="button"
                              aria-pressed={slot.isLocked}
                              disabled={busy}
                              onClick={() =>
                                void patchSlot(
                                  slot.id,
                                  { isLocked: !slot.isLocked },
                                  slot.isLocked ? 'Repas déverrouillé.' : 'Ce repas sera conservé.',
                                )
                              }
                            >
                              <span aria-hidden="true">{slot.isLocked ? '●' : '○'}</span>
                              {slot.isLocked ? 'Gardé' : 'Garder'}
                            </button>
                            )}
                            <button className="moreButton" type="button" onClick={() => setActionSlot(slot)}>
                              Options
                            </button>
                            </div>
                          </div>
                        )}
                      </section>
                    );
                  })}
                </div>
              </article>
            );
          })}
        </section>

        {isDraft && filledCount === 14 && (
          <section className="confirmPanel">
            <div>
              <p className="eyebrow">Tout est prêt</p>
              <h2>Confirmer cette semaine ?</h2>
              <p>Elle alimentera l’historique et la liste de courses définitive.</p>
            </div>
            <button className="primaryButton" type="button" disabled={busy} onClick={confirmWeek}>
              Confirmer ma semaine
            </button>
          </section>
        )}
      </main>

      {alternativeSheet && (
        <div className="sheetBackdrop" onClick={(event) => { if (event.target === event.currentTarget) setAlternativeSheet(null); }}>
          <section className="alternativeSheet mealChooser" role="dialog" aria-modal="true" aria-labelledby="alternative-title">
            <div className="sheetHandle" aria-hidden="true" />
            <header className="sheetHeader">
              <div><p className="eyebrow">Choisir ce repas</p><h2 id="alternative-title">Qu’est-ce qui vous tente ?</h2></div>
              <button className="closeButton" type="button" aria-label="Fermer" autoFocus onClick={() => setAlternativeSheet(null)}>×</button>
            </header>
            <div className="chooserTabs" role="tablist" aria-label="Type de choix">
              {([['suggestions','Suggestions'],['recipes','Mes recettes'],['compose','Composer'],['custom','Idée libre']] as const).map(([key,label]) => <button key={key} type="button" role="tab" aria-selected={chooserTab === key} data-active={chooserTab === key} onClick={() => setChooserTab(key)}>{label}</button>)}
            </div>
            {chooserTab === 'suggestions' && <><div className="alternativeList">
              {alternativeSheet.items.length === 0 && <p className="emptyChooser">Aucune suggestion compatible. Vous pouvez choisir une recette, composer une assiette ou saisir votre idée.</p>}
              {alternativeSheet.items.map((item) => (
                <button className="alternativeCard" key={item.signature} type="button" onClick={() => chooseAlternative(item)}>
                  <span className="alternativeTitle">{item.name}</span>
                  <span className="alternativeDescription">{item.description}</span>
                  <span className="reasonList">{item.reasons.slice(0, 2).map((reason) => <span key={reason}>{reason}</span>)}</span>
                </button>
              ))}
            </div><button className="secondaryButton wideButton" type="button" onClick={showMoreAlternatives}>Voir trois autres idées</button></>}
            {chooserTab === 'recipes' && <div className="chooserPanel"><label className="fieldLabel">Rechercher une recette<input type="search" value={recipeSearch} onChange={(event) => setRecipeSearch(event.target.value)} placeholder="Ex. riz cantonais" /></label>{selectedRecipeId ? <div className="chooserPanel stackForm"><button className="secondaryButton" type="button" onClick={() => setSelectedRecipeId('')}>← Toutes les recettes</button><h3>{mainRecipes.find((item) => item.id === selectedRecipeId)?.name}</h3>{mainRecipes.find((item) => item.id === selectedRecipeId)?.allowStarchSide && <SearchChoice label="Féculent" options={[...starches.map((item) => ({ value: item.id, label: item.name })), ...starchRecipes.map((item) => ({ value: `r:${item.id}`, label: item.name }))]} value={starchId} onChange={setStarchId} query={starchSearch} onQuery={setStarchSearch} />}{mainRecipes.find((item) => item.id === selectedRecipeId)?.allowVegetableSide && <SearchChoice label="Légume" options={[...vegetables.map((item) => ({ value: item.id, label: item.name })), ...vegetableRecipes.map((item) => ({ value: `r:${item.id}`, label: item.name }))]} value={vegetableId} onChange={setVegetableId} query={vegetableSearch} onQuery={setVegetableSearch} />}<button className="primaryButton" type="button" disabled={!starchId && !vegetableId} onClick={() => chooseManual({ recipeId: selectedRecipeId, ...sideSelection() })}>Utiliser ce plat</button></div> : <div className="alternativeList">{mainRecipes.filter((recipe) => recipe.name.toLocaleLowerCase('fr-FR').includes(recipeSearch.toLocaleLowerCase('fr-FR'))).map((recipe) => <button className="alternativeCard" type="button" key={recipe.id} onClick={() => { if (recipe.allowStarchSide || recipe.allowVegetableSide) { setStarchId(''); setVegetableId(''); setSelectedRecipeId(recipe.id); } else chooseManual({ recipeId: recipe.id }); }}><span className="alternativeTitle">{recipe.name}</span><span className="alternativeDescription">{recipe.variantOfId ? `Variante de ${recipes.find((item) => item.id === recipe.variantOfId)?.name ?? 'une recette'} · ` : ''}{recipe.allowStarchSide || recipe.allowVegetableSide ? 'À accompagner' : `${recipe.ingredientCount} produit(s)`}</span></button>)}</div>}</div>}
            {chooserTab === 'compose' && <div className="chooserPanel stackForm"><SearchChoice label="Protéine" options={proteins.map((item) => ({ value: item.id, label: item.name }))} value={proteinId} onChange={setProteinId} query={proteinSearch} onQuery={setProteinSearch} required /><SearchChoice label="Féculent" options={[...starches.map((item) => ({ value: item.id, label: item.name })), ...starchRecipes.map((item) => ({ value: `r:${item.id}`, label: item.name }))]} value={starchId} onChange={setStarchId} query={starchSearch} onQuery={setStarchSearch} /><SearchChoice label="Légume" options={[...vegetables.map((item) => ({ value: item.id, label: item.name })), ...vegetableRecipes.map((item) => ({ value: `r:${item.id}`, label: item.name }))]} value={vegetableId} onChange={setVegetableId} query={vegetableSearch} onQuery={setVegetableSearch} /><p className="fieldHint">Les courses utilisent les portions des produits et des recettes choisis.</p><button className="primaryButton" type="button" disabled={!proteinId || (!starchId && !vegetableId)} onClick={() => chooseManual({ proteinId, ...sideSelection() })}>Utiliser cette assiette</button></div>}
            {chooserTab === 'custom' && <div className="chooserPanel stackForm"><label>Nom du repas<input value={customLabel} onChange={(event) => setCustomLabel(event.target.value)} placeholder="Ex. Croque-monsieur" /></label><p className="fieldHint">Les ingrédients ne seront pas ajoutés automatiquement aux courses. Vous pourrez les ajouter depuis la liste de courses.</p><button className="primaryButton" type="button" disabled={!customLabel.trim()} onClick={() => void chooseCustom()}>Utiliser cette idée</button></div>}
          </section>
        </div>
      )}

      {starterSlotId && (
        <div className="sheetBackdrop" onClick={(event) => { if (event.target === event.currentTarget) setStarterSlotId(null); }}>
          <section className="alternativeSheet mealChooser" role="dialog" aria-modal="true" aria-labelledby="starter-title">
            <div className="sheetHandle" aria-hidden="true" />
            <header className="sheetHeader"><h2 id="starter-title">Choisir une entrée</h2><button className="closeButton" type="button" aria-label="Fermer" onClick={() => setStarterSlotId(null)}>×</button></header>
            <label className="fieldLabel">Rechercher une entrée<input type="search" value={starterSearch} onChange={(event) => setStarterSearch(event.target.value)} placeholder="Asperges, cœurs de palmiers…" /></label>
            <div className="alternativeList">
              {[...starterIngredients.map((item) => ({ id: item.id, name: item.name, kind: 'ingredient' as const })), ...starterRecipes.map((item) => ({ id: item.id, name: item.name, kind: 'recipe' as const }))].filter((item) => item.name.toLocaleLowerCase('fr-FR').includes(starterSearch.toLocaleLowerCase('fr-FR'))).map((item) => <button className="alternativeCard" type="button" key={`${item.kind}:${item.id}`} onClick={() => void chooseStarter(item.kind === 'ingredient' ? item.id : undefined, item.kind === 'recipe' ? item.id : undefined)}>{item.name}</button>)}
            </div>
            <div className="formButtonRow"><button className="secondaryButton" type="button" onClick={() => void chooseStarter()}>Sans entrée</button><button className="secondaryButton" type="button" onClick={() => void chooseStarter(undefined, undefined, true)}>Laisser proposer à la prochaine génération</button></div>
          </section>
        </div>
      )}

      {actionSlot && (
        <div className="sheetBackdrop" onClick={(event) => { if (event.target === event.currentTarget) setActionSlot(null); }}>
          <section className="alternativeSheet actionSheet" role="dialog" aria-modal="true" aria-labelledby="action-title">
            <div className="sheetHandle" aria-hidden="true" />
            <header className="sheetHeader">
              <div><p className="eyebrow">Organiser ce repas</p><h2 id="action-title">Autres options</h2></div>
              <button className="closeButton" type="button" aria-label="Fermer" autoFocus onClick={() => setActionSlot(null)}>×</button>
            </header>
            <label className="fieldLabel">
              Nombre de personnes
              <input
                type="number"
                min="1"
                max="30"
                defaultValue={actionSlot.guestCount}
                onBlur={(event) => {
                  const guestCount = Number(event.currentTarget.value);
                  if (guestCount !== actionSlot.guestCount) {
                    void patchSlot(actionSlot.id, { guestCount }, 'Nombre de personnes modifié.');
                  }
                }}
              />
            </label>
            <div className="specialActionGrid">
              <button type="button" onClick={() => markLeftovers(actionSlot)}>Prévoir des restes</button>
              <button type="button" onClick={() => { setActionSlot(null); void patchSlot(actionSlot.id, { slotType: 'eating_out', customLabel: 'Repas à l’extérieur' }, 'Repas extérieur enregistré.'); }}>Repas à l’extérieur</button>
              {isDraft && <button type="button" onClick={() => { setActionSlot(null); void patchSlot(actionSlot.id, { slotType: 'empty', isLocked: false }, 'Le créneau est de nouveau vide.'); }}>Vider ce créneau</button>}
            </div>
          </section>
        </div>
      )}

      {showOnboarding && <Onboarding onDone={() => setShowOnboarding(false)} />}
      {showCompositionSetup && !showOnboarding && <CompositionSetup ingredients={ingredients} onCancel={() => setShowCompositionSetup(false)} onDone={() => { setCompositionSetupCompleted(true); setShowCompositionSetup(false); }} />}
    </>
  );
}

function SearchChoice({ label, options, value, onChange, query, onQuery, required = false }: {
  label: string; options: { value: string; label: string }[]; value: string;
  onChange: (value: string) => void; query: string; onQuery: (value: string) => void; required?: boolean;
}) {
  const filtered = options.filter((item) => item.value === value || item.label.toLocaleLowerCase('fr-FR').includes(query.toLocaleLowerCase('fr-FR')));
  return <div className="searchChoice"><label className="fieldLabel">{label}<input type="search" value={query} onChange={(event) => onQuery(event.target.value)} placeholder={`Rechercher ${label.toLocaleLowerCase('fr-FR')}…`} /></label><select aria-label={label} value={value} onChange={(event) => onChange(event.target.value)}><option value="">{required ? 'Choisir…' : 'Aucun'}</option>{filtered.map((item) => <option value={item.value} key={item.value}>{item.label}</option>)}</select></div>;
}

function CompositionSetup({ ingredients, onDone, onCancel }: { ingredients: IngredientDto[]; onDone: () => void; onCancel: () => void }) {
  const eligible = ingredients.filter((item) => ['PROTEIN', 'STARCH', 'VEGETABLE'].includes(item.category));
  const [search, setSearch] = useState('');
  const [selected, setSelected] = useState(() => new Set(eligible.filter((item) => item.useInComposedMeals).map((item) => item.id)));
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const groups = [
    ['PROTEIN', 'Protéines'],
    ['STARCH', 'Féculents'],
    ['VEGETABLE', 'Légumes'],
  ] as const;
  async function save() {
    setBusy(true);
    setError('');
    try {
      await apiRequest('/api/settings/compositions', {
        method: 'POST',
        body: JSON.stringify({ ingredientIds: [...selected] }),
      });
      onDone();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Impossible d’enregistrer la sélection.');
    } finally {
      setBusy(false);
    }
  }
  return <div className="onboardingBackdrop"><section className="onboardingCard compositionSetup" role="dialog" aria-modal="true" aria-labelledby="composition-title">
    <p className="eyebrow">Assiettes automatiques</p>
    <h2 id="composition-title">Quels aliments peut-on associer ?</h2>
    <p>Cochez uniquement les aliments qui peuvent constituer un repas. Un ingrédient non coché reste disponible dans vos recettes et vos courses.</p>
    {error && <p className="errorSummary" role="alert">{error}</p>}
    <label className="fieldLabel">Rechercher un produit<input type="search" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Ex. poulet, riz…" /></label>
    <div className="setupGroups">{groups.map(([category, label]) => <fieldset key={category}><legend>{label}</legend><div className="setupChoices">{eligible.filter((item) => item.category === category && item.name.toLocaleLowerCase('fr-FR').includes(search.toLocaleLowerCase('fr-FR'))).map((item) => <label className="checkboxRow" key={item.id}><input type="checkbox" checked={selected.has(item.id)} onChange={(event) => setSelected((current) => { const next = new Set(current); if (event.target.checked) next.add(item.id); else next.delete(item.id); return next; })} /><span>{item.name}</span></label>)}</div></fieldset>)}</div>
    <p className="fieldHint">Vous pourrez modifier ce réglage plus tard depuis chaque ingrédient.</p>
    <div className="setupActions"><button className="secondaryButton" type="button" disabled={busy} onClick={onCancel}>Plus tard</button><button className="primaryButton" type="button" disabled={busy} onClick={() => void save()}>{busy ? 'Enregistrement…' : 'Enregistrer ma sélection'}</button></div>
  </section></div>;
}

function Onboarding({ onDone }: { onDone: () => void }) {
  const [step, setStep] = useState(0);
  const [weekdayGuests, setWeekdayGuests] = useState(4);
  const [weekendGuests, setWeekendGuests] = useState(4);
  const [busy, setBusy] = useState(false);
  async function finish() {
    setBusy(true);
    try {
      await apiRequest('/api/settings', {
        method: 'PATCH',
        body: JSON.stringify({
          defaultGuestsLunchWeekday: weekdayGuests,
          defaultGuestsDinnerWeekday: weekdayGuests,
          defaultGuestsLunchWeekend: weekendGuests,
          defaultGuestsDinnerWeekend: weekendGuests,
          onboardingCompleted: true,
        }),
      });
      onDone();
    } finally { setBusy(false); }
  }
  return <div className="onboardingBackdrop"><section className="onboardingCard" role="dialog" aria-modal="true" aria-labelledby="onboarding-title">
    <div className="progressDots" aria-label={`Étape ${step + 1} sur 3`}>{[0,1,2].map((item) => <span data-active={item <= step} key={item} />)}</div>
    {step === 0 && <><span className="loginMark" aria-hidden="true">M</span><p className="eyebrow">Bienvenue</p><h2 id="onboarding-title">Vos menus, sans prise de tête</h2><p>Quelques secondes suffisent pour adapter les propositions à votre foyer.</p><button className="primaryButton" onClick={() => setStep(1)}>Commencer</button></>}
    {step === 1 && <><p className="eyebrow">Votre foyer</p><h2 id="onboarding-title">Combien serez-vous à table ?</h2><p>Ce sont des valeurs habituelles. Chaque repas pourra être ajusté séparément.</p><div className="formColumns"><label>En semaine<input type="number" min="1" max="30" value={weekdayGuests} onChange={(event) => setWeekdayGuests(Number(event.target.value))} /></label><label>Le week-end<input type="number" min="1" max="30" value={weekendGuests} onChange={(event) => setWeekendGuests(Number(event.target.value))} /></label></div><button className="primaryButton" onClick={() => setStep(2)}>Continuer</button></>}
    {step === 2 && <><p className="eyebrow">Tout est prêt</p><h2 id="onboarding-title">L’application s’occupe du reste</h2><p>Des aliments d’exemple sont déjà disponibles. Touchez « Préparer ma semaine », gardez les repas qui vous plaisent et remplacez les autres. Vous pourrez ajouter vos recettes à tout moment.</p><button className="primaryButton" disabled={busy} onClick={() => void finish()}>{busy ? 'Préparation…' : 'Préparer ma première semaine'}</button></>}
  </section></div>;
}
