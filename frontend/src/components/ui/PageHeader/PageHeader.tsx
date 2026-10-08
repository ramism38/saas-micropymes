import type { ReactNode } from 'react';

import './PageHeader.css';

/**
 * Propiedades aceptadas por PageHeader.
 *
 * El componente está pensado para reutilizarse como cabecera principal
 * de las diferentes páginas del SaaS.
 */
interface PageHeaderProps {
  /**
   * Título principal de la página.
   *
   * Ejemplos:
   * - "Dashboard"
   * - "Clientes"
   * - "Oportunidades"
   */
  title: string;

  /**
   * Texto secundario utilizado para explicar brevemente el propósito
   * de la página.
   */
  description?: string;

  /**
   * Zona opcional reservada para acciones.
   *
   * Ejemplos futuros:
   *
   * <Button>Nuevo cliente</Button>
   * <Button>Nueva oportunidad</Button>
   */
  actions?: ReactNode;
}

/**
 * Cabecera reutilizable para las páginas principales.
 *
 * Mantener esta estructura en un único componente evita que cada feature
 * defina sus propios tamaños, márgenes y distribución.
 */
export function PageHeader({
  title,
  description,
  actions,
}: PageHeaderProps) {
  return (
    <header className="page-header">
      <div className="page-header__content">
        <h1 className="page-header__title">
          {title}
        </h1>

        {description && (
          <p className="page-header__description">
            {description}
          </p>
        )}
      </div>

      {actions && (
        <div className="page-header__actions">
          {actions}
        </div>
      )}
    </header>
  );
}