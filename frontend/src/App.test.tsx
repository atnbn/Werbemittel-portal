import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { App } from './App.tsx';
import type { Role } from './api/types.ts';
import { jsonResponse, problem, stubApi } from './test/apiMock.ts';
import { partner1, seedTemplates } from './test/seedData.ts';

describe('App (smoke)', () => {
  it('renders the portal heading', () => {
    render(<App />);

    expect(screen.getByRole('heading', { name: 'Mini-Marketingportal' })).toBeInTheDocument();
  });
});

function stubLoginAs(username: string, role: Role) {
  return stubApi({
    'GET /api/me': () => jsonResponse(200, { username, role }),
    'GET /api/templates': () => jsonResponse(200, seedTemplates),
    'GET /api/partners/me': () => jsonResponse(200, partner1),
    'GET /api/orders': () => jsonResponse(200, []),
  });
}

async function logIn(username: string, password: string) {
  const user = userEvent.setup();
  render(<App />);
  await user.type(screen.getByLabelText('Benutzername'), username);
  await user.type(screen.getByLabelText('Passwort'), password);
  await user.click(screen.getByRole('button', { name: 'Anmelden' }));
  return user;
}

describe('App', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
    localStorage.clear();
    sessionStorage.clear();
  });

  it('ui1_partnerLoginShowsCatalog', async () => {
    const fetchMock = stubLoginAs('partner1', 'PARTNER');

    await logIn('partner1', 'demo');

    expect(await screen.findByRole('heading', { name: 'Katalog' })).toBeInTheDocument();
    const [url, init] = fetchMock.mock.calls[0] ?? [];
    expect(url).toBe('/api/me');
    expect(new Headers(init?.headers).get('Authorization')).toBe(`Basic ${btoa('partner1:demo')}`);
    expect(screen.queryByRole('heading', { name: 'Bestellübersicht' })).not.toBeInTheDocument();
  });

  it('ui1_zentraleLoginShowsOrderOverview', async () => {
    stubLoginAs('zentrale', 'ZENTRALE');

    await logIn('zentrale', 'demo');

    expect(await screen.findByRole('heading', { name: 'Bestellübersicht' })).toBeInTheDocument();
    expect(screen.queryByRole('heading', { name: 'Katalog' })).not.toBeInTheDocument();
  });

  it('ui1_rejectedLoginShowsFailureMessage', async () => {
    stubApi({ 'GET /api/me': () => problem(401, 'Nicht angemeldet.') });

    await logIn('partner1', 'falsch');

    expect(await screen.findByText('Anmeldung fehlgeschlagen.')).toBeInTheDocument();
    expect(screen.queryByText('Nicht angemeldet.')).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Anmelden' })).toBeInTheDocument();
  });

  it('ui1_loginServerErrorShowsDetail', async () => {
    stubApi({ 'GET /api/me': () => problem(500, 'Interner Serverfehler.') });

    await logIn('partner1', 'demo');

    expect(await screen.findByText('Interner Serverfehler.')).toBeInTheDocument();
    expect(screen.queryByText('Anmeldung fehlgeschlagen.')).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Anmelden' })).toBeInTheDocument();
  });

  it.each([
    ['partner1', 'PARTNER', 'Katalog'],
    ['zentrale', 'ZENTRALE', 'Bestellübersicht'],
  ] as const)(
    'ui9_logoutClearsCredentialsAndShowsLoginPage (%s)',
    async (username, role, heading) => {
      stubLoginAs(username, role);
      const user = await logIn(username, 'demo');
      await screen.findByRole('heading', { name: heading });

      await user.click(screen.getByRole('button', { name: 'Abmelden' }));

      expect(screen.getByRole('button', { name: 'Anmelden' })).toBeInTheDocument();
      expect(screen.queryByRole('heading', { name: heading })).not.toBeInTheDocument();
      expect(screen.getByLabelText('Benutzername')).toHaveValue('');
      expect(screen.getByLabelText('Passwort')).toHaveValue('');
      expect(localStorage).toHaveLength(0);
      expect(sessionStorage).toHaveLength(0);
    },
  );
});
