import 'vuetify/styles'
import '@mdi/font/css/materialdesignicons.css'
import { createVuetify } from 'vuetify'
import { pt } from 'vuetify/locale'
import { themeStorage } from '../util/theme-storage'

const savedTheme = themeStorage.getTheme()
const defaultTheme = savedTheme === 'fitLight' || savedTheme === 'fitDark' ? savedTheme : 'fitDark'

export default createVuetify({
  locale: {
    locale: 'pt',
    messages: { pt },
  },
  defaults: {
    VTextField: { variant: 'underlined' },
    VSelect: { variant: 'underlined' },
    VAutocomplete: { variant: 'underlined' },
    VCombobox: { variant: 'underlined' },
    VTextarea: { variant: 'underlined' },
  },
  theme: {
    defaultTheme,
    themes: {
      fitDark: {
        dark: true,
        colors: {
          background: '#04120d',
          surface: '#0c1d17',
          'surface-variant': '#14261f',
          'on-surface': '#e8f2ed',
          'on-background': '#e8f2ed',
          'on-surface-variant': '#8fa69c',
          outline: '#2c4038',
          primary: '#37d59f',
          'primary-darken-1': '#117555',
          secondary: '#0b4a44',
          error: '#f2666b',
          success: '#37d59f',
          warning: '#f5a524',
        },
      },
      fitLight: {
        dark: false,
        colors: {
          background: '#f4f7f6',
          surface: '#ffffff',
          'surface-variant': '#eef2f1',
          'on-surface': '#101820',
          'on-background': '#101820',
          'on-surface-variant': '#5b6b66',
          outline: '#d9e0de',
          primary: '#0f9d75',
          'primary-darken-1': '#0b7a5c',
          secondary: '#144a73',
          error: '#d6373c',
          success: '#0f9d75',
          warning: '#c98416',
        },
        variables: {
          'disabled-opacity': 0.6,
          'medium-emphasis-opacity': 0.75,
        },
      },
    },
  },
})
