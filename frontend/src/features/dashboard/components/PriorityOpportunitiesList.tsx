import {
  ArrowRight,
  BriefcaseBusiness,
} from 'lucide-react';

import { Link } from 'react-router-dom';

import {
  PriorityBadge,
  type PriorityLevel,
} from '../../../components/ui/PriorityBadge/PriorityBadge';

/**
 * Información mínima que necesita esta lista para representar una
 * oportunidad prioritaria.
 *
 * Esta interfaz NO pretende reproducir todavía el DTO real del backend.
 *
 * Durante la Fase 6 sustituiremos estos datos temporales por el contrato
 * real de la API.
 */
export interface DashboardPriorityOpportunity {
  id: string;
  title: string;
  customerName: string;
  priorityLevel: PriorityLevel;
  priorityScore: number;
}

interface PriorityOpportunitiesListProps {
  /**
   * Oportunidades que queremos mostrar.
   *
   * El Dashboard real devolverá únicamente las más prioritarias.
   */
  opportunities: DashboardPriorityOpportunity[];
}

/**
 * Lista de oportunidades con mayor prioridad comercial.
 *
 * Cada fila es navegable y lleva directamente al detalle de la
 * oportunidad correspondiente.
 */
export function PriorityOpportunitiesList({
  opportunities,
}: PriorityOpportunitiesListProps) {
  return (
    <section className="dashboard-panel">
      {/*
       * Cabecera del panel.
       */}
      <header className="dashboard-panel__header">
        <div>
          <h2 className="dashboard-panel__title">
            Oportunidades prioritarias
          </h2>

          <p className="dashboard-panel__description">
            Las oportunidades que requieren más atención.
          </p>
        </div>

        <BriefcaseBusiness
          className="dashboard-panel__header-icon"
          size={20}
          strokeWidth={1.8}
          aria-hidden="true"
        />
      </header>

      {/*
       * Si no hay oportunidades mostramos un estado vacío sencillo.
       *
       * Más adelante construiremos un EmptyState genérico reutilizable.
       */}
      {opportunities.length === 0 ? (
        <div className="dashboard-panel__empty">
          No hay oportunidades prioritarias en este momento.
        </div>
      ) : (
        <div className="dashboard-priority-list">
          {opportunities.map((opportunity) => (
            <Link
              key={opportunity.id}
              className="dashboard-priority-item"
              to={`/opportunities/${opportunity.id}`}
            >
              {/*
               * Información principal de la oportunidad.
               */}
              <div className="dashboard-priority-item__content">
                <strong className="dashboard-priority-item__title">
                  {opportunity.title}
                </strong>

                <span className="dashboard-priority-item__customer">
                  {opportunity.customerName}
                </span>
              </div>

              {/*
               * Nivel y score calculado por el backend.
               */}
              <PriorityBadge
                level={opportunity.priorityLevel}
                score={opportunity.priorityScore}
              />

              {/*
               * Indicador visual de que la fila conduce a otra página.
               */}
              <ArrowRight
                className="dashboard-priority-item__arrow"
                size={18}
                strokeWidth={1.8}
                aria-hidden="true"
              />
            </Link>
          ))}
        </div>
      )}

      <footer className="dashboard-panel__footer">
        <Link
          className="dashboard-panel__link"
          to="/opportunities"
        >
          Ver todas las oportunidades
          <ArrowRight
            size={16}
            strokeWidth={1.8}
            aria-hidden="true"
          />
        </Link>
      </footer>
    </section>
  );
}