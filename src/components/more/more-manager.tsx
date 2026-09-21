'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import type { FormEvent } from 'react';
import type { IngredientDto, WeeklyPlanDto } from '@/types/api';
import { addDays, formatWeekRange, mondayOfCurrentWeek } from '@/lib/week';

type Settings = { defaultGuestsLunchWeekday: number; defaultGuestsDinnerWeekday: number; defaultGuestsLunchWeekend: number; defaultGuestsDinnerWeekend: number };
type Incompatibility = { id: string; firstId: string; firstName: string; secondId: string; secondName: string };

export function MoreManager({ plans, ingredients, incompatibilities: initialIncompatibilities, initialSettings, calendarUrl }: { plans: WeeklyPlanDto[]; ingredients: IngredientDto[]; incompatibilities: Incompatibility[]; initialSettings: Settings; calendarUrl: string }) {
  const router = useRouter();
  const [section, setSection] = useState<'history' | 'settings' | 'rules'>('history');
  const [incompatibilities, setIncompatibilities] = useState(initialIncompatibilities);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const confirmed = plans.filter((plan) => plan.status === 'confirmed');
  async function reapply(planId: string) {
    const suggested = addDays(mondayOfCurrentWeek(), 7);
    const target = prompt('Lundi de la semaine à préparer (AAAA-MM-JJ)', suggested);
    if (!target) return;
    const response = await fetch(`/api/plans/${planId}/reapply`, { method: 'POST', headers: { 'content-type': 'application/json' }, body: JSON.stringify({ targetStartDate: target }) });
    if (response.ok) router.push(`/?week=${target}`);
    else setError((await response.json()).error ?? 'La semaine n’a pas pu être copiée.');
  }
  return <main className="pageShell catalogPage">
    <header className="sectionHeader"><div><p className="eyebrow">Votre famille</p><h1>Plus</h1><p>Historique, règles simples et nombres de personnes habituels.</p></div><span className="familyAvatar" aria-hidden="true">•••</span></header>
    {message && <p className="successSummary" role="status">{message}</p>}{error && <p className="errorSummary" role="alert">{error}</p>}
    <div className="segmented three" role="tablist"><button data-active={section === 'history'} onClick={() => setSection('history')}>Historique</button><button data-active={section === 'settings'} onClick={() => setSection('settings')}>Foyer</button><button data-active={section === 'rules'} onClick={() => setSection('rules')}>À éviter</button></div>
    {section === 'history' && <section>
      <DataExportBox />
      <CalendarSyncBox calendarUrl={calendarUrl} hasConfirmed={confirmed.length > 0} />
      {confirmed.length === 0 ? <div className="emptyCard"><h2>Aucune semaine confirmée</h2><p>Une fois votre première semaine confirmée, elle restera ici.</p></div> : <div className="catalogList">{confirmed.map((plan) => <article className="catalogCard historyCard" key={plan.id}><div><h2>{formatWeekRange(plan.startDate)}</h2><p>{plan.isFavorite ? '♥ Semaine favorite · ' : ''}14 repas conservés</p></div><div className="cardActions"><Link href={`/?week=${plan.startDate}`}>Voir</Link><button onClick={() => reapply(plan.id)}>Réutiliser</button></div></article>)}</div>}
    </section>}
    {section === 'settings' && <><SettingsForm value={initialSettings} onMessage={setMessage} onError={setError} /><button className="secondaryButton logoutButton" onClick={async () => { await fetch('/api/auth/logout', { method: 'POST' }); router.push('/connexion'); router.refresh(); }}>Se déconnecter de cet appareil</button></>}
    {section === 'rules' && <RulesEditor ingredients={ingredients} value={incompatibilities} onChange={setIncompatibilities} onMessage={setMessage} onError={setError} />}
  </main>;
}

function DataExportBox() {
  return <section className="settingsCard dataExportBox" aria-labelledby="data-export-title">
    <div><h2 id="data-export-title">Exporter les données du menu</h2><p>Téléchargez un fichier JSON avec les ingrédients, recettes, règles et menus. Il ne contient aucun mot de passe ni secret Coolify.</p></div>
    <a className="primaryButton" href="/api/catalog/export" download>Télécharger le catalogue</a>
  </section>;
}

