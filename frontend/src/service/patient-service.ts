import type { AxiosInstance } from 'axios'
import http from '../http/api-patients'
import type {
  PatientCreateRequest,
  PatientEnumsResponse,
  PatientReportFilter,
  PatientReportResponse,
  PatientResponse,
  PatientUpdateRequest,
} from '../types/patient'

class PatientService {
  private http: AxiosInstance
  private enumsCache: Promise<PatientEnumsResponse> | null = null

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async listar(busca?: string): Promise<PatientResponse[]> {
    const { data } = await this.http.get('/patients', { params: busca ? { search: busca } : {} })
    return data
  }

  async buscarPorId(id: number): Promise<PatientResponse> {
    const { data } = await this.http.get(`/patients/${id}`)
    return data
  }

  async salvar(id: number | null, dto: PatientCreateRequest | PatientUpdateRequest): Promise<PatientResponse> {
    if (id) {
      const { data } = await this.http.put(`/patients/${id}`, dto)
      return data
    }
    const { data } = await this.http.post('/patients', dto)
    return data
  }

  async remover(id: number): Promise<void> {
    await this.http.delete(`/patients/${id}`)
  }

  enums(): Promise<PatientEnumsResponse> {
    if (!this.enumsCache) {
      this.enumsCache = this.http.get('/patients/enums').then(({ data }) => data)
    }
    return this.enumsCache
  }

  async relatorio(filtro?: PatientReportFilter): Promise<PatientReportResponse> {
    const { data } = await this.http.get('/patients/report', { params: filtro })
    return data
  }

  async relatorioExcel(filtro?: PatientReportFilter): Promise<Blob> {
    const { data } = await this.http.get('/patients/report/excel', { params: filtro, responseType: 'blob' })
    return data
  }
}

export default new PatientService(http)
