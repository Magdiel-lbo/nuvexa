import type { AxiosInstance } from 'axios'
import http from '../core/http/client'
import type { Empresa, EmpresaCreateRequest, EmpresaEnums, EmpresaUpdateRequest } from '../types/empresa'
import type { Convite } from '../types/equipe'

class EmpresaService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async listar(): Promise<Empresa[]> {
    const { data } = await this.http.get('/organizacoes')
    return data
  }

  async buscarPorId(id: number): Promise<Empresa> {
    const { data } = await this.http.get(`/organizacoes/${id}`)
    return data
  }

  async criar(dados: EmpresaCreateRequest): Promise<Empresa> {
    const { data } = await this.http.post('/organizacoes', dados)
    return data
  }

  async atualizar(id: number, dados: EmpresaUpdateRequest): Promise<Empresa> {
    const { data } = await this.http.put(`/organizacoes/${id}`, dados)
    return data
  }

  async convidarProprietario(id: number, email: string): Promise<Convite> {
    const { data } = await this.http.post(`/organizacoes/${id}/proprietario`, { email })
    return data
  }

  async minhaEmpresa(): Promise<Empresa> {
    const { data } = await this.http.get('/minha-organizacao')
    return data
  }

  async atualizarMinhaEmpresa(dados: EmpresaUpdateRequest): Promise<Empresa> {
    const { data } = await this.http.put('/minha-organizacao', dados)
    return data
  }

  async enums(): Promise<EmpresaEnums> {
    const { data } = await this.http.get('/organizacoes/enums')
    return data
  }
}

export default new EmpresaService(http)
