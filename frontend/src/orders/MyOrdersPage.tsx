import { useEffect, useState } from 'react';
import type { ApiClient } from '../api/client.ts';
import { errorDetail } from '../api/errorDetail.ts';
import type { Order } from '../api/types.ts';
import { formatCents } from '../format/money.ts';

interface MyOrdersPageProps {
  api: ApiClient;
}

export function MyOrdersPage({ api }: MyOrdersPageProps) {
  const [orders, setOrders] = useState<Order[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    api
      .getOrders()
      .then((loaded) => {
        if (active) setOrders(loaded);
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
      <h2>Meine Bestellungen</h2>
      {error ? (
        <p role="alert">{error}</p>
      ) : orders === null ? (
        <p>Lädt …</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Vorlage</th>
              <th>Menge</th>
              <th>Status</th>
              <th>Zuschuss</th>
              <th>Eigenanteil</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => (
              <tr key={order.id}>
                <td>{order.templateName}</td>
                <td>{order.quantity}</td>
                <td>{order.status}</td>
                <td>{formatCents(order.subsidyCents)}</td>
                <td>{formatCents(order.ownShareCents)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
