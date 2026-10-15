const ORGANIZACAO_KEY = 'nuvexa.organizacaoId'

/** Organização escolhida pelo usuário (quando tem mais de uma). O backend revalida a cada requisição. */
export const organizacaoStorage = {
  get(): number | null {
    const valor = localStorage.getItem(ORGANIZACAO_KEY)
    return valor ? Number(valor) : null
  },
  set(id: number): void {
    localStorage.setItem(ORGANIZACAO_KEY, String(id))
  },
  clear(): void {
    localStorage.removeItem(ORGANIZACAO_KEY)
  },
}
