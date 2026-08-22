import { AxiosError } from 'axios'
import type { ApiError } from '../types/api'

export function extrairMensagemErro(erro: unknown, mensagemPadrao: string): string {
  if (erro instanceof AxiosError && erro.response?.data) {
    const apiError = erro.response.data as ApiError
    return apiError.message ?? mensagemPadrao
  }
  return mensagemPadrao
}
