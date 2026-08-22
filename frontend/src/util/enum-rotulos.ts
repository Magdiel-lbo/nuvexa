import patientService from '../service/patient-service'

// Os rótulos dos enums vêm do backend (fonte única da verdade).
// Os valores são únicos entre Sexo/Objetivo/NivelAtividade, então um mapa
// plano value -> label atende todas as telas.
let cache: Promise<Record<string, string>> | null = null

export function carregarRotulosEnum(): Promise<Record<string, string>> {
  if (!cache) {
    cache = patientService.enums().then((enums) =>
      Object.fromEntries(
        [...enums.genders, ...enums.goals, ...enums.activityLevels].map((opcao) => [opcao.value, opcao.label]),
      ),
    )
  }
  return cache
}
