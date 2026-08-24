import type { AxiosInstance } from 'axios'
import http from '../../../core/http/client'
import type {
  PacienteCreateRequest,
  PacienteEnumsResponse,
  PacienteRelatorioFiltro,
  PacienteRelatorioResponse,
  PacienteResponse,
  PacienteUpdateRequest,
} from '../types/paciente'

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

  async salvar(id: number | null, dto: PacienteCreateRequest | PacienteUpdateRequest): Promise<PacienteResponse> {
    if (id) {
      const { data } = await this.http.put(`/pacientes/${id}`, dto)
      return data
    }
    const { data } = await this.http.post('/pacientes', dto)
    return data
  }

  async remover(id: number): Promise<void> {
    await this.http.delete(`/pacientes/${id}`)
  }

  enums(): Promise<PacienteEnumsResponse> {
    if (!this.enumsCache) {
      this.enumsCache = this.http.get('/pacientes/enums').then(({ data }) => data)
    }
    return this.enumsCache
  }

  async relatorio(filtro?: PacienteRelatorioFiltro): Promise<PacienteRelatorioResponse> {
    const { data } = await this.http.get('/pacientes/relatorio', { params: filtro })
    return data
  }

  async relatorioExcel(filtro?: PacienteRelatorioFiltro): Promise<Blob> {
    const { data } = await this.http.get('/pacientes/relatorio/excel', { params: filtro, responseType: 'blob' })
    return data
  }
}

export default new PacienteService(http)
