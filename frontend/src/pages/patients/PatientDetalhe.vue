<template>
  <div class="patient-detalhe">
    <div class="patient-detalhe__header">
      <v-btn icon="mdi-arrow-left" variant="text" :aria-label="$t('acao.voltar')" @click="voltar" />

      <template v-if="paciente">
        <v-avatar color="primary" variant="tonal" size="56">
          <span class="patient-detalhe__initials">{{ initials(paciente.name) }}</span>
        </v-avatar>
        <div class="patient-detalhe__title-group">
          <h1 class="patient-detalhe__title">{{ paciente.name }}</h1>
          <p class="patient-detalhe__subtitle">{{ $t(`sexo.${paciente.gender}`) }} · {{ paciente.age }} {{ $t('paciente.detalhe.anos') }}</p>
        </div>
      </template>

      <v-spacer />

      <v-btn v-if="paciente" color="primary" variant="outlined" prepend-icon="mdi-pencil-outline" @click="editar">
        {{ $t('acao.editar') }}
      </v-btn>
    </div>

    <div v-if="loading" class="patient-detalhe__loading">
      <v-progress-circular indeterminate color="primary" />
    </div>

    <template v-else-if="paciente">
      <div class="patient-detalhe__grid">
        <v-card variant="flat" color="surface-variant" class="patient-detalhe__card">
          <v-card-title>{{ $t('paciente.detalhe.dadosFisicos') }}</v-card-title>
          <v-card-text>
            <dl class="patient-detalhe__list">
              <div class="patient-detalhe__item">
                <dt>{{ $t('paciente.altura') }}</dt>
                <dd>{{ paciente.height }} m</dd>
              </div>
              <div class="patient-detalhe__item">
                <dt>{{ $t('paciente.peso') }}</dt>
                <dd>{{ paciente.weight }} kg</dd>
              </div>
              <div class="patient-detalhe__item">
                <dt>{{ $t('paciente.imc') }}</dt>
                <dd>{{ paciente.bmi.toFixed(1) }} ({{ paciente.bmiClassification }})</dd>
              </div>
            </dl>
          </v-card-text>
        </v-card>

        <v-card variant="flat" color="surface-variant" class="patient-detalhe__card">
          <v-card-title>{{ $t('paciente.detalhe.objetivoAtividade') }}</v-card-title>
          <v-card-text>
            <dl class="patient-detalhe__list">
              <div class="patient-detalhe__item">
                <dt>{{ $t('paciente.objetivo') }}</dt>
                <dd>{{ $t(`objetivo.${paciente.goal}`) }}</dd>
              </div>
              <div class="patient-detalhe__item">
                <dt>{{ $t('paciente.nivelAtividade') }}</dt>
                <dd>{{ $t(`nivelAtividade.${paciente.activityLevel}`) }}</dd>
              </div>
              <div class="patient-detalhe__item">
                <dt>{{ $t('paciente.dataNascimento') }}</dt>
                <dd>{{ formatDate(paciente.birthDate) }}</dd>
              </div>
            </dl>
          </v-card-text>
        </v-card>

        <v-card variant="flat" color="surface-variant" class="patient-detalhe__card">
          <v-card-title>{{ $t('paciente.detalhe.gastoCalorico') }}</v-card-title>
          <v-card-text>
            <dl class="patient-detalhe__list">
              <div class="patient-detalhe__item">
                <dt>{{ $t('paciente.detalhe.taxaMetabolicaBasal') }}</dt>
                <dd>{{ Math.round(paciente.bmr) }} kcal</dd>
              </div>
              <div class="patient-detalhe__item">
                <dt>{{ $t('paciente.detalhe.gastoCaloricoTotal') }}</dt>
                <dd>{{ Math.round(paciente.dailyCalorieExpenditure) }} kcal</dd>
              </div>
              <div class="patient-detalhe__item" v-if="paciente.manualDailyCalories">
                <dt>{{ $t('paciente.gastoCaloricoManual') }}</dt>
                <dd>{{ Math.round(paciente.manualDailyCalories) }} kcal</dd>
              </div>
            </dl>
          </v-card-text>
        </v-card>

        <v-card v-if="paciente.notes" variant="flat" color="surface-variant" class="patient-detalhe__card patient-detalhe__card--full">
          <v-card-title>{{ $t('paciente.observacoes') }}</v-card-title>
          <v-card-text>{{ paciente.notes }}</v-card-text>
        </v-card>
      </div>

      <PatientConsultaHistorico :patient-id="patientId" />
    </template>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import patientService from '../../service/patient-service'
import type { PatientResponse } from '../../types/patient'
import { extrairMensagemErro } from '../../util/api-util'
import { useAppStore } from '../../store/app.store'
import PatientConsultaHistorico from './components/PatientConsultaHistorico.vue'

@Component({ name: 'PatientDetalhe', components: { PatientConsultaHistorico } })
export default class PatientDetalhe extends Vue {
  paciente: PatientResponse | null = null
  loading = false

  get appStore() {
    return useAppStore()
  }

  get patientId(): number {
    return Number(this.$route.params.id)
  }

  async mounted() {
    this.loading = true
    try {
      this.paciente = await patientService.buscarPorId(this.patientId)
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
    this.$router.push(`/patients/${this.patientId}/edit`)
  }

  voltar() {
    this.$router.push('/patients')
  }
}
</script>

<style scoped lang="scss">
.patient-detalhe__header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.patient-detalhe__initials {
  font-size: 1rem;
  font-weight: 600;
}

.patient-detalhe__title-group {
  display: flex;
  flex-direction: column;
}

.patient-detalhe__title {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.patient-detalhe__subtitle {
  color: rgb(var(--v-theme-on-surface-variant));
  margin: 0;
}

.patient-detalhe__loading {
  display: flex;
  justify-content: center;
  padding: 96px 0;
}

.patient-detalhe__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;

  @media (max-width: 960px) {
    grid-template-columns: 1fr;
  }
}

.patient-detalhe__card {
  border-radius: 12px;
}

.patient-detalhe__card--full {
  grid-column: 1 / -1;
}

.patient-detalhe__list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.patient-detalhe__item {
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
