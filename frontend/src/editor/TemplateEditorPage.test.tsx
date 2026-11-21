import { getDefaultNormalizer, render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { createApiClient } from '../api/client.ts';
import type { Partner, Template } from '../api/types.ts';
import { jsonResponse, problem, stubApi } from '../test/apiMock.ts';
import { order7, partner1, partner2, seedTemplate } from '../test/seedData.ts';
import { TemplateEditorPage } from './TemplateEditorPage.tsx';

// Intl setzt ein geschütztes Leerzeichen vor „€“; die Normalisierung darf es nicht ersetzen.
const keepWhitespace = { normalizer: getDefaultNormalizer({ collapseWhitespace: false }) };

function renderEditor(template: Template, partner: Partner) {
  const onOrdered = vi.fn();
  const user = userEvent.setup();
  render(
    <TemplateEditorPage
      api={createApiClient({ username: 'partner1', password: 'demo' })}
      template={template}
      partner={partner}
      onOrdered={onOrdered}
    />,
  );
  return { user, onOrdered };
}

function previewItems(): (string | null)[] {
  return within(screen.getByRole('list', { name: 'Vorschau' }))
    .getAllByRole('listitem')
    .map((item) => item.textContent);
}

describe('TemplateEditorPage', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('ui3_fixedAndPartnerDataFieldsAreReadOnly', () => {
    renderEditor(seedTemplate(2), partner1);

    const expected = [
      ['Marken-Claim', 'Gemeinsam mehr erreichen.'],
      ['Firmenname', 'Autohaus Müller'],
      ['Telefon', '09531 12345'],
    ] as const;
    for (const [label, value] of expected) {
      const input = screen.getByLabelText(label);
      expect(input).toHaveValue(value);
      expect(input).toHaveAttribute('readonly');
    }
    expect(screen.getByLabelText('Überschrift')).not.toHaveAttribute('readonly');
  });

  it('ui3_editableFieldHasMaxLengthAndCharacterCounter', async () => {
    const { user } = renderEditor(seedTemplate(2), partner1);

    const headline = screen.getByLabelText('Überschrift');
    expect(headline).toHaveValue('Sommerangebote');
    expect(headline).toHaveAttribute('maxlength', '30');
    expect(screen.getByText('14/30')).toBeInTheDocument();

    await user.clear(headline);
    await user.type(headline, 'Hallo');

    expect(screen.getByText('5/30')).toBeInTheDocument();
  });

  it('ui4_previewUpdatesImmediatelyWithAllValuesInPositionOrder', async () => {
    const poster = seedTemplate(2);
    const { user } = renderEditor({ ...poster, fields: [...poster.fields].reverse() }, partner1);

    expect(previewItems()).toEqual([
      'Gemeinsam mehr erreichen.',
      'Autohaus Müller',
      '09531 12345',
      'Sommerangebote',
    ]);

    await user.clear(screen.getByLabelText('Überschrift'));
    await user.type(screen.getByLabelText('Überschrift'), 'Sale');

    expect(previewItems()).toEqual([
      'Gemeinsam mehr erreichen.',
      'Autohaus Müller',
      '09531 12345',
      'Sale',
    ]);
  });

  it('ui5_quantityChangeShowsTotalSubsidyAndOwnShare', async () => {
    const { user } = renderEditor(seedTemplate(1), partner1);

    expect(screen.getByText('Gesamtpreis: 0,12 €', keepWhitespace)).toBeInTheDocument();

    await user.clear(screen.getByLabelText('Menge'));
    await user.type(screen.getByLabelText('Menge'), '500');

    expect(screen.getByText('Gesamtpreis: 60,00 €', keepWhitespace)).toBeInTheDocument();
    expect(screen.getByText('Zuschuss: 30,00 €', keepWhitespace)).toBeInTheDocument();
    expect(screen.getByText('Eigenanteil: 30,00 €', keepWhitespace)).toBeInTheDocument();
  });

  it('ui5_insufficientBalanceShowsHint', async () => {
    // Plakat: 250 ct, 40 % → 100 ct Zuschuss pro Stück; partner2 hat 500 ct.
    const { user } = renderEditor(seedTemplate(2), partner2);
    const quantity = screen.getByLabelText('Menge');
    const hint = 'Guthaben reicht nicht für den Zuschuss.';

    await user.clear(quantity);
    await user.type(quantity, '5');
    expect(screen.getByText('Zuschuss: 5,00 €', keepWhitespace)).toBeInTheDocument();
    expect(screen.queryByText(hint)).not.toBeInTheDocument();

    await user.clear(quantity);
    await user.type(quantity, '6');
    expect(screen.getByText('Zuschuss: 6,00 €', keepWhitespace)).toBeInTheDocument();
    expect(screen.getByText(hint)).toBeInTheDocument();
  });

  it('ui6_orderSendsPostWithEditableValuesOnly', async () => {
    const fetchMock = stubApi({ 'POST /api/orders': () => jsonResponse(201, order7) });
    const { user, onOrdered } = renderEditor(seedTemplate(1), partner1);

    await user.clear(screen.getByLabelText('Überschrift'));
    await user.type(screen.getByLabelText('Überschrift'), 'Frühlingsaktion!');
    await user.clear(screen.getByLabelText('Menge'));
    await user.type(screen.getByLabelText('Menge'), '500');
    await user.click(screen.getByRole('button', { name: 'Bestellen' }));

    await waitFor(() => expect(onOrdered).toHaveBeenCalledTimes(1));
    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, init] = fetchMock.mock.calls[0] ?? [];
    expect(url).toBe('/api/orders');
    expect(init?.method).toBe('POST');
    expect(JSON.parse(String(init?.body))).toEqual({
      templateId: 1,
      quantity: 500,
      values: { headline: 'Frühlingsaktion!' },
    });
  });

  it('ui6_orderErrorShowsDetail', async () => {
    stubApi({
      'POST /api/orders': () =>
        problem(422, 'Guthaben reicht nicht für den Zuschuss von 3000 Cent.'),
    });
    const { user, onOrdered } = renderEditor(seedTemplate(1), partner2);

    await user.clear(screen.getByLabelText('Menge'));
    await user.type(screen.getByLabelText('Menge'), '500');
    await user.click(screen.getByRole('button', { name: 'Bestellen' }));

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Guthaben reicht nicht für den Zuschuss von 3000 Cent.',
    );
    expect(onOrdered).not.toHaveBeenCalled();
  });

  it('ui6_orderButtonDisabledForInvalidQuantity', async () => {
    const { user } = renderEditor(seedTemplate(1), partner1);
    const quantity = screen.getByLabelText('Menge');
    const order = screen.getByRole('button', { name: 'Bestellen' });

    expect(order).toBeEnabled();

    await user.clear(quantity);
    expect(order).toBeDisabled();

    await user.type(quantity, '0');
    expect(order).toBeDisabled();

    await user.clear(quantity);
    await user.type(quantity, '1.5');
    expect(order).toBeDisabled();

    await user.clear(quantity);
    await user.type(quantity, '2');
    expect(order).toBeEnabled();
  });
});
