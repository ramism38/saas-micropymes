import {
  type ReactNode,
  useEffect,
  useMemo,
  useState,
} from 'react';

import { ThemeContext } from './ThemeContext';
import type {
  ResolvedTheme,
  ThemePreference,
} from './theme.types';

/**
 * Clave utilizada para guardar la preferencia de tema en localStorage.
 *
 * Utilizar una constante evita repetir una cadena "mágica" en diferentes
 * partes del archivo y facilita cambiarla en el futuro.
 */
const THEME_STORAGE_KEY = 'saas-micropymes-theme';

/**
 * Media query estándar del navegador que permite conocer si el sistema
 * operativo del usuario está configurado en modo oscuro.
 */
const SYSTEM_DARK_MODE_QUERY = '(prefers-color-scheme: dark)';

/**
 * Comprueba si un valor recuperado de localStorage corresponde a una
 * preferencia de tema válida.
 *
 * localStorage siempre devuelve string | null. No debemos asumir que el
 * valor almacenado es correcto porque:
 *
 * - el usuario podría modificarlo manualmente;
 * - una versión anterior de la aplicación podría haber usado otros valores;
 * - podría existir información corrupta.
 */
function isThemePreference(value: string | null): value is ThemePreference {
  return value === 'light' || value === 'dark' || value === 'system';
}

/**
 * Recupera la preferencia guardada anteriormente por el usuario.
 *
 * Si todavía no existe ninguna preferencia válida, utilizamos "system".
 * De esta forma una cuenta nueva respeta automáticamente la configuración
 * visual de su sistema operativo.
 */
function getInitialThemePreference(): ThemePreference {
  const storedTheme = window.localStorage.getItem(THEME_STORAGE_KEY);

  if (isThemePreference(storedTheme)) {
    return storedTheme;
  }

  return 'system';
}

/**
 * Obtiene el tema que actualmente utiliza el sistema operativo.
 *
 * window.matchMedia permite consultar media queries desde JavaScript.
 *
 * Si la media query `(prefers-color-scheme: dark)` coincide, significa
 * que el sistema está en modo oscuro. En caso contrario usamos claro.
 */
function getSystemTheme(): ResolvedTheme {
  return window.matchMedia(SYSTEM_DARK_MODE_QUERY).matches
    ? 'dark'
    : 'light';
}

/**
 * Convierte una preferencia de usuario en el tema visual real.
 *
 * Ejemplos:
 *
 * preference = "light"
 * → "light"
 *
 * preference = "dark"
 * → "dark"
 *
 * preference = "system"
 * → depende de Windows/macOS/navegador.
 */
function resolveTheme(preference: ThemePreference): ResolvedTheme {
  if (preference === 'system') {
    return getSystemTheme();
  }

  return preference;
}

interface ThemeProviderProps {
  /**
   * Componentes de React que estarán dentro del proveedor.
   *
   * En nuestro caso será prácticamente toda la aplicación.
   */
  children: ReactNode;
}

/**
 * Proveedor global responsable de gestionar la apariencia de la aplicación.
 *
 * Sus responsabilidades son:
 *
 * 1. Recuperar la preferencia guardada del usuario.
 * 2. Resolver qué tema debe mostrarse realmente.
 * 3. Aplicar `data-theme` al elemento <html>.
 * 4. Guardar cualquier cambio en localStorage.
 * 5. Escuchar cambios del sistema operativo cuando se utiliza "system".
 *
 * Ningún componente de negocio debería implementar esta lógica de nuevo.
 * Los componentes simplemente consumirán el contexto mediante useTheme().
 */
export function ThemeProvider({ children }: ThemeProviderProps) {
  /**
   * Preferencia seleccionada por el usuario.
   *
   * useState acepta una función para calcular el estado inicial.
   * De esta forma consultamos localStorage solamente durante la
   * inicialización del componente.
   */
  const [theme, setTheme] = useState<ThemePreference>(
    getInitialThemePreference,
  );

  /**
   * Tema visual que finalmente se está aplicando.
   *
   * Se guarda por separado porque "system" necesita convertirse realmente
   * en "light" o "dark".
   */
  const [resolvedTheme, setResolvedTheme] = useState<ResolvedTheme>(() =>
    resolveTheme(getInitialThemePreference()),
  );

  /**
   * Este efecto se ejecuta cada vez que cambia la preferencia del usuario.
   *
   * Aquí hacemos tres cosas:
   *
   * 1. Calculamos qué tema debe mostrarse.
   * 2. Aplicamos ese tema al atributo data-theme del elemento <html>.
   * 3. Guardamos la preferencia original en localStorage.
   */
  useEffect(() => {
    const nextResolvedTheme = resolveTheme(theme);

    setResolvedTheme(nextResolvedTheme);

    document.documentElement.dataset.theme = nextResolvedTheme;

    window.localStorage.setItem(THEME_STORAGE_KEY, theme);
  }, [theme]);

  /**
   * Cuando el usuario ha seleccionado "system", debemos escuchar si el
   * sistema operativo cambia entre claro y oscuro mientras la aplicación
   * permanece abierta.
   *
   * Ejemplo:
   *
   * 18:00 → Windows está en claro.
   * 20:00 → Windows cambia automáticamente a oscuro.
   *
   * Si la preferencia es "system", nuestro SaaS también debe cambiar.
   *
   * Cuando el usuario selecciona explícitamente "light" o "dark" no
   * necesitamos escuchar estos cambios.
   */
  useEffect(() => {
    if (theme !== 'system') {
      return;
    }

    const mediaQuery = window.matchMedia(SYSTEM_DARK_MODE_QUERY);

    /**
     * Función ejecutada cada vez que cambia la configuración visual
     * del sistema operativo.
     */
    const handleSystemThemeChange = (event: MediaQueryListEvent) => {
      const nextResolvedTheme: ResolvedTheme = event.matches
        ? 'dark'
        : 'light';

      setResolvedTheme(nextResolvedTheme);

      document.documentElement.dataset.theme = nextResolvedTheme;
    };

    mediaQuery.addEventListener('change', handleSystemThemeChange);

    /**
     * Cleanup del efecto.
     *
     * React ejecuta esta función cuando:
     * - el componente se desmonta;
     * - o `theme` cambia y el efecto necesita configurarse nuevamente.
     *
     * Es importante eliminar listeners que ya no necesitamos para evitar
     * fugas de memoria y ejecuciones duplicadas.
     */
    return () => {
      mediaQuery.removeEventListener('change', handleSystemThemeChange);
    };
  }, [theme]);

  /**
   * useMemo evita crear un nuevo objeto de contexto innecesariamente
   * en cada renderizado.
   *
   * Solo se recalculará cuando cambie theme o resolvedTheme.
   */
  const contextValue = useMemo(
    () => ({
      theme,
      resolvedTheme,
      setTheme,
    }),
    [theme, resolvedTheme],
  );

  return (
    <ThemeContext.Provider value={contextValue}>
      {children}
    </ThemeContext.Provider>
  );
}