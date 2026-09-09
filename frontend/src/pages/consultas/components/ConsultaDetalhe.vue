<template>
  <div class="consulta-detalhe">
    <div class="consulta-detalhe__header">
      <div class="consulta-detalhe__header-info">
        <v-btn icon="mdi-arrow-left" variant="text" :aria-label="$t('acao.voltar')" @click="$emit('voltar')" />
        <div class="consulta-detalhe__header-titulo-linha">
          <h1 class="consulta-detalhe__titulo">{{ $t('consulta.detalhes') }}</h1>
          <NuvexaStatusChip :label="$t(`consulta.status.${consulta.status}`) as string" :color="statusColor" />
        </div>
      </div>
      <v-btn variant="outlined" @click="$emit('abrir-prontuario')">{{ $t('consulta.verProntuario') }}</v-btn>
    </div>

    <NuvexaPacienteContexto
      :nome="consulta.pacienteNome"
      :subtitulo="contextoSubtitulo"
      :vitais="contextoVitais"
      mostrar-link-prontuario
      @abrir-prontuario="$emit('abrir-prontuario')"
    />

    <v-card variant="flat" color="surface-variant" class="consulta-detalhe__campos">
      <v-card-text class="consulta-detalhe__campos-grid">
        <div class="consulta-detalhe__campo">
          <label>{{ $t('consulta.profissional') }}</label>
          <span>{{ consulta.profissionalNome }}</span>
        </div>
        <div class="consulta-detalhe__campo">
          <label>{{ $t('consulta.dataHora') }}</label>
          <span>{{ formatarDataHora(consulta.dataHora) }}</span>
        </div>
        <div class="consulta-detalhe__campo">
          <label>{{ $t('consulta.duracao') }}</label>
          <span>{{ consulta.duracaoMinutos }}</span>
        </div>
        <div class="consulta-detalhe__campo">
          <label>{{ $t('dashboardConsultas.filtros.tipo') }}</label>
          <span>{{ $t(`consulta.tipo.${consulta.tipo}`) }}</span>
        </div>
        <div class="consulta-detalhe__campo">
          <label>{{ $t('dashboardConsultas.filtros.status') }}</label>
          <span>{{ $t(`consulta.status.${consulta.status}`) }}</span>
        </div>
        <div class="consulta-detalhe__campo consulta-detalhe__campo--largo">
          <label>{{ $t('consulta.motivo') }}</label>
          <span>{{ consulta.motivo || '—' }}</span>
        </div>
      </v-card-text>
    </v-card>

    <v-card variant="flat" color="surface-variant" class="consulta-detalhe__fluxo">
      <v-card-text class="consulta-detalhe__fluxo-conteudo">
        <div class="consulta-detalhe__secao-header">
          <span class="consulta-detalhe__secao-titulo">{{ $t('consulta.fluxo.titulo') }}</span>
          <span class="consulta-detalhe__secao-hint">{{ $t('consulta.fluxo.hint') }}</span>
        </div>

        <div v-if="foraDoFluxo" class="consulta-detalhe__fora-fluxo">
          <span>{{ $t('consulta.fluxo.dicaOutro') }}</span>
        </div>

        <template v-else>
          <div class="consulta-detalhe__etapas">
            <div v-for="(etapa, indice) in etapas" :key="etapa.label" class="consulta-detalhe__etapa-wrap">
              <div class="consulta-detalhe__etapa">
                <div
                  class="consulta-detalhe__etapa-dot"
                  :class="{
                    'consulta-detalhe__etapa-dot--feito': etapa.feito,
                    'consulta-detalhe__etapa-dot--atual': etapa.atual,
                  }"
                >
                  <v-icon v-if="etapa.feito" icon="mdi-check" size="14" />
                  <span v-else>{{ indice + 1 }}</span>
                </div>
                <span class="consulta-detalhe__etapa-label" :class="{ 'consulta-detalhe__etapa-label--atual': etapa.atual }">
                  {{ etapa.label }}
                </span>
                <span class="consulta-detalhe__etapa-quando">{{ etapa.quando }}</span>
              </div>
              <div v-if="indice < etapas.length - 1" class="consulta-detalhe__etapa-linha" :class="{ 'consulta-detalhe__etapa-linha--feito': etapa.feito }" />
            </div>
          </div>

          <div class="consulta-detalhe__fluxo-rodape">
            <span class="consulta-detalhe__dica" :class="{ 'consulta-detalhe__dica--atencao': consulta.status === 'REALIZADA' }">
              {{ dicaEstado }}
            </span>
            <div class="consulta-detalhe__fluxo-acoes">
              <template v-if="consulta.status === 'AGENDADA'">
                <v-btn variant="outlined" color="error" :loading="atualizandoStatus" @click="$emit('mudar-status', 'FALTOU')">
                  {{ $t('consulta.fluxo.marcarFalta') }}
                </v-btn>
                <v-btn variant="outlined" color="warning" @click="$emit('editar')">{{ $t('consulta.fluxo.reagendar') }}</v-btn>
                <v-btn color="primary" variant="flat" :loading="atualizandoStatus" @click="$emit('mudar-status', 'CONFIRMADA')">
                  {{ $t('consulta.fluxo.confirmarPresenca') }}
                </v-btn>
              </template>
              <template v-else-if="consulta.status === 'CONFIRMADA'">
                <v-btn variant="outlined" color="error" :loading="atualizandoStatus" @click="$emit('mudar-status', 'FALTOU')">
                  {{ $t('consulta.fluxo.marcarFalta') }}
                </v-btn>
                <v-btn variant="outlined" color="warning" @click="$emit('editar')">{{ $t('consulta.fluxo.reagendar') }}</v-btn>
                <v-btn color="primary" variant="flat" :loading="atualizandoStatus" @click="$emit('mudar-status', 'REALIZADA')">
                  {{ $t('consulta.fluxo.iniciarAtendimento') }}
                </v-btn>
              </template>
              <template v-else-if="consulta.status === 'REALIZADA'">
                <v-btn variant="outlined" color="info" @click="$emit('agendar-retorno')">{{ $t('consulta.fluxo.agendarRetorno') }}</v-btn>
                <v-tooltip :text="$t('consulta.fluxo.fecharIndisponivel') as string" location="top">
                  <template #activator="{ props: tooltipProps }">
                    <span v-bind="tooltipProps">
                      <v-btn color="primary" variant="flat" disabled>{{ $t('consulta.fluxo.fecharAtendimento') }}</v-btn>
                    </span>
                  </template>
                </v-tooltip>
              </template>
            </div>
          </div>
        </template>
      </v-card-text>
    </v-card>

    <v-card variant="flat" color="surface-variant" class="consulta-detalhe__rodape">
      <v-card-text class="consulta-detalhe__rodape-conteudo">
        <span class="consulta-detalhe__auditoria">{{ auditoria }}</span>
        <v-btn variant="outlined" color="error" :loading="excluindo" @click="confirmarExclusao">
          {{ $t('consulta.excluir') }}
        </v-btn>
      </v-card-text>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import NuvexaStatusChip from '../../../components/common/NuvexaStatusChip.vue'
