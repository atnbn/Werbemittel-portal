import { useState } from 'react';
import type { ApiClient } from '../api/client.ts';
import type { Partner, Template } from '../api/types.ts';
import { CatalogPage } from '../catalog/CatalogPage.tsx';
import { TemplateEditorPage } from '../editor/TemplateEditorPage.tsx';
import { MyOrdersPage } from '../orders/MyOrdersPage.tsx';

interface PartnerPageProps {
  api: ApiClient;
}

// Der Katalog wird bei jedem Öffnen neu gemountet und lädt Guthaben und Vorlagen frisch.
type View =
  | { name: 'catalog' }
  | { name: 'editor'; template: Template; partner: Partner }
  | { name: 'orders' };

export function PartnerPage({ api }: PartnerPageProps) {
  const [view, setView] = useState<View>({ name: 'catalog' });

  return (
    <>
      <nav className="app-nav">
        <button type="button" onClick={() => setView({ name: 'catalog' })}>
          Katalog
        </button>
        <button type="button" onClick={() => setView({ name: 'orders' })}>
          Meine Bestellungen
        </button>
      </nav>
      {view.name === 'catalog' ? (
        <CatalogPage
          api={api}
          onPersonalize={(template, partner) => setView({ name: 'editor', template, partner })}
        />
      ) : view.name === 'editor' ? (
        <TemplateEditorPage
          api={api}
          template={view.template}
          partner={view.partner}
          onOrdered={() => setView({ name: 'orders' })}
        />
      ) : (
        <MyOrdersPage api={api} />
      )}
    </>
  );
}
