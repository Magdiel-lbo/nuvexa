import type { RouteRecordRaw } from 'vue-router'

// O relatório de pacientes real (/relatorios) já existe em nutricao.routes.ts e não é
// duplicado aqui — este arquivo só reúne os relatórios ainda não implementados.
const routes: RouteRecordRaw[] = [
  {
    path: '/relatorios/consultas',
    name: 'relatorio-consultas',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Relatório de consultas' },
  },
  {
    path: '/relatorios/evolucao-nutricional',
    name: 'relatorio-evolucao-nutricional',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Evolução nutricional' },
  },
  {
    path: '/relatorios/financeiro',
    name: 'relatorio-financeiro',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Relatório financeiro' },
  },
  {
    path: '/relatorios/notas-fiscais',
    name: 'relatorio-notas-fiscais',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Notas fiscais' },
  },
]

export default routes
