import avaliacaoService from '../services/avaliacao-service'
import { criarCacheDeRotulos } from '../../util/enum-rotulos'

export const carregarRotulosAvaliacao = criarCacheDeRotulos(() => avaliacaoService.enums())
