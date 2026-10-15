import { defineStore } from 'pinia'
import contextoService from './contexto-service'
import { organizacaoStorage } from './organizacao-storage'
import type { ContextoResponse, OrganizacaoDoUsuario } from './contexto'

interface ContextoState {
  contexto: ContextoResponse | null
  organizacoes: OrganizacaoDoUsuario[]
  carregando: boolean
}

/**
 * Usuário, organização e papel do usuário logado. Serve só para a UI decidir o que exibir —
 * quem realmente autoriza é o backend.
 */
export const useContextoStore = defineStore('contexto', {
  state: (): ContextoState => ({
    contexto: null,
    organizacoes: [],
    carregando: false,
  }),
  getters: {
    organizacaoNome: (state) => state.contexto?.organizacaoNome ?? '',
    papelOrganizacional: (state) => state.contexto?.papelOrganizacional ?? null,
    especialidade: (state) => state.contexto?.especialidade ?? null,
    temNutricao: (state) => state.contexto?.especialidade === 'NUTRICAO',
    isAdmin: (state) => state.contexto?.perfil === 'ADMIN',
    podeExcluirPaciente: (state) => state.contexto?.perfil === 'ADMIN',
    /** Recepção cadastra e agenda, mas não vê o clínico (prontuário, observações da consulta...). */
    podeVerClinico: (state) => !!state.contexto && state.contexto.papelOrganizacional !== 'RECEPCAO',
    podeGerenciarEquipe: (state) =>
      state.contexto?.papelOrganizacional === 'PROPRIETARIO' || state.contexto?.papelOrganizacional === 'GESTOR',
    podeAdministrarOrganizacao: (state) =>
      state.contexto?.papelOrganizacional === 'PROPRIETARIO' ||
      state.contexto?.papelOrganizacional === 'GESTOR',
    podeEditarOrganizacao: (state) => state.contexto?.papelOrganizacional === 'PROPRIETARIO',
    temVariasOrganizacoes: (state) => state.organizacoes.length > 1,
  },
  actions: {
    async carregar() {
      if (this.carregando) return
      this.carregando = true
      try {
        this.organizacoes = await contextoService.organizacoes()
        const escolhida = organizacaoStorage.get()
        if (escolhida !== null && !this.organizacoes.some((o) => o.organizacaoId === escolhida)) {
          organizacaoStorage.clear()
        }
        this.contexto = await contextoService.atual()
      } finally {
        this.carregando = false
      }
    },
    /** Troca a organização: o cabeçalho passa a valer nas próximas requisições, então recarrega-se tudo. */
    async selecionarOrganizacao(organizacaoId: number) {
      organizacaoStorage.set(organizacaoId)
      window.location.assign('/')
    },
    limpar() {
      this.contexto = null
      this.organizacoes = []
      organizacaoStorage.clear()
    },
  },
})
