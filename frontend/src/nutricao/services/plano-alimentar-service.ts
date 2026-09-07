import type { AxiosInstance } from 'axios'
import http from '../../core/http/client'
import type {
  PlanoAlimentar,
  PlanoAlimentarCreateRequest,
  PlanoAlimentarEnumsResponse,
  PlanoAlimentarRelatorioFiltro,
  PlanoAlimentarUpdateRequest,
} from '../types/plano-alimentar'

class PlanoAlimentarService {
  private http: AxiosInstance
  private enumsCache: Promise<PlanoAlimentarEnumsResponse> | null = null

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async listar(busca?: string): Promise<PlanoAlimentar[]> {
    const { data } = await this.http.get('/planos-alimentares', { params: busca ? { busca } : {} })
    return data
  }

  async buscarPorId(id: number): Promise<PlanoAlimentar | undefined> {
    try {
      const { data } = await this.http.get(`/planos-alimentares/${id}`)
      return data
    } catch {
      return undefined
    }
  }

  async buscarPorPaciente(pacienteId: number): Promise<PlanoAlimentar[]> {
    const { data } = await this.http.get('/planos-alimentares', { params: { pacienteId } })
    return data
  }

  async criar(dados: PlanoAlimentarCreateRequest): Promise<PlanoAlimentar> {
    const { data } = await this.http.post('/planos-alimentares', dados)
    return data
  }

  async atualizar(id: number, dados: PlanoAlimentarUpdateRequest): Promise<PlanoAlimentar> {
    const { data } = await this.http.put(`/planos-alimentares/${id}`, dados)
    return data
  }

  async excluir(id: number): Promise<void> {
    await this.http.delete(`/planos-alimentares/${id}`)
  }

  enums(): Promise<PlanoAlimentarEnumsResponse> {
    if (!this.enumsCache) {
      this.enumsCache = this.http.get('/planos-alimentares/enums').then(({ data }) => data)
    }
    return this.enumsCache
  }

  async relatorioExcel(filtro?: PlanoAlimentarRelatorioFiltro): Promise<Blob> {
    const { data } = await this.http.get('/planos-alimentares/relatorio/excel', { params: filtro, responseType: 'blob' })
    return data
  }
}

export default new PlanoAlimentarService(http)
