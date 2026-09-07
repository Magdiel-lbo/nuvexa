import type { AxiosInstance } from 'axios'
import http from '../core/http/client'
import type { PacienteCreateRequest, PacienteEnumsResponse, PacienteResponse, PacienteUpdateRequest } from '../types/paciente'

class PacienteService {
  private http: AxiosInstance
  private enumsCache: Promise<PacienteEnumsResponse> | null = null

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async listar(busca?: string): Promise<PacienteResponse[]> {
    const { data } = await this.http.get('/pacientes', { params: busca ? { busca } : {} })
    return data
  }

  async buscarPorId(id: number): Promise<PacienteResponse> {
    const { data } = await this.http.get(`/pacientes/${id}`)
    return data
  }

  async criar(dados: PacienteCreateRequest): Promise<PacienteResponse> {
    const { data } = await this.http.post('/pacientes', dados)
    return data
  }

  async atualizar(id: number, dados: PacienteUpdateRequest): Promise<PacienteResponse> {
    const { data } = await this.http.put(`/pacientes/${id}`, dados)
    return data
  }

  enums(): Promise<PacienteEnumsResponse> {
    if (!this.enumsCache) {
      this.enumsCache = this.http.get('/pacientes/enums').then(({ data }) => data)
    }
    return this.enumsCache
  }
}

export default new PacienteService(http)
