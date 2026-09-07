import type { AxiosInstance } from 'axios'
import http from '../../core/http/client'
import type {
  PerfilNutricionalCreateRequest,
  PerfilNutricionalEnumsResponse,
  PerfilNutricionalResponse,
  PerfilNutricionalUpdateRequest,
} from '../types/perfil-nutricional'

class PerfilNutricionalService {
  private http: AxiosInstance
  private enumsCache: Promise<PerfilNutricionalEnumsResponse> | null = null

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async listar(busca?: string): Promise<PerfilNutricionalResponse[]> {
    const { data } = await this.http.get('/perfis-nutricionais', { params: busca ? { busca } : {} })
    return data
  }

  async buscarPorPaciente(pacienteId: number): Promise<PerfilNutricionalResponse | undefined> {
    try {
      const { data } = await this.http.get(`/pacientes/${pacienteId}/perfil-nutricional`)
      return data
    } catch {
      return undefined
    }
  }

  async criar(pacienteId: number, dados: PerfilNutricionalCreateRequest): Promise<PerfilNutricionalResponse> {
    const { data } = await this.http.post(`/pacientes/${pacienteId}/perfil-nutricional`, dados)
    return data
  }

  async atualizar(pacienteId: number, dados: PerfilNutricionalUpdateRequest): Promise<PerfilNutricionalResponse> {
    const { data } = await this.http.put(`/pacientes/${pacienteId}/perfil-nutricional`, dados)
    return data
  }

  async excluir(pacienteId: number): Promise<void> {
    await this.http.delete(`/pacientes/${pacienteId}/perfil-nutricional`)
  }

  enums(): Promise<PerfilNutricionalEnumsResponse> {
    if (!this.enumsCache) {
      this.enumsCache = this.http.get('/perfis-nutricionais/enums').then(({ data }) => data)
    }
    return this.enumsCache
  }
}

export default new PerfilNutricionalService(http)
