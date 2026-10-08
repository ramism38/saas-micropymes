import {
  Building2,
  CalendarCheck,
  LayoutDashboard,
  LogOut,
  UserRound,
  UsersRound,
  BriefcaseBusiness,
} from 'lucide-react';

import { NavLink } from 'react-router-dom';

import './Sidebar.css';

/**
 * Representa una opción principal del menú lateral.
 *
 * Tener la navegación declarada como datos nos permite mantener el
 * componente limpio y facilita añadir nuevas secciones en el futuro.
 *
 * Ejemplo:
 *
 * {
 *   label: 'Clientes',
 *   to: '/customers',
 *   icon: UsersRound
 * }
 */
const navigationItems = [
  {
    label: 'Dashboard',
    to: '/dashboard',
    icon: LayoutDashboard,
  },
  {
    label: 'Clientes',
    to: '/customers',
    icon: UsersRound,
  },
  {
    label: 'Oportunidades',
    to: '/opportunities',
    icon: BriefcaseBusiness,
  },
  {
    label: 'Seguimientos',
    to: '/follow-ups',
    icon: CalendarCheck,
  },
];

/**
 * Sidebar principal de la zona privada del SaaS.
 *
 * Sus responsabilidades actuales son:
 *
 * - mostrar la identidad básica de la aplicación;
 * - permitir navegar entre las principales secciones;
 * - indicar visualmente qué ruta está activa;
 * - proporcionar acceso a la configuración de la organización.
 *
 * Más adelante también implementaremos:
 *
 * - versión móvil mediante un drawer;
 * - perfil real del usuario;
 * - cierre de sesión conectado al backend.
 *
 * La Sidebar NO contiene lógica de negocio.
 * Su responsabilidad es exclusivamente navegación y presentación.
 */
export function Sidebar() {
  return (
    <aside className="sidebar">
      {/*
       * ============================================================
       * MARCA / LOGO
       * ============================================================
       *
       * Por ahora utilizamos un pequeño icono junto al nombre.
       *
       * Más adelante podremos sustituirlo por un logotipo definitivo
       * sin modificar el resto de la Sidebar.
       */}
      <div className="sidebar__brand">
        <div
          className="sidebar__brand-icon"
          aria-hidden="true"
        >
          <Building2 size={20} strokeWidth={2} />
        </div>

        <div className="sidebar__brand-text">
          <strong>SaaS</strong>
          <span>Micropymes</span>
        </div>
      </div>

      {/*
       * ============================================================
       * NAVEGACIÓN PRINCIPAL
       * ============================================================
       *
       * NavLink pertenece a React Router.
       *
       * A diferencia de Link, NavLink sabe si su URL corresponde con
       * la página que está activa actualmente.
       *
       * Esto nos permite añadir automáticamente la clase:
       *
       * sidebar__nav-link--active
       *
       * cuando una sección está seleccionada.
       */}
      <nav
        className="sidebar__navigation"
        aria-label="Navegación principal"
      >
        {navigationItems.map((item) => {
          const Icon = item.icon;

          return (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                [
                  'sidebar__nav-link',
                  isActive
                    ? 'sidebar__nav-link--active'
                    : '',
                ]
                  .filter(Boolean)
                  .join(' ')
              }
            >
              {/*
               * El icono es decorativo porque el texto situado al lado
               * ya describe perfectamente el enlace.
               */}
              <Icon
                size={19}
                strokeWidth={1.9}
                aria-hidden="true"
              />

              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </nav>

      {/*
       * Separador visual entre las herramientas comerciales y la
       * administración de la organización.
       */}
      <div className="sidebar__separator" />

      {/*
       * ============================================================
       * ORGANIZACIÓN
       * ============================================================
       */}
      <nav
        className="sidebar__navigation"
        aria-label="Administración"
      >
        <NavLink
          to="/organization"
          className={({ isActive }) =>
            [
              'sidebar__nav-link',
              isActive
                ? 'sidebar__nav-link--active'
                : '',
            ]
              .filter(Boolean)
              .join(' ')
          }
        >
          <Building2
            size={19}
            strokeWidth={1.9}
            aria-hidden="true"
          />

          <span>Organización</span>
        </NavLink>
      </nav>

      {/*
       * ============================================================
       * ZONA INFERIOR
       * ============================================================
       *
       * Utilizamos margin-top: auto desde CSS para colocar esta sección
       * al final de la Sidebar independientemente de la altura de pantalla.
       *
       * Los botones todavía no ejecutan acciones reales porque todavía
       * no hemos integrado perfil ni autenticación con el backend.
       *
       * Los dejamos deshabilitados temporalmente para no ofrecer al usuario
       * acciones que aparentemente funcionan pero realmente no hacen nada.
       */}
      <div className="sidebar__footer">
        <button
          className="sidebar__footer-action"
          type="button"
          disabled
          title="El perfil se habilitará más adelante"
        >
          <UserRound
            size={18}
            strokeWidth={1.9}
            aria-hidden="true"
          />

          <span>Mi perfil</span>
        </button>

        <button
          className="sidebar__footer-action"
          type="button"
          disabled
          title="El cierre de sesión se conectará con el backend más adelante"
        >
          <LogOut
            size={18}
            strokeWidth={1.9}
            aria-hidden="true"
          />

          <span>Cerrar sesión</span>
        </button>
      </div>
    </aside>
  );
}