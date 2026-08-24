import pacienteService from '../services/paciente-service'

// Os rótulos dos enums vêm do backend (fonte única da verdade).
// Os valores são únicos entre Sexo/Objetivo/NivelAtividade, então um mapa
// plano valor -> rótulo atende todas as telas.
let cache: Promise<Record<string, string>> | null = null

export function carregarRotulosEnum(): Promise<Record<string, string>> {
  if (!cache) {
    cache = pacienteService.enums().then((enums) =>
      Object.fromEntries(
        [...enums.sexos, ...enums.objetivos, ...enums.niveisAtividade].map((opcao) => [opcao.valor, opcao.rotulo]),
      ),
    )
  }
  return cache
}
