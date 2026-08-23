import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../pages/auth/Login.vue'),
    meta: { public: true, heroTitleKey: 'auth.heroLoginTitulo', heroBodyKey: 'auth.heroLoginTexto' },
  },
  {
    path: '/cadastrar',
    name: 'cadastrar',
    component: () => import('../pages/auth/Cadastro.vue'),
    meta: { public: true, heroTitleKey: 'auth.heroCriarTitulo', heroBodyKey: 'auth.heroCriarTexto' },
  },
  {
    path: '/esqueci-senha',
    name: 'esqueci-senha',
    component: () => import('../pages/auth/EsqueciSenha.vue'),
    meta: { public: true, heroTitleKey: 'auth.heroEsqueciTitulo', heroBodyKey: 'auth.heroEsqueciTexto' },
  },
  {
    path: '/redefinir-senha',
    name: 'redefinir-senha',
    component: () => import('../pages/auth/RedefinirSenha.vue'),
    meta: { public: true, heroTitleKey: 'auth.heroRedefinirTitulo', heroBodyKey: 'auth.heroRedefinirTexto' },
  },
]

export default routes
