'use client';

import { useMemo, useState } from 'react';
import type { FormEvent } from 'react';
import type { ShoppingEntryDto } from '@/types/api';
import { formatWeekRange } from '@/lib/week';

const units: Record<string, string> = {
  GRAM: 'g', KILOGRAM: 'kg', MILLILITER: 'ml', CENTILITER: 'cl',
  LITER: 'L', PIECE: 'pièce(s)', SLICE: 'tranche(s)', CAN: 'boîte(s)',
};

export function ShoppingList({ planId, startDate, initialEntries }: { planId: string; startDate: string; initialEntries: ShoppingEntryDto[] }) {
  const [entries, setEntries] = useState(initialEntries);
  const [hideChecked, setHideChecked] = useState(false);
  const [label, setLabel] = useState('');
  const [busy, setBusy] = useState<string | null>(null);
  const [error, setError] = useState('');
  const groups = useMemo(() => {
    const map = new Map<string, ShoppingEntryDto[]>();
    for (const entry of entries.filter((item) => !hideChecked || !item.isChecked)) {
      map.set(entry.aisleName, [...(map.get(entry.aisleName) ?? []), entry]);
    }
    return [...map.entries()];
  }, [entries, hideChecked]);

  async function toggle(entry: ShoppingEntryDto) {
    setBusy(entry.id);
    setEntries((current) => current.map((item) => item.id === entry.id ? { ...item, isChecked: !item.isChecked } : item));
    const response = await fetch(`/api/shopping/${entry.id}`, { method: 'PATCH' });
    if (!response.ok) {
      setEntries((current) => current.map((item) => item.id === entry.id ? entry : item));
      setError('Impossible de conserver cette modification.');
    }
    setBusy(null);
  }

  async function add(event: FormEvent) {
    event.preventDefault();
    if (!label.trim()) return;
    setBusy('add');
    const response = await fetch('/api/shopping', {
      method: 'POST', headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ planId, label }),
    });
    if (response.ok) { setEntries(await response.json()); setLabel(''); }
    else setError('Impossible d’ajouter cet article.');
    setBusy(null);
  }

  async function uncheckAll() {
    if (!confirm('Tout décocher et recommencer la liste ?')) return;
    setBusy('all');
    const response = await fetch('/api/shopping', { method: 'PATCH', headers: { 'content-type': 'application/json' }, body: JSON.stringify({ planId }) });
    if (response.ok) setEntries(await response.json()); else setError('Impossible de tout décocher.');
    setBusy(null);
  }

  async function removeManual(entry: ShoppingEntryDto) {
    const response = await fetch(`/api/shopping/${entry.id}`, { method: 'DELETE' });
    if (response.ok) setEntries((items) => items.filter((item) => item.id !== entry.id)); else setError('Impossible de retirer cet article.');
  }

  return (
    <main className="pageShell catalogPage">
      <header className="sectionHeader">
        <div><p className="eyebrow">{formatWeekRange(startDate)}</p><h1>Courses</h1><p>La liste se met à jour avec les repas confirmés.</p></div>
        <span className="familyAvatar" aria-hidden="true">✓</span>
      </header>
      {error && <p className="errorSummary" role="alert">{error}</p>}
      <div className="toolbarCard">
        <span>{entries.filter((item) => item.isChecked).length} sur {entries.length} cochés</span>
        <div className="toolbarActions"><label className="switchLabel"><input type="checkbox" checked={hideChecked} onChange={(event) => setHideChecked(event.target.checked)} /> Masquer les cochés</label>{entries.some((item) => item.isChecked) && <button type="button" onClick={() => void uncheckAll()}>Tout décocher</button>}</div>
      </div>
      <form className="inlineForm" onSubmit={add}>
        <label className="srOnly" htmlFor="manual-item">Ajouter un article</label>
        <input id="manual-item" value={label} onChange={(event) => setLabel(event.target.value)} placeholder="Ajouter un article…" />
        <button className="primaryButton" disabled={busy === 'add'}>Ajouter</button>
      </form>
      {groups.length === 0 ? <div className="emptyCard"><h2>Tout est prêt</h2><p>{entries.length ? 'Tous les articles sont cochés.' : 'Préparez d’abord les repas de la semaine.'}</p></div> : groups.map(([aisle, items]) => (
        <section className="shoppingGroup" key={aisle}>
          <h2>{aisle}</h2>
          {items.map((entry) => (
            <label className="shoppingItem" data-checked={entry.isChecked} key={entry.id}>
              <input type="checkbox" checked={entry.isChecked} disabled={busy === entry.id} onChange={() => toggle(entry)} />
              <span><strong>{entry.label}</strong>{entry.quantity !== null && <small>{Number(entry.quantity.toFixed(2))} {entry.unit ? units[entry.unit] ?? entry.unit : ''}{entry.sourceCount ? ` · ${entry.sourceCount} repas` : ''}</small>}</span>
              {entry.isManual && <button className="removeItem" type="button" aria-label={`Retirer ${entry.label}`} onClick={(event) => { event.preventDefault(); void removeManual(entry); }}>×</button>}
            </label>
          ))}
        </section>
      ))}
    </main>
  );
}
