import { createContext } from 'react';

import type { ThemeContextValue } from './theme.types';

/**
 * Contexto global del sistema de temas.
 *
 * React Context permite compartir información entre componentes sin tener
 * que pasarla manualmente mediante props por toda la aplicación.
 *
 * En nuestro caso almacenará:
 * - la preferencia del usuario;
 * - el tema visual actualmente resuelto;
 * - la función utilizada para cambiar de tema.
 *
 * El valor inicial es `undefined` intencionadamente.
 *
 * Esto nos permitirá detectar si alguien intenta utilizar el sistema de
 * temas fuera de <ThemeProvider> y mostrar un error claro durante el
 * desarrollo.
 */
export const ThemeContext = createContext<ThemeContextValue | undefined>(
  undefined,
);