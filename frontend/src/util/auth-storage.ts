const TOKEN_KEY = 'nuvexa.token'
const ROLE_KEY = 'nuvexa.role'

export const authStorage = {
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY)
  },
  getRole(): string | null {
    return localStorage.getItem(ROLE_KEY)
  },
  setSession(token: string, role: string): void {
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(ROLE_KEY, role)
  },
  clear(): void {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(ROLE_KEY)
  },
}
