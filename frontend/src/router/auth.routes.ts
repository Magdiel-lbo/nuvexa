import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../pages/auth/Login.vue'),
    meta: { public: true },
  },
  {
    path: '/cadastrar',
    name: 'cadastrar',
    component: () => import('../pages/auth/Cadastro.vue'),
    meta: { public: true },
  },
  {
    path: '/esqueci-senha',
    name: 'esqueci-senha',
    component: () => import('../pages/auth/EsqueciSenha.vue'),
    meta: { public: true },
  },
  {
    path: '/redefinir-senha',
    name: 'redefinir-senha',
    component: () => import('../pages/auth/RedefinirSenha.vue'),
    meta: { public: true },
  },
]

export default routes
