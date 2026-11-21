import { describe, expect, it } from 'vitest';
import { calculatePrice } from './pricePreview.ts';

describe('calculatePrice (Spec, Abschnitt 5)', () => {
  it('ui5_example1_calculatesTotalSubsidyAndOwnShare', () => {
    expect(calculatePrice(500, 12, 50)).toEqual({
      totalCents: 6000,
      subsidyCents: 3000,
      ownShareCents: 3000,
    });
  });

  it('ui5_example2_roundsSubsidyDown', () => {
    expect(calculatePrice(333, 12, 33)).toEqual({
      totalCents: 3996,
      subsidyCents: 1318,
      ownShareCents: 2678,
    });
  });

  it('ui5_noFloatingPointError', () => {
    // 100 * 0.29 = 28.999999999999996 → mit einem Bruch als Faktor käme 28 heraus.
    expect(calculatePrice(100, 1, 29)).toEqual({
      totalCents: 100,
      subsidyCents: 29,
      ownShareCents: 71,
    });
  });
});
