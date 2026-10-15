import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/empresa',
    name: 'empresa',
    component: () => import('../pages/empresas/EmpresaLista.vue'),
    meta: { title: 'Empresas', perfil: 'ADMIN' },
  },
  {
    path: '/empresa/nova',
    name: 'empresa-nova',
    component: () => import('../pages/empresas/EmpresaFormulario.vue'),
    meta: { title: 'Nova empresa', perfil: 'ADMIN' },
  },
  {
    path: '/empresa/:id',
    name: 'empresa-editar',
    component: () => import('../pages/empresas/EmpresaFormulario.vue'),
    meta: { title: 'Editar empresa', perfil: 'ADMIN' },
  },
  {
    path: '/minha-empresa',
    name: 'minha-empresa',
    component: () => import('../pages/empresas/MinhaEmpresa.vue'),
    meta: { title: 'Minha empresa', permissao: 'editarOrganizacao' },
  },
  {
    path: '/equipe',
    name: 'equipe',
    component: () => import('../pages/equipe/Equipe.vue'),
    meta: { title: 'Equipe', permissao: 'gerenciarEquipe' },
  },
  { path: '/usuarios', redirect: '/equipe' },
  {
    path: '/vinculos-permissoes',
    name: 'vinculos-permissoes',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Vínculos e permissões' },
  },
  {
    path: '/notificacoes',
    name: 'notificacoes',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Notificações' },
  },
  {
    path: '/integracoes',
    name: 'integracoes',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Integrações' },
  },
  {
    path: '/ia',
    name: 'ia',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'IA' },
  },
  {
    path: '/financeiro',
    name: 'financeiro',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Financeiro' },
  },
]

export default routes
