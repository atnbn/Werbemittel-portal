import { useState } from 'react';
import type { ApiClient } from './api/client.ts';
import type { MeResponse } from './api/types.ts';
import { LoginPage } from './login/LoginPage.tsx';
import { OrderOverviewPage } from './orders/OrderOverviewPage.tsx';
import { PartnerPage } from './partner/PartnerPage.tsx';

// Zugangsdaten liegen nur im Closure von `api`, also nur im Speicher (Spec, Abschnitt 11).
interface Session {
  api: ApiClient;
  me: MeResponse;
}

export function App() {
  const [session, setSession] = useState<Session | null>(null);

  return (
    <main>
      <header>
        <h1>Mini-Marketingportal</h1>
        {session && (
          <button type="button" onClick={() => setSession(null)}>
            Abmelden
          </button>
        )}
      </header>
      {session === null ? (
        <LoginPage onLogin={(api, me) => setSession({ api, me })} />
      ) : session.me.role === 'PARTNER' ? (
        <PartnerPage api={session.api} />
      ) : (
        <OrderOverviewPage />
      )}
    </main>
  );
}
