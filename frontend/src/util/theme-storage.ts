const THEME_KEY = 'nuvexa.theme'

export const themeStorage = {
  getTheme(): string | null {
    return localStorage.getItem(THEME_KEY)
  },
  setTheme(theme: string): void {
    localStorage.setItem(THEME_KEY, theme)
  },
}
