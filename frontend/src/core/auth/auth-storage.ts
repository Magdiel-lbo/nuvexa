const TOKEN_KEY = 'nuvexa.token'
const PERFIL_KEY = 'nuvexa.perfil'
const REFRESH_TOKEN_KEY = 'nuvexa.refreshToken'

export const authStorage = {
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY)
  },
  getPerfil(): string | null {
    return localStorage.getItem(PERFIL_KEY)
  },
  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY)
  },
  setSession(token: string, perfil: string, refreshToken: string): void {
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(PERFIL_KEY, perfil)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  },
  setTokens(token: string, refreshToken: string): void {
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  },
  clear(): void {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(PERFIL_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
  },
}
