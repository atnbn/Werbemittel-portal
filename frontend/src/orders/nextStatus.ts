import type { OrderStatus } from '../api/types.ts';

// Statusmodell aus Spec, Abschnitt 7. VERSENDET ist Endzustand.
const NEXT: Record<OrderStatus, OrderStatus | null> = {
  BESTELLT: 'IN_PRODUKTION',
  IN_PRODUKTION: 'VERSENDET',
  VERSENDET: null,
};

export function nextStatus(status: OrderStatus): OrderStatus | null {
  return NEXT[status];
}
