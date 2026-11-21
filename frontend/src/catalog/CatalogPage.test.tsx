import { getDefaultNormalizer, render, screen, within } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { createApiClient } from '../api/client.ts';
import { jsonResponse, problem, stubApi } from '../test/apiMock.ts';
import { partner1, seedTemplates } from '../test/seedData.ts';
import { CatalogPage } from './CatalogPage.tsx';

function renderCatalog() {
  render(<CatalogPage api={createApiClient({ username: 'partner1', password: 'demo' })} />);
}

describe('CatalogPage', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('ui2_showsAllTemplatesWithNameTypeUnitPriceAndSubsidy', async () => {
    stubApi({
      'GET /api/templates': () => jsonResponse(200, seedTemplates),
      'GET /api/partners/me': () => jsonResponse(200, partner1),
    });

    renderCatalog();

    const expected = [
      ['Flyer Frühling', 'FLYER', '0,12 €', '50 %'],
      ['Plakat Sommer', 'POSTER', '2,50 €', '40 %'],
      ['Zeitungsanzeige Herbst', 'NEWSPAPER_AD', '49,00 €', '30 %'],
    ];
    for (const [name, type, unitPrice, subsidy] of expected) {
      const row = await screen.findByRole('row', { name: new RegExp(name ?? '') });
      const cells = within(row).getAllByRole('cell').map((cell) => cell.textContent);
      expect(cells).toEqual([name, type, unitPrice, subsidy]);
    }
    expect(screen.getAllByRole('row')).toHaveLength(1 + seedTemplates.length);
  });

  it('ui2_showsBalance', async () => {
    stubApi({
      'GET /api/templates': () => jsonResponse(200, seedTemplates),
      'GET /api/partners/me': () => jsonResponse(200, partner1),
    });

    renderCatalog();

    expect(
      await screen.findByText('Guthaben: 100,00 €', {
        normalizer: getDefaultNormalizer({ collapseWhitespace: false }),
      }),
    ).toBeInTheDocument();
  });

  it('ui2_loadErrorShowsDetail', async () => {
    stubApi({
      'GET /api/templates': () => problem(500, 'Vorlagen konnten nicht geladen werden.'),
      'GET /api/partners/me': () => jsonResponse(200, partner1),
    });

    renderCatalog();

    expect(
      await screen.findByText('Vorlagen konnten nicht geladen werden.'),
    ).toBeInTheDocument();
    expect(screen.queryByRole('table')).not.toBeInTheDocument();
  });
});
