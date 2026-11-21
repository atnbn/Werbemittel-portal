import type { Partner, Template, TemplateField } from '../api/types.ts';

const PARTNER_ATTRIBUTES = ['companyName', 'street', 'zip', 'city', 'phone'] as const;

type PartnerAttribute = (typeof PARTNER_ATTRIBUTES)[number];

function isPartnerAttribute(attribute: string | null): attribute is PartnerAttribute {
  return PARTNER_ATTRIBUTES.some((known) => known === attribute);
}

export function sortedFields(template: Template): TemplateField[] {
  return [...template.fields].sort((a, b) => a.position - b.position);
}

/** Wert eines Felds nach Spec, Abschnitt 4. */
export function fieldValue(
  field: TemplateField,
  partner: Partner,
  editableValues: Record<string, string>,
): string {
  switch (field.fieldType) {
    case 'FIXED':
      return field.defaultValue ?? '';
    case 'PARTNER_DATA':
      return isPartnerAttribute(field.partnerAttribute) ? partner[field.partnerAttribute] : '';
    case 'EDITABLE':
      return editableValues[field.key] ?? field.defaultValue ?? '';
  }
}

/** Startwerte des Editors: nur EDITABLE-Keys, jeweils mit dem Standardwert. */
export function initialEditableValues(template: Template): Record<string, string> {
  return Object.fromEntries(
    template.fields
      .filter((field) => field.fieldType === 'EDITABLE')
      .map((field) => [field.key, field.defaultValue ?? '']),
  );
}

/** Alle Feldwerte der Vorschau in der Reihenfolge von `position`. */
export function composePreview(
  template: Template,
  partner: Partner,
  editableValues: Record<string, string>,
): string[] {
  return sortedFields(template).map((field) => fieldValue(field, partner, editableValues));
}
