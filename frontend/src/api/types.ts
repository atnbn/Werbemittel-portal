// API-Typen exakt nach Spec, Abschnitt 9. Einzige Stelle für API-Typen im Frontend.

export type Role = 'PARTNER' | 'ZENTRALE';

export type TemplateType = 'FLYER' | 'POSTER' | 'NEWSPAPER_AD';

export type FieldType = 'FIXED' | 'PARTNER_DATA' | 'EDITABLE';

export type OrderStatus = 'BESTELLT' | 'IN_PRODUKTION' | 'VERSENDET';

/** GET /api/me */
export interface MeResponse {
  username: string;
  role: Role;
}

/** GET /api/partners/me */
export interface Partner {
  id: number;
  companyName: string;
  street: string;
  zip: string;
  city: string;
  phone: string;
  balanceCents: number;
}

export interface TemplateField {
  key: string;
  label: string;
  fieldType: FieldType;
  defaultValue: string | null;
  partnerAttribute: string | null;
  maxLength: number | null;
  position: number;
}

/** GET /api/templates, GET /api/templates/{id} */
export interface Template {
  id: number;
  name: string;
  type: TemplateType;
  unitPriceCents: number;
  subsidyPercent: number;
  fields: TemplateField[];
}

/** POST /api/orders – Request. `values` enthält nur EDITABLE-Felder und ist optional. */
export interface CreateOrderRequest {
  templateId: number;
  quantity: number;
  values?: Record<string, string>;
}

/** POST /api/orders, GET /api/orders, GET /api/orders/{id}, PATCH /api/orders/{id}/status */
export interface Order {
  id: number;
  templateId: number;
  templateName: string;
  partnerId: number;
  quantity: number;
  status: OrderStatus;
  totalCents: number;
  subsidyCents: number;
  ownShareCents: number;
  values: Record<string, string>;
  createdAt: string;
  updatedAt: string;
}

/** PATCH /api/orders/{id}/status – Request */
export interface UpdateOrderStatusRequest {
  status: OrderStatus;
}

/** Fehlerantwort als Problem Details (RFC 9457) */
export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
}
