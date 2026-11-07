import { ZodError } from 'zod';
export class McpFault extends Error {
  constructor(public code: string, public details: Record<string, unknown> = {}, public retryable = false) { super(code); }
}
export function isConcurrentError(error: unknown) {
  const known = error as { code?: string; meta?: { code?: string } };
  return ['P2034', 'P2002'].includes(known?.code ?? '') || ['40001', '40P01'].includes(known?.meta?.code ?? '');
}
export function failure(error: unknown) {
  if (error instanceof McpFault) return { ok: false as const, error: { code: error.code, retryable: error.retryable, details: error.details } };
  if (error instanceof ZodError) return { ok: false as const, error: { code: 'VALIDATION_ERROR', retryable: false, details: { fields: error.issues.map(i => ({ path: i.path.join('.'), code: i.code })) } } };
  const code = (error as { code?: string })?.code;
  return { ok: false as const, error: { code: isConcurrentError(error) ? 'CONCURRENT_CHANGE' : code === 'P2025' ? 'NOT_FOUND' : code === 'P2003' ? 'INVALID_REFERENCE' : 'BUSINESS_VALIDATION_FAILED', retryable: isConcurrentError(error), details: {} } };
}
