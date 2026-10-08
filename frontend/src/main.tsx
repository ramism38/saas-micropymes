import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';

import { ThemeProvider } from './app/providers/theme/ThemeProvider';
import App from './App';
import './index.css';

/**
 * Punto de entrada de la aplicación React.
 *
 * ThemeProvider envuelve a <App /> para que cualquier componente de la
 * aplicación pueda consultar o modificar el tema mediante useTheme().
 *
 * StrictMode se mantiene porque ayuda a detectar determinados problemas
 * durante el desarrollo. No modifica el comportamiento de producción.
 */
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ThemeProvider>
      <App />
    </ThemeProvider>
  </StrictMode>,
);