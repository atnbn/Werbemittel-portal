import { getDefaultNormalizer, render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { createApiClient } from '../api/client.ts';
import type { Partner } from '../api/types.ts';
import { jsonResponse, stubApi } from '../test/apiMock.ts';
import { order7, partner1, seedTemplates } from '../test/seedData.ts';
import { PartnerPage } from './PartnerPage.tsx';

function stubPartnerApi(partners: Partner[] = [partner1]) {
  let partnerCalls = 0;
  return stubApi({
    'GET /api/templates': () => jsonResponse(200, seedTemplates),
    'GET /api/partners/me': () => {
      const partner = partners[Math.min(partnerCalls, partners.length - 1)];
      partnerCalls += 1;
      return jsonResponse(200, partner);
    },
    'POST /api/orders': () => jsonResponse(201, order7),
    'GET /api/orders': () => jsonResponse(200, [order7]),
  });
}

function renderPartnerPage() {
  const user = userEvent.setup();
  render(<PartnerPage api={createApiClient({ username: 'partner1', password: 'demo' })} />);
  return user;
}

async function personalize(user: ReturnType<typeof userEvent.setup>, templateName: string) {
  const row = await screen.findByRole('row', { name: new RegExp(templateName) });
  await user.click(within(row).getByRole('button', { name: 'Personalisieren' }));
}

async function orderFlyer(user: ReturnType<typeof userEvent.setup>) {
  await personalize(user, 'Flyer Frühling');
  await user.clear(screen.getByLabelText('Menge'));
  await user.type(screen.getByLabelText('Menge'), '500');
  await user.click(screen.getByRole('button', { name: 'Bestellen' }));
}

async function expectOrder7Row() {
  const row = await screen.findByRole('row', { name: /Flyer Frühling/ });
  expect(within(row).getAllByRole('cell').map((cell) => cell.textContent)).toEqual([
    'Flyer Frühling',
    '500',
    'BESTELLT',
    '30,00 €',
    '30,00 €',
  ]);
}

describe('PartnerPage', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('ui3_personalizeOpensEditorForTemplate', async () => {
    stubPartnerApi();
    const user = renderPartnerPage();

    await personalize(user, 'Plakat Sommer');

    expect(screen.getByRole('heading', { name: 'Plakat Sommer' })).toBeInTheDocument();
    expect(screen.getByLabelText('Telefon')).toHaveValue('09531 12345');
    expect(screen.queryByRole('heading', { name: 'Katalog' })).not.toBeInTheDocument();
  });

  it('ui6_successfulOrderShowsNewOrderUnderMyOrders', async () => {
    const fetchMock = stubPartnerApi();
    const user = renderPartnerPage();

    await orderFlyer(user);

    expect(await screen.findByRole('heading', { name: 'Meine Bestellungen' })).toBeInTheDocument();
    await expectOrder7Row();
    const requests = fetchMock.mock.calls.map(([url, init]) => `${init?.method ?? 'GET'} ${url}`);
    expect(requests.indexOf('POST /api/orders')).toBeLessThan(requests.indexOf('GET /api/orders'));
  });

  it('ui6_catalogShowsFreshBalanceAfterOrder', async () => {
    stubPartnerApi([partner1, { ...partner1, balanceCents: 7000 }]);
    const user = renderPartnerPage();
    await orderFlyer(user);
    await screen.findByRole('heading', { name: 'Meine Bestellungen' });

    await user.click(screen.getByRole('button', { name: 'Katalog' }));

    expect(
      await screen.findByText('Guthaben: 70,00 €', {
        normalizer: getDefaultNormalizer({ collapseWhitespace: false }),
      }),
    ).toBeInTheDocument();
  });

  it('ui7_navigationShowsMyOrders', async () => {
    stubPartnerApi();
    const user = renderPartnerPage();
    await screen.findByRole('heading', { name: 'Katalog' });

    await user.click(screen.getByRole('button', { name: 'Meine Bestellungen' }));

    expect(await screen.findByRole('heading', { name: 'Meine Bestellungen' })).toBeInTheDocument();
    await expectOrder7Row();
  });
});
