import { defineStore } from 'pinia'
import contextoService from './contexto-service'
import type { ContextoResponse } from './contexto'

interface ContextoState {
  contexto: ContextoResponse | null
  carregando: boolean
}

/**
 * Usuário, organização e papel do usuário logado. Serve só para a UI decidir o que exibir —
 * quem realmente autoriza é o backend.
 */
export const useContextoStore = defineStore('contexto', {
  state: (): ContextoState => ({
    contexto: null,
    carregando: false,
  }),
  getters: {
    organizacaoNome: (state) => state.contexto?.organizacaoNome ?? '',
    papelOrganizacional: (state) => state.contexto?.papelOrganizacional ?? null,
    isAdmin: (state) => state.contexto?.perfil === 'ADMIN',
    podeExcluirPaciente: (state) => state.contexto?.perfil === 'ADMIN',
    podeAdministrarOrganizacao: (state) =>
      state.contexto?.papelOrganizacional === 'PROPRIETARIO' ||
      state.contexto?.papelOrganizacional === 'GESTOR',
  },
  actions: {
    async carregar() {
      if (this.carregando) return
      this.carregando = true
      try {
        this.contexto = await contextoService.atual()
      } finally {
        this.carregando = false
      }
    },
    limpar() {
      this.contexto = null
    },
  },
})