function SettingsForm({ value, onMessage, onError }: { value: Settings; onMessage: (value: string) => void; onError: (value: string) => void }) {
  const [busy, setBusy] = useState(false);
  async function submit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); setBusy(true); const data = new FormData(event.currentTarget); const body = Object.fromEntries(['defaultGuestsLunchWeekday','defaultGuestsDinnerWeekday','defaultGuestsLunchWeekend','defaultGuestsDinnerWeekend'].map((key) => [key, Number(data.get(key))])); const response = await fetch('/api/settings', { method: 'PATCH', headers: { 'content-type': 'application/json' }, body: JSON.stringify(body) }); if (response.ok) onMessage('Les habitudes du foyer sont enregistrées. Elles s’appliqueront aux nouvelles semaines.'); else onError('Impossible d’enregistrer les réglages.'); setBusy(false); }
  return <form className="settingsCard stackForm" onSubmit={submit}><div><h2>Nombre de personnes habituel</h2><p>Vous pourrez toujours le changer sur un repas précis.</p></div><div className="formColumns"><label>Déjeuner en semaine<input name="defaultGuestsLunchWeekday" type="number" min="1" max="30" defaultValue={value.defaultGuestsLunchWeekday} /></label><label>Dîner en semaine<input name="defaultGuestsDinnerWeekday" type="number" min="1" max="30" defaultValue={value.defaultGuestsDinnerWeekday} /></label><label>Déjeuner le week-end<input name="defaultGuestsLunchWeekend" type="number" min="1" max="30" defaultValue={value.defaultGuestsLunchWeekend} /></label><label>Dîner le week-end<input name="defaultGuestsDinnerWeekend" type="number" min="1" max="30" defaultValue={value.defaultGuestsDinnerWeekend} /></label></div><button className="primaryButton" disabled={busy}>{busy ? 'Enregistrement…' : 'Enregistrer mes habitudes'}</button></form>;
}

function RulesEditor({ ingredients, value, onChange, onMessage, onError }: { ingredients: IngredientDto[]; value: Incompatibility[]; onChange: (items: Incompatibility[]) => void; onMessage: (value: string) => void; onError: (value: string) => void }) {
  const [firstId, setFirstId] = useState(ingredients[0]?.id ?? ''); const [secondId, setSecondId] = useState(ingredients[1]?.id ?? '');
  async function add(event: FormEvent) { event.preventDefault(); const response = await fetch('/api/incompatibilities', { method: 'POST', headers: { 'content-type': 'application/json' }, body: JSON.stringify({ firstId, secondId }) }); if (!response.ok) return onError((await response.json()).error ?? 'Impossible d’ajouter cette règle.'); const item = await response.json(); const first = ingredients.find((ingredient) => ingredient.id === firstId); const second = ingredients.find((ingredient) => ingredient.id === secondId); if (!value.some((rule) => rule.id === item.id)) onChange([...value, { id: item.id, firstId, firstName: first?.name ?? '', secondId, secondName: second?.name ?? '' }]); onMessage('Cette association ne sera plus proposée.'); }
  async function remove(id: string) { const response = await fetch(`/api/incompatibilities/${id}`, { method: 'DELETE' }); if (response.ok) onChange(value.filter((item) => item.id !== id)); else onError('Impossible de retirer cette règle.'); }
  return <section><form className="settingsCard stackForm" onSubmit={add}><div><h2>Associations à éviter</h2><p>Par exemple, si deux aliments ne vont jamais bien ensemble chez vous.</p></div><div className="formColumns"><label>Premier aliment<select value={firstId} onChange={(event) => setFirstId(event.target.value)}>{ingredients.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label><label>Deuxième aliment<select value={secondId} onChange={(event) => setSecondId(event.target.value)}>{ingredients.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label></div><button className="primaryButton" disabled={!firstId || !secondId || firstId === secondId}>Ajouter cette règle</button></form><div className="catalogList">{value.map((item) => <article className="catalogCard" key={item.id}><div><h2>{item.firstName} + {item.secondName}</h2><p>Cette association ne sera pas générée.</p></div><button className="textDanger" onClick={() => remove(item.id)}>Retirer</button></article>)}</div></section>;
}

function CalendarSyncBox({ calendarUrl, hasConfirmed }: { calendarUrl: string; hasConfirmed: boolean }) {
  const [copied, setCopied] = useState(false);

  function getFullCalendarUrl() {
    if (typeof window === 'undefined') return calendarUrl;
    return `${window.location.protocol}//${window.location.host}${calendarUrl}`;
  }

  function handleAppleCalendarClick(event: React.MouseEvent<HTMLAnchorElement>) {
    if (typeof window !== 'undefined') {
      event.preventDefault();
      const webcal = `webcal://${window.location.host}${calendarUrl}`;
      window.location.href = webcal;
    }
  }

  async function copyLink() {
    const url = getFullCalendarUrl();
    try {
      await navigator.clipboard.writeText(url);
      setCopied(true);
      setTimeout(() => setCopied(false), 3000);
    } catch {
      prompt('Lien d’abonnement calendrier :', url);
    }
  }

  return (
    <div className="calendarBox">
      <div>
        <h3>Synchronisation Calendrier</h3>
        <p>
          Abonnez votre agenda pour voir les repas de la famille se synchroniser automatiquement.
        </p>
      </div>
      <div className="calendarActions">
        <a className="secondaryButton" href={calendarUrl} onClick={handleAppleCalendarClick}>
          S’abonner sur Apple Calendrier (iPhone / Mac)
        </a>
        <button type="button" className="secondaryButton" onClick={copyLink}>
          {copied ? '✓ Lien copié !' : 'Copier l’URL (Google Agenda / Outlook)'}
        </button>
      </div>
      {!hasConfirmed && (
        <p className="calendarHint">
          💡 <strong>Astuce :</strong> Aucune semaine n’est encore confirmée. Dès que vous cliquerez sur <em>« Confirmer la semaine »</em> dans l’onglet Menu, vos 14 repas apparaîtront automatiquement dans votre agenda.
        </p>
      )}
    </div>
  );
}
