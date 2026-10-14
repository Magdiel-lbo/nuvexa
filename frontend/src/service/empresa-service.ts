import type { AxiosInstance } from 'axios'
import http from '../core/http/client'
import type { Empresa, EmpresaEnums } from '../types/empresa'

class EmpresaService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async listar(): Promise<Empresa[]> {
    const { data } = await this.http.get('/organizacoes')
    return data
  }

  async enums(): Promise<EmpresaEnums> {
    const { data } = await this.http.get('/organizacoes/enums')
    return data
  }
}

export default new EmpresaService(http)
