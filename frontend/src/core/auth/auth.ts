export interface LoginRequest {
  email: string
  senha: string
}

export interface CadastroRequest {
  nome: string
  email: string
  senha: string
}

export interface AutenticacaoResponse {
  token: string
  tipoToken: string
  perfil: string
}

export interface EsqueciSenhaRequest {
  email: string
}

export interface RedefinirSenhaRequest {
  token: string
  novaSenha: string
}

export interface MensagemResponse {
  mensagem: string
}
