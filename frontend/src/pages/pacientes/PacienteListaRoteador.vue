<template>
  <PacienteLista v-if="usaListaNutricional" />
  <PacienteListaBasica v-else />
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import PacienteLista from './PacienteLista.vue'
import PacienteListaBasica from './PacienteListaBasica.vue'
import { useContextoStore } from '../../core/contexto/contexto.store'

/**
 * A lista rica (objetivo, IMC, exportação) depende do perfil nutricional: só aparece para
 * organização de nutrição e para quem pode ver o clínico. Os demais usam a lista do núcleo.
 */
@Component({ name: 'PacienteListaRoteador', components: { PacienteLista, PacienteListaBasica } })
export default class PacienteListaRoteador extends Vue {
  get usaListaNutricional(): boolean {
    const contexto = useContextoStore()
    return contexto.temNutricao && contexto.podeVerClinico
  }
}
</script>
