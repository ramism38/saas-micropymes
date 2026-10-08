import {
  Building2,
  UserRound,
} from 'lucide-react';

import { ThemeSelector } from '../../ui/ThemeSelector/ThemeSelector';

import './Topbar.css';

/**
 * Barra superior principal de la zona privada del SaaS.
 *
 * Actualmente contiene:
 *
 * - selector visual de organización;
 * - selector de apariencia;
 * - acceso visual al perfil.
 *
 * En esta fase todavía NO tenemos conectadas las organizaciones reales
 * del usuario ni el perfil con el backend.
 *
 * El objetivo es construir ahora la interfaz correcta y sustituir los
 * datos temporales por datos reales durante la integración.
 */
export function Topbar() {
  return (
    <header className="topbar">
      {/*
       * La zona izquierda queda disponible.
       *
       * Más adelante podremos utilizarla para el botón que abre la
       * Sidebar en móvil.
       */}
      <div
        className="topbar__start"
        aria-hidden="true"
      />

      <div className="topbar__actions">
        {/*
         * ==========================================================
         * SELECTOR DE ORGANIZACIÓN
         * ==========================================================
         *
         * Actualmente solo existe una opción temporal ("Mi Empresa").
         *
         * En Fase 6 este select se alimentará con las organizaciones
         * reales a las que pertenece el usuario autenticado.
         *
         * El organizationId seleccionado terminará determinando qué
         * datos aparecen en Dashboard, Clientes, Oportunidades, etc.
         */}
        <div className="topbar__organization">
          <Building2
            className="topbar__organization-icon"
            size={18}
            strokeWidth={1.9}
            aria-hidden="true"
          />

          <select
            className="topbar__organization-select"
            aria-label="Organización activa"
            defaultValue="current"
          >
            <option value="current">
              Mi Empresa
            </option>
          </select>
        </div>

        {/*
         * Selector real de apariencia.
         *
         * Este control ya funciona completamente gracias a ThemeProvider.
         */}
        <ThemeSelector />

        {/*
         * ==========================================================
         * PERFIL
         * ==========================================================
         *
         * Todavía no tenemos pantalla ni acciones de perfil conectadas.
         *
         * Lo mantenemos deshabilitado temporalmente para no ofrecer
         * una interacción que realmente no hace nada.
         */}
        <button
          className="topbar__profile"
          type="button"
          disabled
          aria-label="Perfil de usuario"
          title="El perfil se habilitará más adelante"
        >
          <UserRound
            size={20}
            strokeWidth={1.9}
            aria-hidden="true"
          />
        </button>
      </div>
    </header>
  );
}