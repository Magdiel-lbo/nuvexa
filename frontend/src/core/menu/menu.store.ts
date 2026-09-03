import { defineStore } from 'pinia'
import menuService from './menu-service'
import type { MenuItemResponse } from './menu'

interface MenuState {
  itens: MenuItemResponse[]
  carregando: boolean
}

/**
 * Estrutura de navegação vinda do backend (menu.yml) — o DefaultLayout só renderiza isto,
 * sem lista fixa própria.
 */
export const useMenuStore = defineStore('menu', {
  state: (): MenuState => ({
    itens: [],
    carregando: false,
  }),
  actions: {
    async carregar() {
      if (this.carregando || this.itens.length > 0) return
      this.carregando = true
      try {
        this.itens = await menuService.listar()
      } finally {
        this.carregando = false
      }
    },
    limpar() {
      this.itens = []
    },
  },
})
