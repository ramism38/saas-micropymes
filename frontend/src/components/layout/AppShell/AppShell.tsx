import { Outlet } from 'react-router-dom';

import { Sidebar } from '../Sidebar/Sidebar';
import { Topbar } from '../Topbar/Topbar';

import './AppShell.css';

/**
 * Layout principal de las pantallas privadas del SaaS.
 *
 * Su responsabilidad es únicamente organizar las tres grandes áreas
 * de la interfaz:
 *
 * - Sidebar
 * - Topbar
 * - contenido correspondiente a la ruta actual
 *
 * La lógica específica de cada feature nunca debe vivir aquí.
 */
export function AppShell() {
  return (
    <div className="app-shell">
      {/*
       * Navegación principal de escritorio.
       */}
      <aside className="app-shell__sidebar">
        <Sidebar />
      </aside>

      {/*
       * Workspace que ocupa todo el espacio restante.
       */}
      <div className="app-shell__workspace">
        {/*
         * Barra superior reutilizable.
         */}
        <Topbar />

        {/*
         * React Router introduce dentro de Outlet la página
         * correspondiente a la URL activa.
         */}
        <main className="app-shell__content">
          <div className="app-shell__content-inner">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}