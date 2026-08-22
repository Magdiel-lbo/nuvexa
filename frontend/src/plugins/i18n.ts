import { createI18n } from 'vue-i18n'
import ptBR from '../translations/pt-BR'

export default createI18n({
  legacy: false,
  locale: 'pt-BR',
  fallbackLocale: 'pt-BR',
  messages: {
    'pt-BR': ptBR,
  },
})
