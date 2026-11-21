// Einziger Zugang zur API. Kennt ausschließlich die Endpunkte aus Spec, Abschnitt 9.
import type {
  CreateOrderRequest,
  MeResponse,
  Order,
  Partner,
  ProblemDetail,
  Template,
  UpdateOrderStatusRequest,
} from './types.ts';

export interface Credentials {
  username: string;
  password: string;
}

export class ApiError extends Error {
  readonly status: number;
  readonly detail: string;

  constructor(status: number, detail: string) {
    super(detail);
    this.name = 'ApiError';
    this.status = status;
    this.detail = detail;
  }
}

function encodeBasicAuth({ username, password }: Credentials): string {
  const bytes = new TextEncoder().encode(`${username}:${password}`);
  let binary = '';
  for (const byte of bytes) {
    binary += String.fromCharCode(byte);
  }
  return `Basic ${btoa(binary)}`;
}

function isProblemDetail(body: unknown): body is Pick<ProblemDetail, 'detail'> {
  return (
    typeof body === 'object' &&
    body !== null &&
    'detail' in body &&
    typeof body.detail === 'string'
  );
}

async function readErrorDetail(response: Response): Promise<string> {
  try {
    const body: unknown = await response.json();
    if (isProblemDetail(body)) {
      return body.detail;
    }
  } catch {
    // Kein JSON-Body (z. B. Proxy-Fehler): Fallback auf den HTTP-Statustext.
  }
  return response.statusText || `HTTP ${response.status}`;
}

async function request<T>(
  credentials: Credentials,
  method: 'GET' | 'POST' | 'PATCH',
  path: string,
  body?: unknown,
): Promise<T> {
  const headers: Record<string, string> = {
    Authorization: encodeBasicAuth(credentials),
    Accept: 'application/json',
  };
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
  }

  const response = await fetch(path, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  if (!response.ok) {
    throw new ApiError(response.status, await readErrorDetail(response));
  }
  return (await response.json()) as T;
}

export function createApiClient(credentials: Credentials) {
  return {
    getMe: () => request<MeResponse>(credentials, 'GET', '/api/me'),
    getMyPartner: () => request<Partner>(credentials, 'GET', '/api/partners/me'),
    getTemplates: () => request<Template[]>(credentials, 'GET', '/api/templates'),
    getTemplate: (id: number) => request<Template>(credentials, 'GET', `/api/templates/${id}`),
    createOrder: (order: CreateOrderRequest) =>
      request<Order>(credentials, 'POST', '/api/orders', order),
    getOrders: () => request<Order[]>(credentials, 'GET', '/api/orders'),
    getOrder: (id: number) => request<Order>(credentials, 'GET', `/api/orders/${id}`),
    updateOrderStatus: (id: number, update: UpdateOrderStatusRequest) =>
      request<Order>(credentials, 'PATCH', `/api/orders/${id}/status`, update),
  };
}

export type ApiClient = ReturnType<typeof createApiClient>;
