import { useState, type FormEvent } from 'react';
import type { ApiClient } from '../api/client.ts';
import { errorDetail } from '../api/errorDetail.ts';
import type { Partner, Template } from '../api/types.ts';
import { formatCents } from '../format/money.ts';
import { calculatePrice } from './pricePreview.ts';
import { composePreview, fieldValue, initialEditableValues, sortedFields } from './previewValues.ts';

interface TemplateEditorPageProps {
  api: ApiClient;
  template: Template;
  partner: Partner;
  onOrdered: () => void;
}

function parseQuantity(input: string): number | null {
  const quantity = Number(input);
  return input !== '' && Number.isInteger(quantity) && quantity >= 1 ? quantity : null;
}

export function TemplateEditorPage({ api, template, partner, onOrdered }: TemplateEditorPageProps) {
  const [values, setValues] = useState(() => initialEditableValues(template));
  const [quantityInput, setQuantityInput] = useState('1');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const quantity = parseQuantity(quantityInput);
  const price =
    quantity === null
      ? null
      : calculatePrice(quantity, template.unitPriceCents, template.subsidyPercent);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (quantity === null) return;
    setError(null);
    setSubmitting(true);
    try {
      await api.createOrder({ templateId: template.id, quantity, values });
      onOrdered();
    } catch (e) {
      setError(errorDetail(e));
      setSubmitting(false);
    }
  }

  return (
    <section>
      <h2>{template.name}</h2>
      <form className="editor" onSubmit={handleSubmit}>
        {sortedFields(template).map((field) =>
          field.fieldType === 'EDITABLE' ? (
            <p key={field.key}>
              <label>
                {field.label}
                <input
                  value={values[field.key] ?? ''}
                  maxLength={field.maxLength ?? undefined}
                  onChange={(e) => setValues({ ...values, [field.key]: e.target.value })}
                />
              </label>
              {field.maxLength !== null && (
                <span className="char-count">
                  {`${(values[field.key] ?? '').length}/${field.maxLength}`}
                </span>
              )}
            </p>
          ) : (
            <p key={field.key}>
              <label>
                {field.label}
                <input readOnly value={fieldValue(field, partner, values)} />
              </label>
            </p>
          ),
        )}

        <ol className="preview" aria-label="Vorschau">
          {composePreview(template, partner, values).map((value, index) => (
            <li key={index}>{value}</li>
          ))}
        </ol>

        <label>
          Menge
          <input
            type="number"
            min={1}
            step={1}
            value={quantityInput}
            onChange={(e) => setQuantityInput(e.target.value)}
          />
        </label>
        {price && (
          <>
            <p>Gesamtpreis: {formatCents(price.totalCents)}</p>
            <p>Zuschuss: {formatCents(price.subsidyCents)}</p>
            <p>Eigenanteil: {formatCents(price.ownShareCents)}</p>
            {partner.balanceCents < price.subsidyCents && (
              <p className="warning">Guthaben reicht nicht für den Zuschuss.</p>
            )}
          </>
        )}

        <button type="submit" disabled={quantity === null || submitting}>
          Bestellen
        </button>
        {error && <p role="alert">{error}</p>}
      </form>
    </section>
  );
}
