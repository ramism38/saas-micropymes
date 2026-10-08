import type { ChangeEvent } from 'react';
import {
  Monitor,
  Moon,
  Sun,
} from 'lucide-react';

import { useTheme } from '../../../app/providers/theme/useTheme';
import type { ThemePreference } from '../../../app/providers/theme/theme.types';

import './ThemeSelector.css';

/**
 * Comprueba que un string recibido desde el <select> corresponde
 * realmente con uno de los temas soportados por la aplicación.
 *
 * Aunque nosotros controlamos las opciones del selector, realizar esta
 * comprobación evita depender de conversiones de tipo inseguras como:
 *
 *   value as ThemePreference
 *
 * y protege el componente frente a futuros cambios.
 */
function isThemePreference(value: string): value is ThemePreference {
  return value === 'light' || value === 'dark' || value === 'system';
}

/**
 * Devuelve el icono correspondiente a la preferencia seleccionada.
 *
 * Importante:
 *
 * Si theme === "system" mostramos el icono de monitor aunque el tema
 * resuelto actualmente sea oscuro. Queremos representar la preferencia
 * seleccionada por el usuario, no el resultado final.
 */
function getThemeIcon(theme: ThemePreference) {
  switch (theme) {
    case 'light':
      return Sun;

    case 'dark':
      return Moon;

    case 'system':
    default:
      return Monitor;
  }
}

/**
 * Selector de apariencia de la aplicación.
 *
 * Permite al usuario elegir entre:
 *
 * - Sistema
 * - Claro
 * - Oscuro
 *
 * Toda la lógica real de persistencia y aplicación del tema sigue
 * perteneciendo a ThemeProvider.
 *
 * Este componente únicamente actúa como interfaz para modificar esa
 * preferencia mediante `setTheme`.
 */
export function ThemeSelector() {
  const { theme, setTheme } = useTheme();

  /**
   * Elegimos dinámicamente qué componente de Lucide debe mostrarse
   * junto al selector.
   */
  const ThemeIcon = getThemeIcon(theme);

  /**
   * Se ejecuta cuando el usuario cambia la opción del <select>.
   */
  const handleThemeChange = (
    event: ChangeEvent<HTMLSelectElement>,
  ) => {
    const selectedTheme = event.target.value;

    if (isThemePreference(selectedTheme)) {
      setTheme(selectedTheme);
    }
  };

  return (
    <div className="theme-selector">
      {/*
       * El icono es decorativo porque el propio selector ya tiene
       * una etiqueta accesible mediante aria-label.
       */}
      <ThemeIcon
        className="theme-selector__icon"
        size={18}
        strokeWidth={1.9}
        aria-hidden="true"
      />

      <select
        className="theme-selector__select"
        aria-label="Seleccionar apariencia"
        value={theme}
        onChange={handleThemeChange}
      >
        <option value="system">
          Sistema
        </option>

        <option value="light">
          Claro
        </option>

        <option value="dark">
          Oscuro
        </option>
      </select>
    </div>
  );
}