import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/pacientes',
    name: 'pacientes',
    component: () => import('../pages/pacientes/PacienteLista.vue'),
    meta: { title: 'Pacientes' },
  },
  {
    path: '/pacientes/novo',
    name: 'paciente-novo',
    component: () => import('../pages/pacientes/PacienteFormulario.vue'),
    meta: { title: 'Novo paciente' },
  },
  {
    path: '/pacientes/:id/editar',
    name: 'paciente-editar',
    component: () => import('../pages/pacientes/PacienteFormulario.vue'),
    meta: { title: 'Editar paciente' },
  },
  {
    path: '/pacientes/:id',
    name: 'paciente-visualizar',
    component: () => import('../pages/pacientes/PacienteFormulario.vue'),
    meta: { title: 'Paciente' },
  },
]

export default routes
