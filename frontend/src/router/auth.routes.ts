import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../pages/auth/Login.vue'),
    meta: { public: true, heroTitleKey: 'auth.heroLoginTitulo', heroBodyKey: 'auth.heroLoginTexto' },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../pages/auth/Register.vue'),
    meta: { public: true, heroTitleKey: 'auth.heroCriarTitulo', heroBodyKey: 'auth.heroCriarTexto' },
  },
  {
    path: '/forgot-password',
    name: 'forgot-password',
    component: () => import('../pages/auth/ForgotPassword.vue'),
    meta: { public: true, heroTitleKey: 'auth.heroEsqueciTitulo', heroBodyKey: 'auth.heroEsqueciTexto' },
  },
  {
    path: '/reset-password',
    name: 'reset-password',
    component: () => import('../pages/auth/ResetPassword.vue'),
    meta: { public: true, heroTitleKey: 'auth.heroRedefinirTitulo', heroBodyKey: 'auth.heroRedefinirTexto' },
  },
]

export default routes
