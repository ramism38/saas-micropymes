import { useContext } from 'react';

import { ThemeContext } from './ThemeContext';

/**
 * Hook personalizado para acceder al sistema de temas.
 *
 * Gracias a este hook los componentes no necesitan importar directamente
 * ThemeContext ni repetir `useContext(ThemeContext)` cada vez.
 *
 * Ejemplo:
 *
 * const { theme, setTheme } = useTheme();
 *
 * setTheme('dark');
 */
export function useTheme() {
  const context = useContext(ThemeContext);

  /**
   * Si context es undefined significa que el componente está intentando
   * utilizar useTheme() fuera de <ThemeProvider>.
   *
   * En lugar de fallar posteriormente de una manera difícil de entender,
   * mostramos un mensaje de error explícito para el desarrollador.
   */
  if (context === undefined) {
    throw new Error(
      'useTheme must be used inside a ThemeProvider.',
    );
  }

  return context;
}