'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

const items = [
  { href: '/', label: 'Semaine', icon: '▦' },
  { href: '/courses', label: 'Courses', icon: '✓' },
  { href: '/recettes', label: 'Recettes', icon: '⌕' },
  { href: '/plus', label: 'Plus', icon: '•••' },
];

export function BottomNavigation() {
  const pathname = usePathname();

  if (pathname === '/connexion') return null;

  return (
    <nav className="bottomNav" aria-label="Navigation principale">
      {items.map((item) => {
        const isActive =
          item.href === '/' ? pathname === '/' : pathname.startsWith(item.href);

        return (
          <Link
            className="bottomNavItem"
            data-active={isActive}
            href={item.href}
            key={item.href}
            aria-current={isActive ? 'page' : undefined}
          >
            <span className="bottomNavIcon" aria-hidden="true">
              {item.icon}
            </span>
            <span>{item.label}</span>
          </Link>
        );
      })}
    </nav>
  );
}
