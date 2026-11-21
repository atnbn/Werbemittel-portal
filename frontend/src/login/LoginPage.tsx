import { useState, type FormEvent } from 'react';
import { ApiError, createApiClient, type ApiClient } from '../api/client.ts';
import { errorDetail } from '../api/errorDetail.ts';
import type { MeResponse } from '../api/types.ts';

interface LoginPageProps {
  onLogin: (api: ApiClient, me: MeResponse) => void;
}

export function LoginPage({ onLogin }: LoginPageProps) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    const api = createApiClient({ username, password });
    try {
      onLogin(api, await api.getMe());
    } catch (e) {
      setError(e instanceof ApiError && e.status === 401 ? 'Anmeldung fehlgeschlagen.' : errorDetail(e));
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <label>
        Benutzername
        <input
          name="username"
          autoComplete="username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />
      </label>
      <label>
        Passwort
        <input
          name="password"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
      </label>
      <button type="submit">Anmelden</button>
      {error && <p role="alert">{error}</p>}
    </form>
  );
}
