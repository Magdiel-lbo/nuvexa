import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/patients',
    name: 'patients',
    component: () => import('../pages/patients/PatientLista.vue'),
    meta: { title: 'Pacientes' },
  },
  {
    path: '/patients/new',
    name: 'patient-new',
    component: () => import('../pages/patients/PatientFormulario.vue'),
    meta: { title: 'Novo paciente' },
  },
  {
    path: '/patients/:id/edit',
    name: 'patient-edit',
    component: () => import('../pages/patients/PatientFormulario.vue'),
    meta: { title: 'Editar paciente' },
  },
  {
    path: '/patients/:id',
    name: 'patient-view',
    component: () => import('../pages/patients/PatientDetalhe.vue'),
    meta: { title: 'Paciente' },
  },
]

export default routes
