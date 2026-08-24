import type { AxiosInstance } from 'axios'
import http from '../http/client'
import type { ContextoResponse } from './contexto'

class ContextoService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async atual(): Promise<ContextoResponse> {
    const { data } = await this.http.get('/contexto')
    return data
  }
}

export default new ContextoService(http)
