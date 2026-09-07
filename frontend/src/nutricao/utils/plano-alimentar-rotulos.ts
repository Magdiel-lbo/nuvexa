import planoAlimentarService from '../services/plano-alimentar-service'
import { criarCacheDeRotulos } from '../../util/enum-rotulos'

export const carregarRotulosPlanoAlimentar = criarCacheDeRotulos(() => planoAlimentarService.enums())
