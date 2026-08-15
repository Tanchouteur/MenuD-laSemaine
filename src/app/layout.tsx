import type { Metadata, Viewport } from 'next';
import type { ReactNode } from 'react';
import { BottomNavigation } from '@/components/navigation/bottom-navigation';
import './globals.css';

export const metadata: Metadata = {
  title: {
    default: 'Menu de la semaine',
    template: '%s · Menu de la semaine',
  },
  description: 'Les repas de la famille, simplement organisés.',
  manifest: '/site.webmanifest',
};

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
  themeColor: '#f5f2e9',
};

export default function RootLayout({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <html lang="fr">
      <body>
        {children}
        <BottomNavigation />
      </body>
    </html>
  );
}
