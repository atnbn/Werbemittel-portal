export interface PricePreview {
  totalCents: number;
  subsidyCents: number;
  ownShareCents: number;
}

/**
 * Preisvorschau nach Spec, Abschnitt 5. Rechnet wie der `PriceCalculator` im Backend
 * ganzzahlig (`totalCents * subsidyPercent / 100`, abgerundet), nie mit einem Bruch als Faktor.
 * Verbindlich ist die Berechnung des Servers.
 */
export function calculatePrice(
  quantity: number,
  unitPriceCents: number,
  subsidyPercent: number,
): PricePreview {
  const totalCents = quantity * unitPriceCents;
  const subsidyCents = Math.floor((totalCents * subsidyPercent) / 100);
  return { totalCents, subsidyCents, ownShareCents: totalCents - subsidyCents };
}
