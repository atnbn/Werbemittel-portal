import { useEffect, useState } from 'react';
import type { ApiClient } from '../api/client.ts';
import { errorDetail } from '../api/errorDetail.ts';
import type { Partner, Template } from '../api/types.ts';
import { formatCents } from '../format/money.ts';

interface CatalogPageProps {
  api: ApiClient;
  onPersonalize: (template: Template, partner: Partner) => void;
}

interface CatalogData {
  templates: Template[];
  partner: Partner;
}

export function CatalogPage({ api, onPersonalize }: CatalogPageProps) {
  const [data, setData] = useState<CatalogData | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    Promise.all([api.getTemplates(), api.getMyPartner()])
      .then(([templates, partner]) => {
        if (active) setData({ templates, partner });
      })
      .catch((e: unknown) => {
        if (active) setError(errorDetail(e));
      });
    return () => {
      active = false;
    };
  }, [api]);

  return (
    <section>
      <h2>Katalog</h2>
      {error ? (
        <p role="alert">{error}</p>
      ) : data === null ? (
        <p>Lädt …</p>
      ) : (
        <>
          <p className="balance">Guthaben: {formatCents(data.partner.balanceCents)}</p>
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Typ</th>
                <th className="amount">Stückpreis</th>
                <th className="amount">Zuschussquote</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {data.templates.map((template) => (
                <tr key={template.id}>
                  <td>{template.name}</td>
                  <td>{template.type}</td>
                  <td className="amount">{formatCents(template.unitPriceCents)}</td>
                  <td className="amount">{`${template.subsidyPercent} %`}</td>
                  <td>
                    <button type="button" onClick={() => onPersonalize(template, data.partner)}>
                      Personalisieren
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </section>
  );
}
