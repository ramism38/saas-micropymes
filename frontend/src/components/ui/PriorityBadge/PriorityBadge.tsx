import './PriorityBadge.css';

/**
 * Niveles de prioridad definidos por nuestra lógica de negocio.
 *
 * Estos valores coinciden conceptualmente con los niveles que calcula
 * el backend a partir del score de prioridad.
 */
export type PriorityLevel =
  | 'LOW'
  | 'MEDIUM'
  | 'HIGH'
  | 'URGENT';

interface PriorityBadgeProps {
  /**
   * Nivel de prioridad que queremos representar.
   */
  level: PriorityLevel;

  /**
   * Score numérico calculado por el sistema.
   *
   * Es opcional porque en algunas pantallas puede interesarnos mostrar
   * solamente el nivel:
   *
   * [ URGENT ]
   *
   * mientras que en otras, como el Dashboard, queremos mostrar:
   *
   * [ URGENT · 82 ]
   */
  score?: number;
}

/**
 * Traducciones visibles para el usuario.
 *
 * Internamente mantenemos los valores técnicos:
 *
 * LOW / MEDIUM / HIGH / URGENT
 *
 * pero la interfaz puede mostrar textos más naturales en español.
 */
const priorityLabels: Record<PriorityLevel, string> = {
  LOW: 'Baja',
  MEDIUM: 'Media',
  HIGH: 'Alta',
  URGENT: 'Urgente',
};

/**
 * Badge reutilizable para representar la prioridad comercial.
 *
 * El componente no decide directamente colores concretos.
 *
 * En CSS cada nivel utiliza colores semánticos procedentes del
 * Design System para mantener compatibilidad con claro y oscuro.
 */
export function PriorityBadge({
  level,
  score,
}: PriorityBadgeProps) {
  return (
    <span
      className={[
        'priority-badge',
        `priority-badge--${level.toLowerCase()}`,
      ].join(' ')}
    >
      <span>{priorityLabels[level]}</span>

      {score !== undefined && (
        <span className="priority-badge__score">
          · {score}
        </span>
      )}
    </span>
  );
}