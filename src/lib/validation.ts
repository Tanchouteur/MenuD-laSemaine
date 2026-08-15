import { z } from 'zod';

export const guestCountSchema = z.number().int().min(1).max(30);

export const isoDateSchema = z
  .string()
  .regex(/^\d{4}-\d{2}-\d{2}$/, 'La date doit être au format AAAA-MM-JJ.');
