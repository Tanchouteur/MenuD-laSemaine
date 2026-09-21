import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    environment: 'node',
    include: ['src/**/*.test.ts'],
    exclude: ['src/**/*.integration.test.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'],
      reportsDirectory: 'coverage',
      include: [
        'src/engine/**/*.ts',
        'src/domain/**/*.ts',
        'src/lib/api.ts',
        'src/lib/family-auth.ts',
        'src/lib/schemas.ts',
        'src/lib/validation.ts',
        'src/lib/week.ts',
      ],
      exclude: ['src/**/*.test.ts', 'src/engine/types.ts', 'src/engine/index.ts'],
      thresholds: {
        lines: 85,
        functions: 85,
        statements: 85,
        branches: 75,
      },
    },
  },
  resolve: {
    alias: {
      '@': new URL('./src', import.meta.url).pathname,
      'server-only': new URL('./tests/server-only.ts', import.meta.url).pathname,
    },
  },
});
