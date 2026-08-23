<template>
  <div class="paciente-detalhe">
    <div class="paciente-detalhe__header">
      <v-btn icon="mdi-arrow-left" variant="text" :aria-label="$t('acao.voltar')" @click="voltar" />

      <template v-if="paciente">
        <v-avatar color="primary" variant="tonal" size="56">
          <span class="paciente-detalhe__initials">{{ initials(paciente.nome) }}</span>
        </v-avatar>
        <div class="paciente-detalhe__title-group">
          <h1 class="paciente-detalhe__title">{{ paciente.nome }}</h1>
          <p class="paciente-detalhe__subtitle">{{ rotulos[paciente.sexo] ?? paciente.sexo }} · {{ paciente.idade }} {{ $t('paciente.detalhe.anos') }}</p>
        </div>
      </template>

      <v-spacer />

      <v-btn v-if="paciente" color="primary" variant="outlined" prepend-icon="mdi-pencil-outline" @click="editar">
        {{ $t('acao.editar') }}
      </v-btn>
    </div>

    <div v-if="loading" class="paciente-detalhe__loading">
      <v-progress-circular indeterminate color="primary" />
    </div>

    <template v-else-if="paciente">
      <div class="paciente-detalhe__grid">
        <v-card variant="flat" color="surface-variant" class="paciente-detalhe__card">
          <v-card-title>{{ $t('paciente.detalhe.dadosFisicos') }}</v-card-title>
          <v-card-text>
            <dl class="paciente-detalhe__list">
              <div class="paciente-detalhe__item">
                <dt>{{ $t('paciente.altura') }}</dt>
                <dd>{{ paciente.altura }} m</dd>
              </div>
              <div class="paciente-detalhe__item">
                <dt>{{ $t('paciente.peso') }}</dt>
                <dd>{{ paciente.peso }} kg</dd>
              </div>
              <div class="paciente-detalhe__item">
                <dt>{{ $t('paciente.imc') }}</dt>
                <dd>{{ paciente.imc.toFixed(1) }} ({{ paciente.classificacaoImc }})</dd>
              </div>
            </dl>
          </v-card-text>
        </v-card>

        <v-card variant="flat" color="surface-variant" class="paciente-detalhe__card">
          <v-card-title>{{ $t('paciente.detalhe.objetivoAtividade') }}</v-card-title>
          <v-card-text>
            <dl class="paciente-detalhe__list">
              <div class="paciente-detalhe__item">
                <dt>{{ $t('paciente.objetivo') }}</dt>
                <dd>{{ rotulos[paciente.objetivo] ?? paciente.objetivo }}</dd>
              </div>
              <div class="paciente-detalhe__item">
                <dt>{{ $t('paciente.nivelAtividade') }}</dt>
                <dd>{{ rotulos[paciente.nivelAtividade] ?? paciente.nivelAtividade }}</dd>
              </div>
              <div class="paciente-detalhe__item">
                <dt>{{ $t('paciente.dataNascimento') }}</dt>
                <dd>{{ formatDate(paciente.dataNascimento) }}</dd>
              </div>
            </dl>
          </v-card-text>
        </v-card>

        <v-card variant="flat" color="surface-variant" class="paciente-detalhe__card">
          <v-card-title>{{ $t('paciente.detalhe.gastoCalorico') }}</v-card-title>
          <v-card-text>
            <dl class="paciente-detalhe__list">
              <div class="paciente-detalhe__item">
                <dt>{{ $t('paciente.detalhe.taxaMetabolicaBasal') }}</dt>
                <dd>{{ Math.round(paciente.taxaMetabolicaBasal) }} kcal</dd>
              </div>
              <div class="paciente-detalhe__item">
                <dt>{{ $t('paciente.detalhe.gastoCaloricoTotal') }}</dt>
                <dd>{{ Math.round(paciente.gastoCaloricoDiario) }} kcal</dd>
              </div>
              <div class="paciente-detalhe__item" v-if="paciente.caloriasDiariasManuais">
                <dt>{{ $t('paciente.gastoCaloricoManual') }}</dt>
                <dd>{{ Math.round(paciente.caloriasDiariasManuais) }} kcal</dd>
              </div>
            </dl>
          </v-card-text>
        </v-card>

        <v-card v-if="paciente.observacoes" variant="flat" color="surface-variant" class="paciente-detalhe__card paciente-detalhe__card--full">
          <v-card-title>{{ $t('paciente.observacoes') }}</v-card-title>
          <v-card-text>{{ paciente.observacoes }}</v-card-text>
        </v-card>
      </div>

      <PacienteConsultaHistorico :paciente-id="pacienteId" />
    </template>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import pacienteService from '../../service/paciente-service'
import type { PacienteResponse } from '../../types/paciente'
import { extrairMensagemErro } from '../../util/api-util'
import { useAppStore } from '../../store/app.store'
import { carregarRotulosEnum } from '../../util/enum-rotulos'
import PacienteConsultaHistorico from './components/PacienteConsultaHistorico.vue'

@Component({ name: 'PacienteDetalhe', components: { PacienteConsultaHistorico } })
export default class PacienteDetalhe extends Vue {
  paciente: PacienteResponse | null = null
  rotulos: Record<string, string> = {}
  loading = false

  get appStore() {
    return useAppStore()
  }

  get pacienteId(): number {
    return Number(this.$route.params.id)
  }

  async mounted() {
    this.rotulos = await carregarRotulosEnum()
    this.loading = true
    try {
      this.paciente = await pacienteService.buscarPorId(this.pacienteId)
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarPaciente') as string), erro: true })
      this.voltar()
    } finally {
      this.loading = false
    }
  }

  initials(name: string): string {
    const parts = name.trim().split(/\s+/)
    const first = parts[0]?.[0] ?? ''
    const last = parts.length > 1 ? parts[parts.length - 1][0] : ''
    return (first + last).toUpperCase()
  }

  formatDate(iso: string): string {
    const [year, month, day] = iso.split('-')
    return `${day}/${month}/${year}`
  }

  editar() {
    this.$router.push(`/pacientes/${this.pacienteId}/editar`)
  }

  voltar() {
    this.$router.push('/pacientes')
  }
}
</script>

<style scoped lang="scss">
.paciente-detalhe__header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.paciente-detalhe__initials {
  font-size: 1rem;
  font-weight: 600;
}

.paciente-detalhe__title-group {
  display: flex;
  flex-direction: column;
}

.paciente-detalhe__title {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.paciente-detalhe__subtitle {
  color: rgb(var(--v-theme-on-surface-variant));
  margin: 0;
}

.paciente-detalhe__loading {
  display: flex;
  justify-content: center;
  padding: 96px 0;
}

.paciente-detalhe__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;

  @media (max-width: 960px) {
    grid-template-columns: 1fr;
  }
}

.paciente-detalhe__card {
  border-radius: 12px;
}

.paciente-detalhe__card--full {
  grid-column: 1 / -1;
}

.paciente-detalhe__list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.paciente-detalhe__item {
  display: flex;
  justify-content: space-between;
  gap: 16px;

  dt {
    color: rgb(var(--v-theme-on-surface-variant));
  }

  dd {
    margin: 0;
    font-weight: 500;
    text-align: right;
  }
}
</style>
