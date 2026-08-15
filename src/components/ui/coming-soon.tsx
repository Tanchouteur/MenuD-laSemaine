import Link from 'next/link';

type ComingSoonProps = {
  eyebrow: string;
  title: string;
  description: string;
  icon: string;
};

export function ComingSoon({
  eyebrow,
  title,
  description,
  icon,
}: ComingSoonProps) {
  return (
    <main className="pageShell placeholderPage">
      <p className="eyebrow">{eyebrow}</p>
      <div className="placeholderIcon" aria-hidden="true">
        {icon}
      </div>
      <h1>{title}</h1>
      <p>{description}</p>
      <Link className="primaryButton linkButton" href="/">
        Revenir à ma semaine
      </Link>
    </main>
  );
}
