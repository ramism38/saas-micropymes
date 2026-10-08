import { useParams } from 'react-router-dom';

/**
 * Página de detalle de un cliente concreto.
 *
 * La URL contendrá el identificador del cliente:
 *
 * /customers/:customerId
 *
 * Ejemplo:
 *
 * /customers/550e8400-e29b-41d4-a716-446655440000
 *
 * `useParams()` permite recuperar los parámetros dinámicos incluidos
 * en la URL.
 */
export function CustomerDetailPage() {
  const { customerId } = useParams();

  return (
    <main>
      <h1>Detalle del cliente</h1>

      <p>
        ID del cliente: {customerId}
      </p>
    </main>
  );
}