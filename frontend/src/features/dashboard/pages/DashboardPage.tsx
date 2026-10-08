import {
  AlertTriangle,
  BriefcaseBusiness,
  CalendarCheck,
  Clock3,
} from 'lucide-react';

import { PageHeader } from '../../../components/ui/PageHeader/PageHeader';
import { SummaryCard } from '../../../components/ui/SummaryCard/SummaryCard';

import {
  PriorityOpportunitiesList,
  type DashboardPriorityOpportunity,
} from '../components/PriorityOpportunitiesList';

import {
  TodayFollowUpsList,
  type DashboardTodayFollowUp,
} from '../components/TodayFollowUpsList';

import '../Dashboard.css';

/**
 * ============================================================
 * DATOS TEMPORALES
 * ============================================================
 *
 * Toda esta información existe únicamente para poder construir y probar
 * la interfaz antes de conectar el backend.
 *
 * Durante la Fase 6 estos objetos desaparecerán y los datos procederán
 * de nuestros endpoints reales.
 */

const dashboardSummary = {
  openOpportunities: 4,
  pendingFollowUps: 7,
  overdueFollowUps: 2,
  todayFollowUps: 3,
};

/**
 * Oportunidades prioritarias de ejemplo.
 *
 * Los IDs son temporales.
 *
 * Si pulsamos una fila navegaremos a:
 *
 * /opportunities/opportunity-1
 *
 * y nuestra ruta dinámica seguirá funcionando aunque todavía no exista
 * esa oportunidad realmente en PostgreSQL.
 */
const priorityOpportunities: DashboardPriorityOpportunity[] = [
  {
    id: 'opportunity-1',
    title: 'Renovación anual',
    customerName: 'ACME SL',
    priorityLevel: 'URGENT',
    priorityScore: 82,
  },
  {
    id: 'opportunity-2',
    title: 'Nuevo sistema de gestión',
    customerName: 'TechNova',
    priorityLevel: 'HIGH',
    priorityScore: 63,
  },
  {
    id: 'opportunity-3',
    title: 'Proyecto web corporativo',
    customerName: 'Demo SL',
    priorityLevel: 'MEDIUM',
    priorityScore: 41,
  },
];

/**
 * Seguimientos programados para hoy.
 *
 * `scheduledTime` es temporalmente una cadena preparada para mostrar.
 *
 * Cuando conectemos el backend trabajaremos con fechas reales y
 * realizaremos el formateo en frontend.
 */
const todayFollowUps: DashboardTodayFollowUp[] = [
  {
    id: 'follow-up-1',
    opportunityId: 'opportunity-1',
    title: 'Llamar a Juan Pérez',
    opportunityTitle: 'Renovación anual',
    customerName: 'ACME SL',
    scheduledTime: '10:30',
    type: 'CALL',
  },
  {
    id: 'follow-up-2',
    opportunityId: 'opportunity-3',
    title: 'Enviar propuesta revisada',
    opportunityTitle: 'Proyecto web corporativo',
    customerName: 'Demo SL',
    scheduledTime: '12:00',
    type: 'EMAIL',
  },
  {
    id: 'follow-up-3',
    opportunityId: 'opportunity-2',
    title: 'Reunión de revisión',
    opportunityTitle: 'Nuevo sistema de gestión',
    customerName: 'TechNova',
    scheduledTime: '16:00',
    type: 'MEETING',
  },
];

/**
 * Página principal de la aplicación.
 *
 * Su objetivo es ofrecer una fotografía rápida y operativa del trabajo
 * comercial pendiente.
 *
 * La prioridad no es mostrar gráficos decorativos, sino responder:
 *
 * "¿Qué necesita mi atención hoy?"
 */
export function DashboardPage() {
  return (
    <section className="dashboard">
      <PageHeader
        title="Dashboard"
        description="Aquí tienes un resumen de tu actividad comercial."
      />

      {/*
       * ============================================================
       * RESUMEN NUMÉRICO
       * ============================================================
       */}
      <div className="dashboard__summary-grid">
        <SummaryCard
          label="Oportunidades abiertas"
          value={dashboardSummary.openOpportunities}
          icon={BriefcaseBusiness}
          tone="primary"
        />

        <SummaryCard
          label="Seguimientos pendientes"
          value={dashboardSummary.pendingFollowUps}
          icon={Clock3}
          tone="primary"
        />

        <SummaryCard
          label="Seguimientos vencidos"
          value={dashboardSummary.overdueFollowUps}
          icon={AlertTriangle}
          tone="danger"
        />

        <SummaryCard
          label="Para hoy"
          value={dashboardSummary.todayFollowUps}
          icon={CalendarCheck}
          tone="success"
        />
      </div>

      {/*
       * ============================================================
       * CONTENIDO OPERATIVO
       * ============================================================
       *
       * A la izquierda mostramos las oportunidades más prioritarias.
       *
       * A la derecha mostramos las acciones comerciales programadas
       * para el día actual.
       */}
      <div className="dashboard__main-grid">
        <PriorityOpportunitiesList
          opportunities={priorityOpportunities}
        />

        <TodayFollowUpsList
          followUps={todayFollowUps}
        />
      </div>
    </section>
  );
}