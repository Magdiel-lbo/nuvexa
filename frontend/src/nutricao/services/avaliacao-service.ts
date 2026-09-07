import type { AxiosInstance } from 'axios'
import http from '../../core/http/client'
import type {
  Avaliacao,
  AvaliacaoCreateRequest,
  AvaliacaoEnumsResponse,
  AvaliacaoRelatorioFiltro,
  AvaliacaoUpdateRequest,
} from '../types/avaliacao'

class AvaliacaoService {
  private http: AxiosInstance
  private enumsCache: Promise<AvaliacaoEnumsResponse> | null = null

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async listar(busca?: string): Promise<Avaliacao[]> {
    const { data } = await this.http.get('/avaliacoes', { params: busca ? { busca } : {} })
    return data
  }

  async buscarPorId(id: number): Promise<Avaliacao | undefined> {
    try {
      const { data } = await this.http.get(`/avaliacoes/${id}`)
      return data
    } catch {
      return undefined
    }
  }

  async buscarPorPaciente(pacienteId: number): Promise<Avaliacao[]> {
    const { data } = await this.http.get('/avaliacoes', { params: { pacienteId } })
    return data
  }

  async criar(dados: AvaliacaoCreateRequest): Promise<Avaliacao> {
    const { data } = await this.http.post('/avaliacoes', dados)
    return data
  }

  async atualizar(id: number, dados: AvaliacaoUpdateRequest): Promise<Avaliacao> {
    const { data } = await this.http.put(`/avaliacoes/${id}`, dados)
    return data
  }

  async excluir(id: number): Promise<void> {
    await this.http.delete(`/avaliacoes/${id}`)
  }

  enums(): Promise<AvaliacaoEnumsResponse> {
    if (!this.enumsCache) {
      this.enumsCache = this.http.get('/avaliacoes/enums').then(({ data }) => data)
    }
    return this.enumsCache
  }

  async relatorioExcel(filtro?: AvaliacaoRelatorioFiltro): Promise<Blob> {
    const { data } = await this.http.get('/avaliacoes/relatorio/excel', { params: filtro, responseType: 'blob' })
    return data
  }
}

export default new AvaliacaoService(http)
