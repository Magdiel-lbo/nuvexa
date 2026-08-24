import { createApp } from 'vue'
import { createPinia } from 'pinia'
import './style.css'

// Bootstrap: cada vertical se registra antes do router (importado abaixo) montar suas rotas.
import './verticals/nutricao/nutricao.vertical'

import App from './App.vue'
import router from './router'
import vuetify from './plugins/vuetify'
import i18n from './plugins/i18n'
import './plugins/chartjs'

createApp(App).use(createPinia()).use(router).use(vuetify).use(i18n).mount('#app')