import NuvexaPacienteContexto from '../../../components/common/NuvexaPacienteContexto.vue'
import type { NuvexaVital } from '../../../components/common/NuvexaPacienteContexto.vue'
import { consultaStatusColor } from '../../../util/consulta-status'
import type { Consulta, ConsultaStatus } from '../../../types/consulta'
import type { PerfilNutricionalResponse } from '../../../nutricao/types/perfil-nutricional'

interface Etapa {
  label: string
  quando: string
  feito: boolean
  atual: boolean
}

const FORA_DO_FLUXO: ConsultaStatus[] = ['CANCELADA', 'FALTOU']

@Component({
  name: 'ConsultaDetalhe',
  components: { NuvexaStatusChip, NuvexaPacienteContexto },
  emits: ['voltar', 'editar', 'abrir-prontuario', 'mudar-status', 'excluir', 'agendar-retorno'],
})
export default class ConsultaDetalhe extends Vue {
  @Prop({ required: true })
  consulta!: Consulta

  @Prop({ default: null })
  perfil!: PerfilNutricionalResponse | null

  @Prop({ default: null })
  ultimaConsultaData!: string | null

  @Prop({ default: () => ({}) })
  rotulosEnum!: Record<string, string>

  @Prop({ default: false })
  excluindo!: boolean

