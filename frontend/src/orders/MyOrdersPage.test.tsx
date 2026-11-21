import { render, screen, within } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { createApiClient } from '../api/client.ts';
import type { Order } from '../api/types.ts';
import { jsonResponse, problem, stubApi } from '../test/apiMock.ts';
import { order7 } from '../test/seedData.ts';
import { MyOrdersPage } from './MyOrdersPage.tsx';

const order8: Order = {
  ...order7,
  id: 8,
  templateId: 2,
  templateName: 'Plakat Sommer',
  quantity: 10,
  status: 'IN_PRODUKTION',
  totalCents: 2500,
  subsidyCents: 1000,
  ownShareCents: 1500,
};

function renderMyOrders() {
  render(<MyOrdersPage api={createApiClient({ username: 'partner1', password: 'demo' })} />);
}

describe('MyOrdersPage', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('ui7_showsOrdersWithTemplateQuantityStatusSubsidyAndOwnShare', async () => {
    stubApi({ 'GET /api/orders': () => jsonResponse(200, [order7, order8]) });

    renderMyOrders();

    expect(screen.getByRole('heading', { name: 'Meine Bestellungen' })).toBeInTheDocument();
    const expected = [
      ['Flyer Frühling', '500', 'BESTELLT', '30,00 €', '30,00 €'],
      ['Plakat Sommer', '10', 'IN_PRODUKTION', '10,00 €', '15,00 €'],
    ];
    for (const cellsExpected of expected) {
      const row = await screen.findByRole('row', { name: new RegExp(cellsExpected[0] ?? '') });
      const cells = within(row).getAllByRole('cell').map((cell) => cell.textContent);
      expect(cells).toEqual(cellsExpected);
    }
    expect(screen.getAllByRole('row')).toHaveLength(3);
  });

  it('ui7_loadErrorShowsDetail', async () => {
    stubApi({ 'GET /api/orders': () => problem(500, 'Bestellungen konnten nicht geladen werden.') });

    renderMyOrders();

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Bestellungen konnten nicht geladen werden.',
    );
    expect(screen.queryByRole('table')).not.toBeInTheDocument();
  });
});
