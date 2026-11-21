import { ApiError } from './client.ts';

/** Anzeigetext für einen fehlgeschlagenen API-Aufruf (Spec, Abschnitt 11). */
export function errorDetail(error: unknown): string {
  if (error instanceof ApiError) {
    return error.detail;
  }
  return error instanceof Error ? error.message : String(error);
}
