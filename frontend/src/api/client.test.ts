import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApiError, createApiClient } from './client.ts';

function jsonResponse(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  });
}

describe('api client (smoke)', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('sends the Basic-Auth header and returns the parsed body', async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      jsonResponse(200, { username: 'partner1', role: 'PARTNER' }),
    );
    vi.stubGlobal('fetch', fetchMock);

    const me = await createApiClient({ username: 'partner1', password: 'demo' }).getMe();

    expect(me).toEqual({ username: 'partner1', role: 'PARTNER' });
    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(url).toBe('/api/me');
    expect(init.method).toBe('GET');
    expect(new Headers(init.headers).get('Authorization')).toBe(`Basic ${btoa('partner1:demo')}`);
  });

  it('throws an ApiError with status and detail from the problem details', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        jsonResponse(422, {
          type: 'about:blank',
          title: 'Unprocessable Entity',
          status: 422,
          detail: 'Guthaben reicht nicht für den Zuschuss von 3000 Cent.',
        }),
      ),
    );

    const error = await createApiClient({ username: 'partner2', password: 'demo' })
      .createOrder({ templateId: 1, quantity: 500 })
      .catch((e: unknown) => e);

    expect(error).toBeInstanceOf(ApiError);
    expect(error).toMatchObject({
      status: 422,
      detail: 'Guthaben reicht nicht für den Zuschuss von 3000 Cent.',
    });
  });
});
