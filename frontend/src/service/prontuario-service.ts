import type { AxiosInstance } from 'axios'
import http from '../core/http/client'
import type {
  EventoAuditoria,
  Prontuario,
  ProntuarioAdendo,
  ProntuarioAdendoCreateRequest,
  ProntuarioAnexo,
  ProntuarioCreateRequest,
  ProntuarioEnumsResponse,
  ProntuarioRelatorioFiltro,
  ProntuarioUpdateRequest,
} from '../types/prontuario'

class ProntuarioService {
  private http: AxiosInstance
  private enumsCache: Promise<ProntuarioEnumsResponse> | null = null

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async listar(busca?: string): Promise<Prontuario[]> {
    const { data } = await this.http.get('/prontuarios', { params: busca ? { busca } : {} })
    return data
  }

  async buscarPorId(id: number): Promise<Prontuario | undefined> {
    try {
      const { data } = await this.http.get(`/prontuarios/${id}`)
      return data
    } catch {
      return undefined
    }
  }

  async buscarPorPaciente(pacienteId: number): Promise<Prontuario[]> {
    const { data } = await this.http.get('/prontuarios', { params: { pacienteId } })
    return data
  }

  async criar(dados: ProntuarioCreateRequest): Promise<Prontuario> {
    const { data } = await this.http.post('/prontuarios', dados)
    return data
  }

  async atualizar(id: number, dados: ProntuarioUpdateRequest): Promise<Prontuario> {
    const { data } = await this.http.put(`/prontuarios/${id}`, dados)
    return data
  }

  async excluir(id: number): Promise<void> {
    await this.http.delete(`/prontuarios/${id}`)
  }

  async assinar(id: number): Promise<Prontuario> {
    const { data } = await this.http.patch(`/prontuarios/${id}/assinar`)
    return data
  }

  async listarAdendos(prontuarioId: number): Promise<ProntuarioAdendo[]> {
    const { data } = await this.http.get(`/prontuarios/${prontuarioId}/adendos`)
    return data
  }

  async criarAdendo(prontuarioId: number, dados: ProntuarioAdendoCreateRequest): Promise<ProntuarioAdendo> {
    const { data } = await this.http.post(`/prontuarios/${prontuarioId}/adendos`, dados)
    return data
  }

  async listarAuditoria(prontuarioId: number): Promise<EventoAuditoria[]> {
    const { data } = await this.http.get(`/prontuarios/${prontuarioId}/auditoria`)
    return data
  }

  async listarAnexos(prontuarioId: number): Promise<ProntuarioAnexo[]> {
    const { data } = await this.http.get(`/prontuarios/${prontuarioId}/anexos`)
    return data
  }

  async uploadAnexo(prontuarioId: number, arquivo: File): Promise<ProntuarioAnexo> {
    const formData = new FormData()
    formData.append('arquivo', arquivo)
    const { data } = await this.http.post(`/prontuarios/${prontuarioId}/anexos`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return data
  }

  async downloadAnexo(prontuarioId: number, anexoId: number): Promise<Blob> {
    const { data } = await this.http.get(`/prontuarios/${prontuarioId}/anexos/${anexoId}/download`, { responseType: 'blob' })
    return data
  }

  async excluirAnexo(prontuarioId: number, anexoId: number): Promise<void> {
    await this.http.delete(`/prontuarios/${prontuarioId}/anexos/${anexoId}`)
  }

  enums(): Promise<ProntuarioEnumsResponse> {
    if (!this.enumsCache) {
      this.enumsCache = this.http.get('/prontuarios/enums').then(({ data }) => data)
    }
    return this.enumsCache
  }

  async relatorioExcel(filtro?: ProntuarioRelatorioFiltro): Promise<Blob> {
    const { data } = await this.http.get('/prontuarios/relatorio/excel', { params: filtro, responseType: 'blob' })
    return data
  }
}

export default new ProntuarioService(http)
