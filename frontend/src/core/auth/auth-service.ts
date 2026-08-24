import type { AxiosInstance } from 'axios'
import http from '../http/client'
import type {
  AutenticacaoResponse,
  CadastroRequest,
  EsqueciSenhaRequest,
  LoginRequest,
  MensagemResponse,
  RedefinirSenhaRequest,
} from './auth'

class AuthService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async login(dto: LoginRequest): Promise<AutenticacaoResponse> {
    const { data } = await this.http.post('/auth/login', dto)
    return data
  }

  async cadastrar(dto: CadastroRequest): Promise<AutenticacaoResponse> {
    const { data } = await this.http.post('/auth/cadastrar', dto)
    return data
  }

  async esqueciSenha(dto: EsqueciSenhaRequest): Promise<MensagemResponse> {
    const { data } = await this.http.post('/auth/esqueci-senha', dto)
    return data
  }

  async redefinirSenha(dto: RedefinirSenhaRequest): Promise<MensagemResponse> {
    const { data } = await this.http.post('/auth/redefinir-senha', dto)
    return data
  }
}

export default new AuthService(http)
