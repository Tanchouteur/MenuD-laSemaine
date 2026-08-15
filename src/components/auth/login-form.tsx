'use client';

import { useState } from 'react';
import type { FormEvent } from 'react';

export function LoginForm({ next }: { next: string }) {
  const [error, setError] = useState(''); const [busy, setBusy] = useState(false);
  async function submit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); setBusy(true); setError(''); const data = new FormData(event.currentTarget); const response = await fetch('/api/auth/login', { method: 'POST', headers: { 'content-type': 'application/json' }, body: JSON.stringify({ password: data.get('password') }) }); if (response.ok) window.location.href = next; else { setError((await response.json()).error ?? 'Connexion impossible.'); setBusy(false); } }
  return <form className="stackForm loginForm" onSubmit={submit}><label>Mot de passe familial<input name="password" type="password" autoComplete="current-password" autoFocus required /></label>{error && <p className="errorSummary" role="alert">{error}</p>}<button className="primaryButton" disabled={busy}>{busy ? 'Connexion…' : 'Ouvrir les menus'}</button></form>;
}
