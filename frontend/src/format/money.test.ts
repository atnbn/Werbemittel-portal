import { describe, expect, it } from 'vitest';
import { formatCents } from './money.ts';

describe('formatCents (Spec, Abschnitt 11)', () => {
  it('formats cents as de-DE euro', () => {
    // Intl setzt ein geschütztes Leerzeichen (U+00A0) vor das Euro-Zeichen.
    expect(formatCents(6000)).toBe('60,00 €');
    expect(formatCents(12)).toBe('0,12 €');
    expect(formatCents(10000)).toBe('100,00 €');
    expect(formatCents(500)).toBe('5,00 €');
  });
});
