import { describe, expect, it } from 'vitest';
import { nextStatus } from './nextStatus.ts';

describe('nextStatus', () => {
  it('ui8_nextStatusFollowsStatusModel', () => {
    expect(nextStatus('BESTELLT')).toBe('IN_PRODUKTION');
    expect(nextStatus('IN_PRODUKTION')).toBe('VERSENDET');
  });

  it('ui8_shippedHasNoNextStatus', () => {
    expect(nextStatus('VERSENDET')).toBeNull();
  });
});
