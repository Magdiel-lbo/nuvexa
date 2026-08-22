import type { AxiosInstance } from 'axios'
import http from '../http/api-patients'
import type {
  AuthResponse,
  ForgotPasswordRequest,
  LoginRequest,
  MessageResponse,
  RegisterRequest,
  ResetPasswordRequest,
} from '../types/auth'

class AuthService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async login(dto: LoginRequest): Promise<AuthResponse> {
    const { data } = await this.http.post('/auth/login', dto)
    return data
  }

  async registrar(dto: RegisterRequest): Promise<AuthResponse> {
    const { data } = await this.http.post('/auth/register', dto)
    return data
  }

  async esqueciSenha(dto: ForgotPasswordRequest): Promise<MessageResponse> {
    const { data } = await this.http.post('/auth/forgot-password', dto)
    return data
  }

  async redefinirSenha(dto: ResetPasswordRequest): Promise<MessageResponse> {
    const { data } = await this.http.post('/auth/reset-password', dto)
    return data
  }
}

export default new AuthService(http)
