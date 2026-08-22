import { defineStore } from 'pinia'
import { authStorage } from '../util/auth-storage'

interface AuthState {
  token: string | null
  role: string | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: authStorage.getToken(),
    role: authStorage.getRole(),
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
  },
  actions: {
    setSession(token: string, role: string) {
      this.token = token
      this.role = role
      authStorage.setSession(token, role)
    },
    logout() {
      this.token = null
      this.role = null
      authStorage.clear()
    },
  },
})
