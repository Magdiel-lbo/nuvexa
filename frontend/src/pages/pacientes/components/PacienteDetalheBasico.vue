<template>
  <div class="paciente-basico">
    <div class="paciente-basico__header">
      <div class="paciente-basico__header-info">
        <v-btn icon="mdi-arrow-left" variant="text" :aria-label="$t('acao.voltar')" @click="$emit('voltar')" />
        <div>
          <h1 class="paciente-basico__titulo">{{ paciente.nome }}</h1>
          <span class="paciente-basico__subtitulo">{{ rotulos[paciente.sexo] ?? paciente.sexo }} · {{ paciente.idade }} {{ $t('paciente.detalhe.anos') }}</span>
        </div>
      </div>
      <div class="paciente-basico__acoes">
        <v-btn v-if="podeVerClinico" variant="outlined" prepend-icon="mdi-file-document-outline" @click="$emit('abrir-prontuario')">
          {{ $t('paciente.detalhe.verProntuario') }}
        </v-btn>
        <v-btn color="primary" variant="flat" prepend-icon="mdi-plus" @click="$emit('nova-consulta')">{{ $t('paciente.detalhe.novaConsulta') }}</v-btn>
      </div>
    </div>

    <div class="paciente-basico__grid">
      <v-card variant="flat" color="surface-variant" class="paciente-basico__card">
        <v-card-text class="paciente-basico__card-conteudo">
          <span class="paciente-basico__card-titulo">{{ $t('paciente.basico.proximasConsultas') }}</span>
          <button v-for="consulta in proximasConsultas" :key="consulta.id" type="button" class="paciente-basico__item" @click="$emit('abrir-consulta', consulta.id)">
            <span class="paciente-basico__item-data">{{ formatarDataHora(consulta.dataHora) }}</span>
            <NuvexaStatusChip :label="$t(`consulta.status.${consulta.status}`) as string" color="on-surface-variant" />
          </button>
          <span v-if="proximasConsultas.length === 0" class="paciente-basico__vazio">{{ $t('paciente.basico.semConsultas') }}</span>
        </v-card-text>
      </v-card>

      <v-card v-if="podeVerClinico" variant="flat" color="surface-variant" class="paciente-basico__card">
        <v-card-text class="paciente-basico__card-conteudo">
          <div class="paciente-basico__card-linha">
            <span class="paciente-basico__card-titulo">{{ $t('paciente.basico.registros') }}</span>
            <a href="#" @click.prevent="$emit('novo-registro')">{{ $t('paciente.basico.novoRegistro') }}</a>
          </div>
          <button v-for="registro in registrosRecentes" :key="registro.id" type="button" class="paciente-basico__item" @click="$emit('abrir-registro', registro.id)">
            <span class="paciente-basico__item-data">{{ formatarDataHora(registro.atualizadoEm) }}</span>
            <span class="paciente-basico__item-desc">{{ rotulosProntuario[registro.secao] ?? registro.secao }}</span>
            <NuvexaStatusChip :label="rotulosProntuario[registro.status] ?? registro.status" color="on-surface-variant" />
          </button>
          <span v-if="registrosRecentes.length === 0" class="paciente-basico__vazio">{{ $t('paciente.detalhe.semRegistros') }}</span>
        </v-card-text>
      </v-card>

      <PacienteProfissionais :paciente-id="paciente.id" />
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import NuvexaStatusChip from '../../../components/common/NuvexaStatusChip.vue'
import PacienteProfissionais from './PacienteProfissionais.vue'
import type { PacienteResponse } from '../../../types/paciente'
import type { Consulta } from '../../../types/consulta'
import type { Prontuario } from '../../../types/prontuario'
import { useContextoStore } from '../../../core/contexto/contexto.store'

/** Detalhe do paciente para organizações sem vertical de nutrição (ou sem acesso ao perfil nutricional). */
@Component({ name: 'PacienteDetalheBasico', components: { NuvexaStatusChip, PacienteProfissionais }, emits: ['voltar', 'abrir-prontuario', 'nova-consulta', 'abrir-consulta', 'novo-registro', 'abrir-registro'] })
export default class PacienteDetalheBasico extends Vue {
  @Prop({ required: true })
  paciente!: PacienteResponse

  @Prop({ default: () => [] })
  consultas!: Consulta[]

  @Prop({ default: () => [] })
  prontuarios!: Prontuario[]

  @Prop({ default: () => ({}) })
  rotulos!: Record<string, string>

  @Prop({ default: () => ({}) })
  rotulosProntuario!: Record<string, string>

  get podeVerClinico(): boolean {
    return useContextoStore().podeVerClinico
  }

  get proximasConsultas(): Consulta[] {
    const agora = Date.now()
    return [...this.consultas]
      .filter((consulta) => new Date(consulta.dataHora).getTime() >= agora)
      .sort((a, b) => new Date(a.dataHora).getTime() - new Date(b.dataHora).getTime())
      .slice(0, 5)
  }

  get registrosRecentes(): Prontuario[] {
    return [...this.prontuarios].sort((a, b) => new Date(b.atualizadoEm).getTime() - new Date(a.atualizadoEm).getTime()).slice(0, 5)
  }

  formatarDataHora(iso: string): string {
    return new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
  }
}
</script>

<style scoped lang="scss">
.paciente-basico__header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.paciente-basico__header-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.paciente-basico__titulo {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.paciente-basico__subtitulo {
  color: rgb(var(--v-theme-on-surface-variant));
  font-size: 0.85rem;
}

.paciente-basico__acoes {
  display: flex;
  gap: 10px;
}

.paciente-basico__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 16px;
}

.paciente-basico__card {
  border-radius: 12px;
}

.paciente-basico__card-conteudo {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.paciente-basico__card-linha {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.paciente-basico__card-titulo {
  font-size: 0.9rem;
  font-weight: 600;
}

.paciente-basico__item {
  display: flex;
  align-items: center;
  gap: 10px;
  background: none;
  border: none;
  padding: 6px 0;
  cursor: pointer;
  text-align: left;
  font: inherit;
  color: inherit;
}

.paciente-basico__item-data {
  font-size: 0.8rem;
  min-width: 130px;
}

.paciente-basico__item-desc {
  flex: 1;
  font-size: 0.85rem;
}

.paciente-basico__vazio {
  font-size: 0.8rem;
  color: rgb(var(--v-theme-on-surface-variant));
}
</style>
