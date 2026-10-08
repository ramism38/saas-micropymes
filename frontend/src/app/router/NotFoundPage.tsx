import { Link } from 'react-router-dom';

/**
 * Página utilizada cuando ninguna de las rutas configuradas coincide
 * con la dirección solicitada por el usuario.
 *
 * Por ejemplo:
 *
 * /esto-no-existe
 *
 * mostrará esta página en lugar de dejar la aplicación en blanco.
 */
export function NotFoundPage() {
  return (
    <main>
      <h1>Página no encontrada</h1>

      <p>
        La dirección que has introducido no existe.
      </p>

      <Link to="/dashboard">
        Volver al Dashboard
      </Link>
    </main>
  );
}