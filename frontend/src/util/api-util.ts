import { AxiosError } from 'axios'
import type { ApiErro } from '../types/api'

export function extrairMensagemErro(erro: unknown, mensagemPadrao: string): string {
  if (erro instanceof AxiosError && erro.response?.data) {
    const apiErro = erro.response.data as ApiErro
    return apiErro.mensagem ?? mensagemPadrao
  }
  return mensagemPadrao
}
