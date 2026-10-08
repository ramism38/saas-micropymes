/**
 * Preferencia de tema que puede seleccionar el usuario.
 *
 * - "light": fuerza siempre el tema claro.
 * - "dark": fuerza siempre el tema oscuro.
 * - "system": utiliza la preferencia del sistema operativo.
 *
 * Es importante diferenciar esta preferencia del tema que finalmente
 * se está mostrando. Por ejemplo, el usuario puede tener seleccionado
 * "system", pero el tema visual resuelto puede ser "dark".
 */
export type ThemePreference = 'light' | 'dark' | 'system';

/**
 * Tema visual que realmente se está aplicando a la interfaz.
 *
 * Nunca puede ser "system", porque el navegador necesita terminar
 * mostrando realmente una de las dos variantes visuales disponibles.
 */
export type ResolvedTheme = 'light' | 'dark';

/**
 * Valores y funciones que estarán disponibles para cualquier componente
 * que consuma nuestro ThemeContext.
 */
export interface ThemeContextValue {
  /**
   * Preferencia elegida por el usuario.
   *
   * Ejemplo:
   * theme === "system"
   */
  theme: ThemePreference;

  /**
   * Tema que se está mostrando realmente.
   *
   * Ejemplo:
   * theme === "system"
   * resolvedTheme === "dark"
   */
  resolvedTheme: ResolvedTheme;

  /**
   * Permite cambiar la preferencia del usuario.
   *
   * ThemeProvider será responsable de:
   * - guardar el nuevo valor;
   * - aplicar el tema correspondiente;
   * - actualizar el DOM.
   */
  setTheme: (theme: ThemePreference) => void;
}