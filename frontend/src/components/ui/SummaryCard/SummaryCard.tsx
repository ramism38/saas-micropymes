import type {
  ComponentType,
  SVGProps,
} from 'react';

import './SummaryCard.css';

/**
 * Tipo genérico utilizado para recibir iconos compatibles con React.
 *
 * En nuestro caso utilizaremos componentes procedentes de lucide-react.
 */
type IconComponent = ComponentType<
  SVGProps<SVGSVGElement> & {
    size?: number | string;
    strokeWidth?: number | string;
  }
>;

/**
 * Variantes visuales disponibles.
 *
 * No representan colores concretos directamente.
 *
 * Cada variante utiliza tokens semánticos definidos en CSS, por lo que
 * seguirá funcionando tanto en modo claro como oscuro.
 */
export type SummaryCardTone =
  | 'primary'
  | 'success'
  | 'warning'
  | 'danger';

interface SummaryCardProps {
  /**
   * Texto que explica qué representa el número.
   */
  label: string;

  /**
   * Valor principal de la tarjeta.
   */
  value: number;

  /**
   * Icono mostrado en la esquina superior.
   */
  icon: IconComponent;

  /**
   * Variante visual de la tarjeta.
   *
   * Por defecto utilizamos "primary".
   */
  tone?: SummaryCardTone;
}

/**
 * Tarjeta utilizada para representar métricas rápidas.
 *
 * Ejemplo:
 *
 * 4
 * Oportunidades abiertas
 *
 * Estos componentes NO conocen nada del backend.
 * Únicamente reciben datos y los representan visualmente.
 */
export function SummaryCard({
  label,
  value,
  icon: Icon,
  tone = 'primary',
}: SummaryCardProps) {
  return (
    <article className={`summary-card summary-card--${tone}`}>
      <div className="summary-card__icon">
        <Icon
          size={20}
          strokeWidth={1.9}
          aria-hidden="true"
        />
      </div>

      <div className="summary-card__content">
        <strong className="summary-card__value">
          {value}
        </strong>

        <span className="summary-card__label">
          {label}
        </span>
      </div>
    </article>
  );
}