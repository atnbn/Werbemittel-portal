import { useEffect, useState } from 'react';
import type { ApiClient } from '../api/client.ts';
import { errorDetail } from '../api/errorDetail.ts';
import type { Order, OrderStatus } from '../api/types.ts';
import { formatCents } from '../format/money.ts';
import { nextStatus } from './nextStatus.ts';

interface OrderOverviewPageProps {
  api: ApiClient;
}

export function OrderOverviewPage({ api }: OrderOverviewPageProps) {
  const [orders, setOrders] = useState<Order[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [pendingId, setPendingId] = useState<number | null>(null);

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

  function changeStatus(order: Order, status: OrderStatus) {
    setPendingId(order.id);
    setError(null);
    api
      .updateOrderStatus(order.id, { status })
      .then((updated) => {
        setOrders((current) =>
          current === null ? current : current.map((o) => (o.id === updated.id ? updated : o)),
        );
      })
      .catch((e: unknown) => {
        setError(errorDetail(e));
      })
      .finally(() => {
        setPendingId(null);
      });
  }

  return (
    <section>
      <h2>Bestellübersicht</h2>
      {error && <p role="alert">{error}</p>}
      {orders === null ? (
        !error && <p>Lädt …</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Firmenname</th>
              <th>Vorlage</th>
              <th>Status</th>
              <th className="amount">Gesamtpreis</th>
              <th className="amount">Zuschuss</th>
              <th className="amount">Eigenanteil</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => {
              const next = nextStatus(order.status);
              return (
                <tr key={order.id}>
                  <td>{order.values['companyName'] ?? ''}</td>
                  <td>{order.templateName}</td>
                  <td>{order.status}</td>
                  <td className="amount">{formatCents(order.totalCents)}</td>
                  <td className="amount">{formatCents(order.subsidyCents)}</td>
                  <td className="amount">{formatCents(order.ownShareCents)}</td>
                  <td>
                    {next && (
                      <button
                        type="button"
                        disabled={pendingId === order.id}
                        onClick={() => changeStatus(order, next)}
                      >
                        Auf {next} setzen
                      </button>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      )}
    </section>
  );
}
