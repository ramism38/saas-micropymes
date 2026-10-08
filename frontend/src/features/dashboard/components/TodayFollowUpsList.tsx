import {
  ArrowRight,
  CalendarDays,
  CircleEllipsis,
  Mail,
  MessageCircle,
  Phone,
  Video,
} from 'lucide-react';

import { Link } from 'react-router-dom';

/**
 * Tipos de seguimiento soportados actualmente por nuestro dominio.
 *
 * Estos valores reflejan los tipos existentes en el backend.
 */
export type DashboardFollowUpType =
  | 'CALL'
  | 'EMAIL'
  | 'WHATSAPP'
  | 'MEETING'
  | 'OTHER';

/**
 * Información mínima que necesita el Dashboard para representar
 * un seguimiento programado para hoy.
 */
export interface DashboardTodayFollowUp {
  id: string;
  opportunityId: string;
  title: string;
  opportunityTitle: string;
  customerName: string;
  scheduledTime: string;
  type: DashboardFollowUpType;
}

interface TodayFollowUpsListProps {
  followUps: DashboardTodayFollowUp[];
}

/**
 * Devuelve el icono correspondiente a cada tipo de seguimiento.
 *
 * Mantener este mapeo en una única función evita repartir condiciones
 * por todo el JSX.
 */
function getFollowUpIcon(type: DashboardFollowUpType) {
  switch (type) {
    case 'CALL':
      return Phone;

    case 'EMAIL':
      return Mail;

    case 'WHATSAPP':
      return MessageCircle;

    case 'MEETING':
      return Video;

    case 'OTHER':
    default:
      return CircleEllipsis;
  }
}

/**
 * Traducciones visibles de los tipos de seguimiento.
 */
const followUpTypeLabels: Record<
  DashboardFollowUpType,
  string
> = {
  CALL: 'Llamada',
  EMAIL: 'Email',
  WHATSAPP: 'WhatsApp',
  MEETING: 'Reunión',
  OTHER: 'Otro',
};

/**
 * Panel del Dashboard encargado de mostrar las tareas comerciales
 * programadas para el día actual.
 *
 * De momento cada fila navega al detalle de la oportunidad.
 *
 * Más adelante podremos añadir la acción rápida "Completar" cuando
 * conectemos la API y tengamos manejo real del estado.
 */
export function TodayFollowUpsList({
  followUps,
}: TodayFollowUpsListProps) {
  return (
    <section className="dashboard-panel">
      <header className="dashboard-panel__header">
        <div>
          <h2 className="dashboard-panel__title">
            Seguimientos de hoy
          </h2>

          <p className="dashboard-panel__description">
            Contactos comerciales programados para hoy.
          </p>
        </div>

        <CalendarDays
          className="dashboard-panel__header-icon"
          size={20}
          strokeWidth={1.8}
          aria-hidden="true"
        />
      </header>

      {followUps.length === 0 ? (
        <div className="dashboard-panel__empty">
          No tienes seguimientos pendientes para hoy.
        </div>
      ) : (
        <div className="dashboard-follow-up-list">
          {followUps.map((followUp) => {
            const TypeIcon = getFollowUpIcon(followUp.type);

            return (
              <Link
                key={followUp.id}
                className="dashboard-follow-up-item"
                to={`/opportunities/${followUp.opportunityId}`}
              >
                {/*
                 * Hora del seguimiento.
                 */}
                <time className="dashboard-follow-up-item__time">
                  {followUp.scheduledTime}
                </time>

                {/*
                 * Icono que representa el canal de contacto.
                 */}
                <div className="dashboard-follow-up-item__icon">
                  <TypeIcon
                    size={17}
                    strokeWidth={1.9}
                    aria-hidden="true"
                  />
                </div>

                {/*
                 * Descripción de la tarea y contexto comercial.
                 */}
                <div className="dashboard-follow-up-item__content">
                  <strong className="dashboard-follow-up-item__title">
                    {followUp.title}
                  </strong>

                  <span className="dashboard-follow-up-item__context">
                    {followUp.opportunityTitle}
                    {' · '}
                    {followUp.customerName}
                  </span>
                </div>

                {/*
                 * Tipo textual.
                 */}
                <span className="dashboard-follow-up-item__type">
                  {followUpTypeLabels[followUp.type]}
                </span>

                <ArrowRight
                  className="dashboard-follow-up-item__arrow"
                  size={18}
                  strokeWidth={1.8}
                  aria-hidden="true"
                />
              </Link>
            );
          })}
        </div>
      )}

      <footer className="dashboard-panel__footer">
        <Link
          className="dashboard-panel__link"
          to="/follow-ups"
        >
          Ver todos los seguimientos
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