import axios from 'axios'
import configs from '../../configs/env'
import { authStorage } from '../auth/auth-storage'
import { useAppStore } from '../../store/app.store'
import i18n from '../../plugins/i18n'

const http = axios.create({
  baseURL: configs.apiBaseUrl,
})

http.interceptors.request.use((config) => {
  const token = authStorage.getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const isAuthRequest = error.config?.url?.startsWith('/auth/')
    if (error.response?.status === 401 && !isAuthRequest) {
      authStorage.clear()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }

    // 403 é decisão de autorização do backend (papel insuficiente ou usuário sem vínculo):
    // não desloga, só avisa. Quem chamou ainda recebe o erro para tratar o próprio fluxo.
    if (error.response?.status === 403) {
      useAppStore().setToast({ mensagem: i18n.global.t('erro.acessoNegado'), erro: true })
    }

    return Promise.reject(error)
  },
)

export default http
