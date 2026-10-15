import axios from 'axios'
import configs from '../../configs/env'
import { authStorage } from '../auth/auth-storage'
import { organizacaoStorage } from '../contexto/organizacao-storage'
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
  const organizacaoId = organizacaoStorage.get()
  if (organizacaoId !== null) {
    config.headers['X-Organizacao-Id'] = String(organizacaoId)
  }
  return config
})

// Uma renovação por vez: várias requisições que recebem 401 juntas esperam a mesma promessa, senão
// cada uma gastaria o refresh token (rotativo) e as demais seriam recusadas.
let renovacaoEmAndamento: Promise<string> | null = null

function renovarSessao(): Promise<string> {
  if (!renovacaoEmAndamento) {
    const refreshToken = authStorage.getRefreshToken()
    if (!refreshToken) {
      return Promise.reject(new Error('sem refresh token'))
    }
    renovacaoEmAndamento = axios
      .post(`${configs.apiBaseUrl}/auth/refresh`, { refreshToken })
      .then(({ data }) => {
        authStorage.setTokens(data.token, data.refreshToken)
        return data.token as string
      })
      .finally(() => {
        renovacaoEmAndamento = null
      })
  }
  return renovacaoEmAndamento
}

function encerrarSessao() {
  authStorage.clear()
  organizacaoStorage.clear()
  if (window.location.pathname !== '/login') {
    window.location.href = '/login'
  }
}

http.interceptors.response.use(
  (response) => response,
  async (error) => {
    const config = error.config
    const isAuthRequest = config?.url?.startsWith('/auth/')
    if (error.response?.status === 401 && !isAuthRequest) {
      if (!config._renovada && authStorage.getRefreshToken()) {
        config._renovada = true
        try {
          const novoToken = await renovarSessao()
          config.headers.Authorization = `Bearer ${novoToken}`
          return http(config)
        } catch {
          encerrarSessao()
          return Promise.reject(error)
        }
      }
      encerrarSessao()
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