  @Prop({ default: false })
  atualizandoStatus!: boolean

  get statusColor(): string {
    return consultaStatusColor(this.consulta.status)
  }

  get foraDoFluxo(): boolean {
    return FORA_DO_FLUXO.includes(this.consulta.status)
  }

  get contextoSubtitulo(): string {
    if (!this.perfil) {
      return ''
    }
    const sexo = this.rotulosEnum[this.perfil.sexo] ?? this.perfil.sexo
    const objetivo = this.rotulosEnum[this.perfil.objetivo] ?? this.perfil.objetivo
    return `${this.perfil.idade} ${this.$t('paciente.detalhe.anos')} · ${sexo} · ${objetivo}`
  }

  get contextoVitais(): NuvexaVital[] {
    const vitais: NuvexaVital[] = []
    if (this.perfil) {
      vitais.push(
        { label: this.$t('prontuario.pesoAtual') as string, valor: this.perfil.peso !== null ? `${this.perfil.peso.toFixed(1).replace('.', ',')} kg` : '—' },
        { label: this.$t('paciente.imc') as string, valor: this.perfil.imc !== null ? this.perfil.imc.toFixed(1) : '—' },
      )
    }
    vitais.push({ label: this.$t('consulta.ultimaConsulta') as string, valor: this.ultimaConsultaData ? this.formatarData(this.ultimaConsultaData) : '—' })
    return vitais
  }

  get passoAtual(): number {
    const passos: Record<string, number> = { AGENDADA: 1, CONFIRMADA: 2, REALIZADA: 3 }
    return passos[this.consulta.status] ?? 0
  }

  get etapas(): Etapa[] {
    const passo = this.passoAtual
    const definicoes = [
      { label: this.$t('consulta.status.AGENDADA') as string, quando: this.formatarDataHora(this.consulta.criadoEm) },
      { label: this.$t('consulta.status.CONFIRMADA') as string, quando: this.$t('consulta.fluxo.quandoConfirmada') as string },
      { label: this.$t('consulta.status.REALIZADA') as string, quando: this.$t('consulta.fluxo.quandoRealizada') as string },
      { label: this.$t('consulta.fluxo.etapaFechada') as string, quando: this.$t('consulta.fluxo.quandoFechada') as string },
    ]
    return definicoes.map((def, indice) => {
      const numero = indice + 1
      return { label: def.label, quando: def.quando, feito: numero < passo, atual: numero === passo }
    })
  }

  get dicaEstado(): string {
    if (this.consulta.status === 'AGENDADA') return this.$t('consulta.fluxo.dicaAgendada') as string
    if (this.consulta.status === 'CONFIRMADA') return this.$t('consulta.fluxo.dicaConfirmada') as string
    if (this.consulta.status === 'REALIZADA') return this.$t('consulta.fluxo.dicaRealizada') as string
    return ''
  }

  get auditoria(): string {
    const c = this.consulta
    const criado = this.formatarDataHora(c.criadoEm)
    if (c.atualizadoEm === c.criadoEm) {
      return this.$t('consulta.semAlteracao', { criado }) as string
    }
    return this.$t('consulta.auditoriaSimples', { criado, atualizado: this.formatarDataHora(c.atualizadoEm) }) as string
  }

  formatarData(iso: string): string {
    return new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })
  }

  formatarDataHora(iso: string): string {
    const data = new Date(iso)
    const dataLabel = data.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })
    const horaLabel = data.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
    return `${dataLabel}, ${horaLabel}`
  }

  confirmarExclusao() {
    if (confirm(this.$t('consulta.confirmarExclusao') as string)) {
      this.$emit('excluir')
    }
  }
}
</script>

