import pacienteService from '../../service/paciente-service'
import perfilNutricionalService from '../services/perfil-nutricional-service'

// Os rótulos dos enums vêm do backend (fonte única da verdade), de dois recursos separados
// desde a separação Paciente(core)/PerfilNutricional(nutricao) — Sexo é genérico, Objetivo e
// NivelAtividade são de nutrição. Os valores são únicos entre os três, então um mapa plano
// valor -> rótulo atende todas as telas, igual antes da separação.
let cache: Promise<Record<string, string>> | null = null

export function carregarRotulosEnum(): Promise<Record<string, string>> {
  if (!cache) {
    cache = Promise.all([pacienteService.enums(), perfilNutricionalService.enums()]).then(([pacienteEnums, perfilEnums]) =>
      Object.fromEntries(
        [...pacienteEnums.sexos, ...perfilEnums.objetivos, ...perfilEnums.niveisAtividade].map((opcao) => [opcao.valor, opcao.rotulo]),
      ),
    )
  }
  return cache
}
