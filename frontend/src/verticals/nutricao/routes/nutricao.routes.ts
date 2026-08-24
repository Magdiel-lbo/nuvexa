import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/pacientes',
    name: 'pacientes',
    component: () => import('../pages/PacienteLista.vue'),
    meta: { title: 'Pacientes' },
  },
  {
    path: '/pacientes/novo',
    name: 'paciente-novo',
    component: () => import('../pages/PacienteFormulario.vue'),
    meta: { title: 'Novo paciente' },
  },
  {
    path: '/pacientes/:id/editar',
    name: 'paciente-editar',
    component: () => import('../pages/PacienteFormulario.vue'),
    meta: { title: 'Editar paciente' },
  },
  {
    path: '/pacientes/:id',
    name: 'paciente-detalhe',
    component: () => import('../pages/PacienteDetalhe.vue'),
    meta: { title: 'Paciente' },
  },
  {
    path: '/relatorios',
    name: 'relatorios',
    component: () => import('../pages/RelatorioPacientes.vue'),
    meta: { title: 'Relatórios' },
  },
]

export default routes
