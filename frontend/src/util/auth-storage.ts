const TOKEN_KEY = 'nuvexa.token'
const PERFIL_KEY = 'nuvexa.perfil'

export const authStorage = {
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY)
  },
  getPerfil(): string | null {
    return localStorage.getItem(PERFIL_KEY)
  },
  setSession(token: string, perfil: string): void {
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(PERFIL_KEY, perfil)
  },
  clear(): void {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(PERFIL_KEY)
  },
}
