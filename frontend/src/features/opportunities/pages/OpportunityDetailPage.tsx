import { useParams } from 'react-router-dom';

/**
 * Página de detalle de una oportunidad.
 *
 * Esta terminará siendo una de las pantallas más importantes del SaaS.
 *
 * Incluirá las pestañas:
 * - Resumen;
 * - Presupuestos;
 * - Seguimientos;
 * - Actividad.
 *
 * El identificador de la oportunidad se obtiene directamente de la URL.
 */
export function OpportunityDetailPage() {
  const { opportunityId } = useParams();

  return (
    <main>
      <h1>Detalle de oportunidad</h1>

      <p>
        ID de la oportunidad: {opportunityId}
      </p>
    </main>
  );
}