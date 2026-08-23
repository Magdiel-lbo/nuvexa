import { defineStore } from 'pinia'
import { authStorage } from '../util/auth-storage'

interface AuthState {
  token: string | null
  perfil: string | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: authStorage.getToken(),
    perfil: authStorage.getPerfil(),
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
  },
  actions: {
    setSession(token: string, perfil: string) {
      this.token = token
      this.perfil = perfil
      authStorage.setSession(token, perfil)
    },
    logout() {
      this.token = null
      this.perfil = null
      authStorage.clear()
    },
  },
})
