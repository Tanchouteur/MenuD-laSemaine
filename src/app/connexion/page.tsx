import type { Metadata } from 'next';
import { LoginForm } from '@/components/auth/login-form';

export const metadata: Metadata = { title: 'Connexion' };

export default async function LoginPage({ searchParams }: { searchParams: Promise<Record<string, string | string[] | undefined>> }) {
  const rawNext = (await searchParams).next;
  const next = typeof rawNext === 'string' && rawNext.startsWith('/') && !rawNext.startsWith('//') ? rawNext : '/';
  return <main className="loginPage"><section className="loginCard"><span className="loginMark" aria-hidden="true">M</span><p className="eyebrow">Espace familial</p><h1>Bienvenue à table</h1><p>Entrez le mot de passe partagé par votre famille.</p><LoginForm next={next} /></section></main>;
}
