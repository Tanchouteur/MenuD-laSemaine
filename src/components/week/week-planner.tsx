'use client';

import Link from 'next/link';
import { useState } from 'react';
import { formatDay, formatWeekRange } from '@/lib/week';
import type { MealSlotDto, WeeklyPlanDto } from '@/types/api';

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
}: WeekPlannerProps) {
  const [plan, setPlan] = useState(initialPlan);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [alternativeSheet, setAlternativeSheet] = useState<AlternativeSheet | null>(null);
  const [actionSlot, setActionSlot] = useState<MealSlotDto | null>(null);
  const [customLabel, setCustomLabel] = useState('');
  const [showOnboarding, setShowOnboarding] = useState(!initialOnboardingCompleted);
  const [undoSlot, setUndoSlot] = useState<MealSlotDto | null>(null);
  const isDraft = plan.status === 'draft';

  async function run(action: () => Promise<WeeklyPlanDto>, success: string) {
    setBusy(true);
    setError(null);
    try {
      setPlan(await action());
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
  }

  async function showAlternatives(
    slotId: string,
    rejected = new Set<string>(),
    page = 0,
  ) {
    setBusy(true);
    setError(null);
    try {
      const items = await apiRequest<AlternativeDto[]>(`/api/slots/${slotId}/alternatives`, {
        method: 'POST',
        body: JSON.stringify({
          rejectedSignatures: [...rejected],
          seed: `${slotId}:page-${page}:rejected-${rejected.size}`,
        }),
      });
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
    setAlternativeSheet(null);
    void run(
      () =>
        apiRequest(`/api/slots/${slotId}/choose`, {
          method: 'POST',
          body: JSON.stringify({ signature: item.signature, version: plan.version }),
        }),
      'Le repas a été remplacé.',
    ).then((succeeded) => { if (succeeded && previous) setUndoSlot(previous); });
  }

  async function undoLastSlotChange() {
    if (!undoSlot) return;
    const previous = undoSlot;
    setUndoSlot(null);
    setBusy(true);
    setError(null);
    try {
      let restored: WeeklyPlanDto;
      if (previous.assignment) {
        restored = await apiRequest(`/api/slots/${previous.id}/choose`, { method: 'POST', body: JSON.stringify({ signature: previous.assignment.signature }) });
      } else {
        restored = await apiRequest(`/api/slots/${previous.id}`, { method: 'PATCH', body: JSON.stringify({ slotType: previous.slotType, customLabel: previous.customLabel, leftoversFromSlotId: previous.leftoversFromSlotId }) });
      }
      restored = await apiRequest(`/api/slots/${previous.id}`, { method: 'PATCH', body: JSON.stringify({ isLocked: previous.isLocked, guestCount: previous.guestCount }) });
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

        {message && <p className="successSummary undoSummary" role="status"><span>{message}</span>{undoSlot && isDraft && <button type="button" onClick={() => void undoLastSlotChange()}>Annuler</button>}</p>}
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
                        {isDraft && (
                          <div className="mealActions threeActions">
                            <button
                              className="secondaryButton"
                              type="button"
                              disabled={busy || !slot.assignment}
                              onClick={() => void showAlternatives(slot.id)}
                            >
                              Remplacer
                            </button>
                            <button
                              className="lockButton"
                              data-locked={slot.isLocked}
                              type="button"
                              aria-pressed={slot.isLocked}
                              disabled={busy || !slot.assignment}
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
                            <button className="moreButton" type="button" onClick={() => setActionSlot(slot)}>
                              Options
                            </button>
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
        <div className="sheetBackdrop">
          <section className="alternativeSheet" role="dialog" aria-modal="true" aria-labelledby="alternative-title">
            <div className="sheetHandle" aria-hidden="true" />
            <header className="sheetHeader">
              <div><p className="eyebrow">Autres idées</p><h2 id="alternative-title">Quel repas vous tente ?</h2></div>
              <button className="closeButton" type="button" aria-label="Fermer" autoFocus onClick={() => setAlternativeSheet(null)}>×</button>
            </header>
            <div className="alternativeList">
              {alternativeSheet.items.map((item) => (
                <button className="alternativeCard" key={item.signature} type="button" onClick={() => chooseAlternative(item)}>
                  <span className="alternativeTitle">{item.name}</span>
                  <span className="alternativeDescription">{item.description}</span>
                  <span className="reasonList">{item.reasons.slice(0, 2).map((reason) => <span key={reason}>{reason}</span>)}</span>
                </button>
              ))}
            </div>
            <button className="secondaryButton wideButton" type="button" onClick={showMoreAlternatives}>Voir trois autres idées</button>
          </section>
        </div>
      )}

      {actionSlot && (
        <div className="sheetBackdrop">
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
            </div>
            <label className="fieldLabel">
              Choix libre
              <input value={customLabel} onChange={(event) => setCustomLabel(event.target.value)} placeholder="Ex. Croque-monsieur" />
            </label>
            <button className="primaryButton" type="button" disabled={!customLabel.trim()} onClick={() => { const label = customLabel; setCustomLabel(''); setActionSlot(null); void patchSlot(actionSlot.id, { slotType: 'custom', customLabel: label }, 'Repas libre enregistré.'); }}>Utiliser ce choix</button>
          </section>
        </div>
      )}

      {showOnboarding && <Onboarding onDone={() => setShowOnboarding(false)} />}
    </>
  );
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
