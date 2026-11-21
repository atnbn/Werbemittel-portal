import { vi } from 'vitest';

export function jsonResponse(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  });
}

export function problem(status: number, detail: string): Response {
  return jsonResponse(status, { type: 'about:blank', title: 'Error', status, detail });
}

/** Antworten je Route, Schlüssel `METHODE pfad`, z. B. `GET /api/me`. */
export type Routes = Record<string, () => Response>;

export function stubApi(routes: Routes) {
  const fetchMock = vi.fn((input: string, init?: RequestInit) => {
    const key = `${init?.method ?? 'GET'} ${input}`;
    const route = routes[key];
    if (!route) {
      return Promise.reject(new Error(`Unerwarteter Request im Test: ${key}`));
    }
    return Promise.resolve(route());
  });
  vi.stubGlobal('fetch', fetchMock);
  return fetchMock;
}
