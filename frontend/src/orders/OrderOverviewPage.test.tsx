import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { createApiClient } from '../api/client.ts';
import type { Order } from '../api/types.ts';
import { jsonResponse, problem, stubApi } from '../test/apiMock.ts';
import { order7 } from '../test/seedData.ts';
import { OrderOverviewPage } from './OrderOverviewPage.tsx';

const order8: Order = {
  id: 8,
  templateId: 2,
  templateName: 'Plakat Sommer',
  partnerId: 2,
  quantity: 2,
  status: 'IN_PRODUKTION',
  totalCents: 500,
  subsidyCents: 200,
  ownShareCents: 300,
  values: {
    claim: 'Gemeinsam mehr erreichen.',
    companyName: 'Fahrrad Schmidt',
    phone: '0951 98765',
    headline: 'Sommerangebote',
  },
  createdAt: '2026-11-21T11:00:00Z',
  updatedAt: '2026-11-21T12:00:00Z',
};

const order9: Order = {
  ...order7,
  id: 9,
  status: 'VERSENDET',
  quantity: 100,
  totalCents: 1200,
  subsidyCents: 600,
  ownShareCents: 600,
};

function renderOverview() {
  const user = userEvent.setup();
  render(<OrderOverviewPage api={createApiClient({ username: 'zentrale', password: 'demo' })} />);
  return user;
}

async function findOrderRow(name: string) {
  return screen.findByRole('row', { name: new RegExp(name) });
}

function cellTexts(row: HTMLElement) {
  return within(row)
    .getAllByRole('cell')
    .map((cell) => cell.textContent);
}

describe('OrderOverviewPage', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('ui8_showsAllOrdersWithCompanyNameFromSnapshotTemplateStatusAndAmounts', async () => {
    stubApi({ 'GET /api/orders': () => jsonResponse(200, [order7, order8, order9]) });

    renderOverview();

    expect(screen.getByRole('heading', { name: 'Bestellübersicht' })).toBeInTheDocument();
    const rows = await screen.findAllByRole('row');
    expect(rows).toHaveLength(4);
    const [, row7, row8, row9] = rows;
    expect(cellTexts(row7 as HTMLElement).slice(0, 6)).toEqual([
      'Autohaus Müller',
      'Flyer Frühling',
      'BESTELLT',
      '60,00 €',
      '30,00 €',
      '30,00 €',
    ]);
    expect(cellTexts(row8 as HTMLElement).slice(0, 6)).toEqual([
      'Fahrrad Schmidt',
      'Plakat Sommer',
      'IN_PRODUKTION',
      '5,00 €',
      '2,00 €',
      '3,00 €',
    ]);
    expect(cellTexts(row9 as HTMLElement).slice(0, 6)).toEqual([
      'Autohaus Müller',
      'Flyer Frühling',
      'VERSENDET',
      '12,00 €',
      '6,00 €',
      '6,00 €',
    ]);
  });

  it('ui8_missingCompanyNameLeavesCellEmpty', async () => {
    const withoutCompanyName: Order = { ...order7, values: { headline: 'Frühlingsaktion!' } };
    stubApi({ 'GET /api/orders': () => jsonResponse(200, [withoutCompanyName]) });

    renderOverview();

    const row = await findOrderRow('Flyer Frühling');
    expect(cellTexts(row)[0]).toBe('');
  });

  it('ui8_eachOrderHasOnlyButtonForNextStatusAndNoneWhenShipped', async () => {
    stubApi({ 'GET /api/orders': () => jsonResponse(200, [order7, order8, order9]) });

    renderOverview();

    const rows = await screen.findAllByRole('row');
    const [, row7, row8, row9] = rows as HTMLElement[];
    expect(
      within(row7 as HTMLElement)
        .getAllByRole('button')
        .map((b) => b.textContent),
    ).toEqual(['Auf IN_PRODUKTION setzen']);
    expect(
      within(row8 as HTMLElement)
        .getAllByRole('button')
        .map((b) => b.textContent),
    ).toEqual(['Auf VERSENDET setzen']);
    expect(within(row9 as HTMLElement).queryAllByRole('button')).toHaveLength(0);
  });

  it('ui8_statusButtonSendsPatchAndUpdatesRow', async () => {
    const updated: Order = { ...order7, status: 'IN_PRODUKTION', updatedAt: '2026-11-22T09:00:00Z' };
    const fetchMock = stubApi({
      'GET /api/orders': () => jsonResponse(200, [order7, order8]),
      'PATCH /api/orders/7/status': () => jsonResponse(200, updated),
    });

    const user = renderOverview();
    const row = await findOrderRow('Autohaus Müller');
    await user.click(within(row).getByRole('button', { name: 'Auf IN_PRODUKTION setzen' }));

    expect(
      await within(row).findByRole('button', { name: 'Auf VERSENDET setzen' }),
    ).toBeInTheDocument();
    expect(cellTexts(row)[2]).toBe('IN_PRODUKTION');
    const patchCall = fetchMock.mock.calls.find(([, init]) => init?.method === 'PATCH');
    expect(patchCall?.[0]).toBe('/api/orders/7/status');
    expect(JSON.parse(String(patchCall?.[1]?.body))).toEqual({ status: 'IN_PRODUKTION' });
    expect(fetchMock.mock.calls.filter(([url]) => url === '/api/orders')).toHaveLength(1);
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  it('ui8_statusButtonDisabledDuringRequest', async () => {
    let resolvePatch: (response: Response) => void = () => {};
    const pending = new Promise<Response>((resolve) => {
      resolvePatch = resolve;
    });
    vi.stubGlobal(
      'fetch',
      vi.fn((_input: string, init?: RequestInit) =>
        init?.method === 'PATCH' ? pending : Promise.resolve(jsonResponse(200, [order7])),
      ),
    );

    const user = renderOverview();
    const row = await findOrderRow('Autohaus Müller');
    const button = within(row).getByRole('button', { name: 'Auf IN_PRODUKTION setzen' });
    await user.click(button);

    expect(button).toBeDisabled();
    resolvePatch(jsonResponse(200, { ...order7, status: 'IN_PRODUKTION' }));
    expect(
      await within(row).findByRole('button', { name: 'Auf VERSENDET setzen' }),
    ).toBeEnabled();
  });

  it('ui8_statusChangeErrorShowsDetail', async () => {
    stubApi({
      'GET /api/orders': () => jsonResponse(200, [order7]),
      'PATCH /api/orders/7/status': () =>
        problem(409, 'Statuswechsel von BESTELLT nach IN_PRODUKTION nicht erlaubt.'),
    });

    const user = renderOverview();
    const row = await findOrderRow('Autohaus Müller');
    await user.click(within(row).getByRole('button', { name: 'Auf IN_PRODUKTION setzen' }));

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Statuswechsel von BESTELLT nach IN_PRODUKTION nicht erlaubt.',
    );
    expect(cellTexts(row)[2]).toBe('BESTELLT');
    expect(within(row).getByRole('button', { name: 'Auf IN_PRODUKTION setzen' })).toBeEnabled();
    expect(screen.getByRole('table')).toBeInTheDocument();
  });

  it('ui8_loadErrorShowsDetail', async () => {
    stubApi({ 'GET /api/orders': () => problem(500, 'Bestellungen konnten nicht geladen werden.') });

    renderOverview();

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Bestellungen konnten nicht geladen werden.',
    );
    expect(screen.queryByRole('table')).not.toBeInTheDocument();
  });
});
