// Ausschnitt der Seed-Daten aus Spec, Abschnitt 10, für Komponententests.
import type { Partner, Template } from '../api/types.ts';

export const partner1: Partner = {
  id: 1,
  companyName: 'Autohaus Müller',
  street: 'Hauptstr. 1',
  zip: '96106',
  city: 'Ebern',
  phone: '09531 12345',
  balanceCents: 10000,
};

const claim = {
  key: 'claim',
  label: 'Marken-Claim',
  fieldType: 'FIXED',
  defaultValue: 'Gemeinsam mehr erreichen.',
  partnerAttribute: null,
  maxLength: null,
  position: 1,
} as const;

const companyName = {
  key: 'companyName',
  label: 'Firmenname',
  fieldType: 'PARTNER_DATA',
  defaultValue: null,
  partnerAttribute: 'companyName',
  maxLength: null,
  position: 2,
} as const;

export const seedTemplates: Template[] = [
  {
    id: 1,
    name: 'Flyer Frühling',
    type: 'FLYER',
    unitPriceCents: 12,
    subsidyPercent: 50,
    fields: [
      claim,
      companyName,
      {
        key: 'headline',
        label: 'Überschrift',
        fieldType: 'EDITABLE',
        defaultValue: 'Jetzt vorbeikommen!',
        partnerAttribute: null,
        maxLength: 40,
        position: 3,
      },
    ],
  },
  {
    id: 2,
    name: 'Plakat Sommer',
    type: 'POSTER',
    unitPriceCents: 250,
    subsidyPercent: 40,
    fields: [
      claim,
      companyName,
      {
        key: 'phone',
        label: 'Telefon',
        fieldType: 'PARTNER_DATA',
        defaultValue: null,
        partnerAttribute: 'phone',
        maxLength: null,
        position: 3,
      },
      {
        key: 'headline',
        label: 'Überschrift',
        fieldType: 'EDITABLE',
        defaultValue: 'Sommerangebote',
        partnerAttribute: null,
        maxLength: 30,
        position: 4,
      },
    ],
  },
  {
    id: 3,
    name: 'Zeitungsanzeige Herbst',
    type: 'NEWSPAPER_AD',
    unitPriceCents: 4900,
    subsidyPercent: 30,
    fields: [
      claim,
      companyName,
      {
        key: 'city',
        label: 'Ort',
        fieldType: 'PARTNER_DATA',
        defaultValue: null,
        partnerAttribute: 'city',
        maxLength: null,
        position: 3,
      },
      {
        key: 'offer',
        label: 'Angebotstext',
        fieldType: 'EDITABLE',
        defaultValue: 'Jetzt Termin vereinbaren.',
        partnerAttribute: null,
        maxLength: 60,
        position: 4,
      },
    ],
  },
];
