import { defineStore } from 'pinia'
import vuetify from '../plugins/vuetify'
import { themeStorage } from '../util/theme-storage'

export const useThemeStore = defineStore('theme', {
  state: () => ({
    name: vuetify.theme.global.name.value as 'fitDark' | 'fitLight',
  }),
  getters: {
    isDark: (state) => state.name === 'fitDark',
  },
  actions: {
    toggleTheme() {
      this.name = this.name === 'fitDark' ? 'fitLight' : 'fitDark'
      vuetify.theme.global.name.value = this.name
      themeStorage.setTheme(this.name)
    },
  },
})
