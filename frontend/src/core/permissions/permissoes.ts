import { useContextoStore } from '../contexto/contexto.store'

/**
 * Ponto único de leitura de permissões no frontend. Componentes chamam `usePermissoes()`
 * em vez de acessar `contextoStore` diretamente — se uma regra mudar, muda só aqui.
 *
 * A UI só decide o que MOSTRAR com isto; quem decide o que é PERMITIDO continua sendo
 * o backend (@PreAuthorize + escopo organizacional na query).
 */
export function usePermissoes() {
  const contexto = useContextoStore()
  return {
    isAdmin: contexto.isAdmin,
    podeExcluirPaciente: contexto.podeExcluirPaciente,
    podeAdministrarOrganizacao: contexto.podeAdministrarOrganizacao,
  }
}
