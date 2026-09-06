import prontuarioService from '../service/prontuario-service'
import { criarCacheDeRotulos } from './enum-rotulos'

export const carregarRotulosProntuario = criarCacheDeRotulos(() => prontuarioService.enums())
