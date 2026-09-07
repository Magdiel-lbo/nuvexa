import type { AxiosInstance } from 'axios'
import http from '../../core/http/client'
import type { PacienteRelatorioFiltro, PacienteRelatorioResponse } from '../types/paciente-relatorio'

class PacienteRelatorioService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
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

export default new PacienteRelatorioService(http)
