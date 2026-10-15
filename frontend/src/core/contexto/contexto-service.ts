import type { AxiosInstance } from 'axios'
import http from '../http/client'
import type { ContextoResponse, OrganizacaoDoUsuario } from './contexto'

class ContextoService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async atual(): Promise<ContextoResponse> {
    const { data } = await this.http.get('/contexto')
    return data
  }

  async organizacoes(): Promise<OrganizacaoDoUsuario[]> {
    const { data } = await this.http.get('/contexto/organizacoes')
    return data
  }
}

export default new ContextoService(http)
