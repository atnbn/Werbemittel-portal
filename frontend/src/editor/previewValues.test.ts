import { describe, expect, it } from 'vitest';
import { partner1, seedTemplate } from '../test/seedData.ts';
import { composePreview, initialEditableValues } from './previewValues.ts';

describe('composePreview', () => {
  it('ui4_composesAllFieldValuesInPositionOrder', () => {
    const poster = seedTemplate(2);
    const shuffled = { ...poster, fields: [...poster.fields].reverse() };

    expect(composePreview(shuffled, partner1, { headline: 'Sale' })).toEqual([
      'Gemeinsam mehr erreichen.',
      'Autohaus Müller',
      '09531 12345',
      'Sale',
    ]);
  });

  it('ui4_emptyEditableValueStaysEmpty', () => {
    expect(composePreview(seedTemplate(1), partner1, { headline: '' })).toEqual([
      'Gemeinsam mehr erreichen.',
      'Autohaus Müller',
      '',
    ]);
  });
});

describe('initialEditableValues', () => {
  it('ui6_initialEditableValuesContainOnlyEditableKeys', () => {
    expect(initialEditableValues(seedTemplate(3))).toEqual({
      offer: 'Jetzt Termin vereinbaren.',
    });
  });
});
