import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/consultas',
    name: 'consultas',
    component: () => import('../pages/consultas/ConsultasDashboard.vue'),
    meta: {
      title: 'Consultas',
      description: 'Acompanhe as consultas agendadas, confirmadas e realizadas.',
    },
  },
  {
    path: '/consultas/:id/editar',
    name: 'consulta-editar',
    component: () => import('../pages/consultas/ConsultaEditar.vue'),
    meta: { title: 'Editar consulta' },
  },
  {
    path: '/consultas/:id',
    name: 'consulta-visualizar',
    component: () => import('../pages/consultas/ConsultaEditar.vue'),
    meta: { title: 'Consulta' },
  },
]

export default routes
