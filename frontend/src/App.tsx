import { RouterProvider } from 'react-router-dom';

import { router } from './app/router/router';

/**
 * Componente raíz del frontend.
 *
 * Su responsabilidad es deliberadamente muy pequeña:
 *
 * conectar nuestra aplicación React con el router principal.
 *
 * `RouterProvider` observa la URL actual y decide qué página debe
 * renderizar.
 *
 * Ejemplos:
 *
 * /dashboard
 * -> DashboardPage
 *
 * /customers
 * -> CustomersPage
 *
 * /customers/123
 * -> CustomerDetailPage
 *
 * Mantener App pequeño evita convertirlo en un archivo gigantesco
 * donde terminemos mezclando navegación, lógica de negocio,
 * autenticación y componentes visuales.
 */
function App() {
  return <RouterProvider router={router} />;
}

export default App;