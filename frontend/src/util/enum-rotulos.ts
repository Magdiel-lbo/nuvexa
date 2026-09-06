export interface EnumOpcao {
  valor: string
  rotulo: string
}

// Os rótulos dos enums vêm do backend (fonte única da verdade).
export function criarCacheDeRotulos<T extends Record<string, EnumOpcao[]>>(carregarEnums: () => Promise<T>) {
  let cache: Promise<Record<string, string>> | null = null

  return function carregarRotulos(): Promise<Record<string, string>> {
    if (!cache) {
      cache = carregarEnums().then((enums) => Object.fromEntries(Object.values(enums).flat().map((opcao) => [opcao.valor, opcao.rotulo])))
    }
    return cache
  }
}
