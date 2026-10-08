import {
  createBrowserRouter,
  Navigate,
} from 'react-router-dom';

import { AppShell } from '../../components/layout/AppShell/AppShell';

import { LoginPage } from '../../features/auth/pages/LoginPage';
import { RegisterPage } from '../../features/auth/pages/RegisterPage';

import { CustomerDetailPage } from '../../features/customers/pages/CustomerDetailPage';
import { CustomersPage } from '../../features/customers/pages/CustomersPage';

import { DashboardPage } from '../../features/dashboard/pages/DashboardPage';

import { FollowUpsPage } from '../../features/follow-ups/pages/FollowUpsPage';

import { OpportunitiesPage } from '../../features/opportunities/pages/OpportunitiesPage';
import { OpportunityDetailPage } from '../../features/opportunities/pages/OpportunityDetailPage';

import { OrganizationPage } from '../../features/organization/pages/OrganizationPage';

import { NotFoundPage } from './NotFoundPage';

/**
 * Router principal del frontend.
 *
 * Las rutas están divididas conceptualmente en dos grupos:
 *
 * 1. Rutas públicas.
 *    No utilizan AppShell.
 *
 *    Ejemplos:
 *    - /login
 *    - /register
 *
 * 2. Rutas de aplicación.
 *    Comparten AppShell y, por tanto, Sidebar + Topbar.
 *
 *    Ejemplos:
 *    - /dashboard
 *    - /customers
 *    - /opportunities
 *
 * Más adelante este segundo grupo será también protegido mediante
 * autenticación real.
 */
export const router = createBrowserRouter([
  /**
   * Ruta raíz.
   *
   * Temporalmente enviamos directamente al Dashboard.
   *
   * Cuando integremos autenticación:
   *
   * sesión existente    -> /dashboard
   * sin sesión          -> /login
   */
  {
    path: '/',
    element: (
      <Navigate
        to="/dashboard"
        replace
      />
    ),
  },

  /**
   * ============================================================
   * RUTAS PÚBLICAS
   * ============================================================
   *
   * Login y registro no utilizan AppShell.
   *
   * Esto es importante porque estas pantallas tendrán su propio diseño
   * centrado y no necesitan Sidebar ni Topbar.
   */
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/register',
    element: <RegisterPage />,
  },

  /**
   * ============================================================
   * RUTAS DE LA APLICACIÓN
   * ============================================================
   *
   * Todas estas rutas comparten el mismo AppShell.
   *
   * React Router renderizará AppShell una sola vez y colocará la página
   * correspondiente dentro de su <Outlet />.
   */
  {
    element: <AppShell />,

    children: [
      /**
       * Dashboard.
       */
      {
        path: '/dashboard',
        element: <DashboardPage />,
      },

      /**
       * Clientes.
       */
      {
        path: '/customers',
        element: <CustomersPage />,
      },
      {
        path: '/customers/:customerId',
        element: <CustomerDetailPage />,
      },

      /**
       * Oportunidades.
       */
      {
        path: '/opportunities',
        element: <OpportunitiesPage />,
      },
      {
        path: '/opportunities/:opportunityId',
        element: <OpportunityDetailPage />,
      },

      /**
       * Seguimientos.
       */
      {
        path: '/follow-ups',
        element: <FollowUpsPage />,
      },

      /**
       * Organización.
       */
      {
        path: '/organization',
        element: <OrganizationPage />,
      },
    ],
  },

  /**
   * ============================================================
   * 404
   * ============================================================
   *
   * Cualquier URL que no coincida con las anteriores termina aquí.
   */
  {
    path: '*',
    element: <NotFoundPage />,
  },
]);