<style scoped lang="scss">
.consulta-detalhe {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.consulta-detalhe__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
}

.consulta-detalhe__header-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.consulta-detalhe__header-titulo-linha {
  display: flex;
  align-items: center;
  gap: 11px;
  flex-wrap: wrap;
}

.consulta-detalhe__titulo {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
}

.consulta-detalhe__campos {
  border-radius: 12px;
}

.consulta-detalhe__campos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px 24px;
}

.consulta-detalhe__campo {
  display: flex;
  flex-direction: column;
  gap: 4px;

  label {
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.04em;
    color: rgb(var(--v-theme-on-surface-variant));
  }

  span {
    font-size: 14px;
  }
}

.consulta-detalhe__campo--largo {
  grid-column: 1 / -1;
}

.consulta-detalhe__fluxo {
  border-radius: 12px;
}

.consulta-detalhe__fluxo-conteudo {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.consulta-detalhe__secao-header {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.consulta-detalhe__secao-titulo {
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: 0.09em;
  text-transform: uppercase;
  color: rgb(var(--v-theme-primary));
  white-space: nowrap;
}

.consulta-detalhe__secao-hint {
  font-size: 11.5px;
  color: rgb(var(--v-theme-on-surface-variant));
}

.consulta-detalhe__fora-fluxo {
  font-size: 13px;
  color: rgb(var(--v-theme-on-surface-variant));
}

.consulta-detalhe__etapas {
  display: flex;
  align-items: flex-start;
  flex-wrap: wrap;
}

.consulta-detalhe__etapa-wrap {
  display: flex;
  align-items: flex-start;
  flex: 1 1 120px;
  min-width: 0;
}

.consulta-detalhe__etapa {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;
  flex: 1;
  min-width: 0;
}

.consulta-detalhe__etapa-dot {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 1.5px solid rgb(var(--v-theme-outline));
  color: rgb(var(--v-theme-on-surface-variant));
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 11.5px;
  font-weight: 700;
}

.consulta-detalhe__etapa-dot--feito,
.consulta-detalhe__etapa-dot--atual {
  border-color: rgb(var(--v-theme-primary));
  color: rgb(var(--v-theme-primary));
}

.consulta-detalhe__etapa-dot--feito {
  background: rgba(var(--v-theme-primary), 0.14);
}

.consulta-detalhe__etapa-dot--atual {
  background: rgba(var(--v-theme-primary), 0.1);
}

.consulta-detalhe__etapa-label {
  font-size: 12.5px;
  font-weight: 500;
  color: rgb(var(--v-theme-on-surface-variant));
}

.consulta-detalhe__etapa-label--atual {
  font-weight: 600;
  color: rgb(var(--v-theme-on-surface));
}

.consulta-detalhe__etapa-quando {
  font-size: 11px;
  color: rgb(var(--v-theme-on-surface-variant));
}

.consulta-detalhe__etapa-linha {
  height: 1.5px;
  background: rgb(var(--v-theme-outline));
  flex: 0 0 24px;
  margin-top: 13px;
}

.consulta-detalhe__etapa-linha--feito {
  background: rgb(var(--v-theme-primary));
}

.consulta-detalhe__fluxo-rodape {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
  border-top: 1px solid rgba(var(--v-theme-on-surface), 0.12);
  padding-top: 18px;
}

.consulta-detalhe__dica {
  font-size: 12.5px;
  color: rgb(var(--v-theme-on-surface-variant));
  line-height: 1.55;
  max-width: 460px;
}

.consulta-detalhe__dica--atencao {
  color: rgb(var(--v-theme-warning));
}

.consulta-detalhe__fluxo-acoes {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.consulta-detalhe__rodape {
  border-radius: 12px;
}

.consulta-detalhe__rodape-conteudo {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.consulta-detalhe__auditoria {
  font-size: 12px;
  color: rgb(var(--v-theme-on-surface-variant));
  line-height: 1.6;
  max-width: 520px;
}
</style>
