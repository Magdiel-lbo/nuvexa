import { defineStore } from 'pinia'

interface Toast {
  show: boolean
  mensagem: string
  erro: boolean
}

export const useAppStore = defineStore('app', {
  state: (): { toast: Toast } => ({
    toast: {
      show: false,
      mensagem: '',
      erro: false,
    },
  }),
  actions: {
    setToast(toast: { mensagem: string; erro: boolean }) {
      this.toast = { show: true, ...toast }
    },
  },
})
