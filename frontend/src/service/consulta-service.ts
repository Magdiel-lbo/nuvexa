import type { AxiosInstance } from 'axios'
import http from '../core/http/client'
import type { Consulta, ConsultaCreateRequest, ConsultaUpdateRequest, Profissional } from '../types/consulta'

class ConsultaService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async listar(busca?: string): Promise<Consulta[]> {
    const { data } = await this.http.get('/consultas', { params: busca ? { busca } : {} })
    return data
  }

  async buscarPorId(id: number): Promise<Consulta | undefined> {
    try {
      const { data } = await this.http.get(`/consultas/${id}`)
      return data
    } catch {
      return undefined
    }
  }

  async buscarPorPaciente(pacienteId: number): Promise<Consulta[]> {
    const { data } = await this.http.get('/consultas', { params: { pacienteId } })
    return data
  }

  async listarProfissionais(): Promise<Profissional[]> {
    const { data } = await this.http.get('/consultas/profissionais')
    return data
  }

  async criar(dados: ConsultaCreateRequest): Promise<Consulta> {
    const { data } = await this.http.post('/consultas', dados)
    return data
  }

  async atualizar(id: number, dados: ConsultaUpdateRequest): Promise<Consulta> {
    const { data } = await this.http.put(`/consultas/${id}`, dados)
    return data
  }

  // Recebe a consulta inteira porque o PUT do backend substitui o registro por completo — não
  // existe PATCH parcial. Preserva todos os outros campos, só troca o status.
  async cancelar(consulta: Consulta): Promise<Consulta> {
    return this.atualizar(consulta.id, {
      profissionalId: consulta.profissionalId,
      dataHora: consulta.dataHora,
      duracaoMinutos: consulta.duracaoMinutos,
      tipo: consulta.tipo,
      status: 'CANCELADA',
      observacoes: consulta.observacoes,
    })
  }

  async excluir(id: number): Promise<void> {
    await this.http.delete(`/consultas/${id}`)
  }
}

export default new ConsultaService(http)
