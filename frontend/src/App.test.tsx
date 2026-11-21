import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { App } from './App.tsx';

describe('App (smoke)', () => {
  it('renders the portal heading', () => {
    render(<App />);

    expect(screen.getByRole('heading', { name: 'Mini-Marketingportal' })).toBeInTheDocument();
  });
});
