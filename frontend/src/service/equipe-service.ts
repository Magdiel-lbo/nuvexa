import type { AxiosInstance } from 'axios'
import axios from 'axios'
import configs from '../configs/env'
import http from '../core/http/client'
import type { AutenticacaoResponse } from '../core/auth/auth'
import type { OrganizacaoDoUsuario, PapelOrganizacional } from '../core/contexto/contexto'
import type { Convite, ConviteAceiteRequest, ConviteCreateRequest, Membro, PacienteProfissional } from '../types/equipe'

class EquipeService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async membros(): Promise<Membro[]> {
    const { data } = await this.http.get('/equipe')
    return data
  }

  async alterarPapel(vinculoId: number, papel: PapelOrganizacional): Promise<Membro> {
    const { data } = await this.http.put(`/equipe/${vinculoId}/papel`, { papel })
    return data
  }

  async removerMembro(vinculoId: number): Promise<void> {
    await this.http.delete(`/equipe/${vinculoId}`)
  }

  async convites(): Promise<Convite[]> {
    const { data } = await this.http.get('/convites')
    return data
  }

  async convidar(dados: ConviteCreateRequest): Promise<Convite> {
    const { data } = await this.http.post('/convites', dados)
    return data
  }

  async revogarConvite(id: number): Promise<void> {
    await this.http.delete(`/convites/${id}`)
  }

  /** Quem ainda não tem conta: o token do e-mail cria a conta. Rota pública, sem interceptores de sessão. */
  async aceitarCriandoConta(dados: ConviteAceiteRequest): Promise<AutenticacaoResponse> {
    const { data } = await axios.post(`${configs.apiBaseUrl}/auth/convites/aceitar`, dados)
    return data
  }

  /** Quem já tem conta e está logado com o e-mail do convite. */
  async aceitarLogado(token: string): Promise<OrganizacaoDoUsuario> {
    const { data } = await this.http.post('/convites/aceitar', { token })
    return data
  }

  async profissionaisDoPaciente(pacienteId: number): Promise<PacienteProfissional[]> {
    const { data } = await this.http.get(`/pacientes/${pacienteId}/profissionais`)
    return data
  }

  async vincularProfissional(pacienteId: number, profissionalId: number): Promise<PacienteProfissional> {
    const { data } = await this.http.post(`/pacientes/${pacienteId}/profissionais`, { profissionalId })
    return data
  }

  async desvincularProfissional(pacienteId: number, profissionalId: number): Promise<void> {
    await this.http.delete(`/pacientes/${pacienteId}/profissionais/${profissionalId}`)
  }
}

export default new EquipeService(http)
