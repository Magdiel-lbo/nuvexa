<template>
  <div class="home">
    <div class="home__header">
      <h1 class="home__title">{{ saudacao }}</h1>

      <div class="home__actions">
        <v-btn color="primary" prepend-icon="mdi-plus" size="large" @click="novaConsulta">
          {{ $t('consulta.novo') }}
        </v-btn>
        <v-btn variant="outlined" color="primary" prepend-icon="mdi-plus" size="large" @click="novoPaciente">
          {{ $t('paciente.novo') }}
        </v-btn>
      </div>
    </div>

    <NuvexaSummaryCards class="home__section" :cards="cards" />

    <DashboardTodayAgenda class="home__section" :consultas="agendaHoje" :loading="consultasCarregando" />

    <h2 class="home__section-title">{{ $t('home.analytics.tituloSecao') }}</h2>

    <DashboardConsultaAnalytics class="home__section" :consultas="consultas" :loading="consultasCarregando" />

    <div class="home__analytics-grid home__section">
      <DashboardPatientGrowth :pacientes="pacientes" :loading="pacientesCarregando" />
      <DashboardInsights :consultas="consultas" :pacientes="pacientes" :loading="consultasCarregando || pacientesCarregando" />
    </div>

    <h2 class="home__section-title">{{ $t('home.carteira.tituloSecao') }}</h2>

    <DashboardPatientIndicators class="home__section" :linhas="relatorioLinhas" :loading="indicadoresCarregando" />
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import NuvexaSummaryCards from '../../components/common/NuvexaSummaryCards.vue'
import type { SummaryCardItem } from '../../components/common/NuvexaSummaryCards.vue'
import DashboardTodayAgenda from './components/DashboardTodayAgenda.vue'
import DashboardPatientIndicators from './components/DashboardPatientIndicators.vue'
import DashboardConsultaAnalytics from './components/DashboardConsultaAnalytics.vue'
import DashboardPatientGrowth from './components/DashboardPatientGrowth.vue'
import DashboardInsights from './components/DashboardInsights.vue'
import consultaService from '../../service/consulta-service'
import pacienteService from '../../nutricao/services/paciente-service'
import { useContextoStore } from '../../core/contexto/contexto.store'
import { useAppStore } from '../../store/app.store'
import { extrairMensagemErro } from '../../util/api-util'
import type { Consulta } from '../../types/consulta'
import type { PacienteResponse, PacienteRelatorioLinha } from '../../nutricao/types/paciente'

const DIAS_NOVOS_PACIENTES = 30

function isSameDay(isoDate: string, reference: Date): boolean {
  const date = new Date(isoDate)
  return (
    date.getFullYear() === reference.getFullYear() &&
    date.getMonth() === reference.getMonth() &&
    date.getDate() === reference.getDate()
  )
}

function saudacaoPorHorario(): string {
  const hora = new Date().getHours()
  if (hora < 12) return 'Bom dia'
  if (hora < 18) return 'Boa tarde'
  return 'Boa noite'
}

@Component({
  name: 'Dashboard',
  components: {
    NuvexaSummaryCards,
    DashboardTodayAgenda,
    DashboardPatientIndicators,
    DashboardConsultaAnalytics,
    DashboardPatientGrowth,
    DashboardInsights,
  },
})
export default class Dashboard extends Vue {
  consultas: Consulta[] = []
  pacientes: PacienteResponse[] = []
  relatorioLinhas: PacienteRelatorioLinha[] = []

  consultasCarregando = false
  pacientesCarregando = false
  indicadoresCarregando = false

  get appStore() {
    return useAppStore()
  }

  get contextoStore() {
    return useContextoStore()
  }

  get saudacao(): string {
    const saudacao = saudacaoPorHorario()
    const nome = this.contextoStore.contexto?.nome
    return nome ? `${saudacao}, ${nome}` : saudacao
  }

  get agendaHoje(): Consulta[] {
    const hoje = new Date()
    return this.consultas
      .filter((consulta) => isSameDay(consulta.dataHora, hoje))
      .sort((a, b) => a.dataHora.localeCompare(b.dataHora))
  }

  get aguardandoConfirmacao(): number {
    return this.consultas.filter((consulta) => consulta.status === 'AGENDADA').length
  }

  get novosPacientes(): number {
    const limite = new Date()
    limite.setDate(limite.getDate() - DIAS_NOVOS_PACIENTES)
    return this.pacientes.filter((paciente) => new Date(paciente.criadoEm) >= limite).length
  }

  get cards(): SummaryCardItem[] {
    return [
      {
        label: this.$t('home.resumo.consultasHoje') as string,
        value: this.consultasCarregando ? '—' : this.agendaHoje.length,
        icon: 'mdi-calendar-today',
        color: 'primary',
      },
      {
        label: this.$t('home.resumo.aguardandoConfirmacao') as string,
        value: this.consultasCarregando ? '—' : this.aguardandoConfirmacao,
        icon: 'mdi-clock-alert-outline',
        color: 'warning',
      },
      {
        label: this.$t('home.resumo.totalPacientes') as string,
        value: this.pacientesCarregando ? '—' : this.pacientes.length,
        icon: 'mdi-account-group-outline',
        color: 'secondary',
      },
      {
        label: this.$t('home.resumo.novosPacientes') as string,
        value: this.pacientesCarregando ? '—' : this.novosPacientes,
        icon: 'mdi-account-plus-outline',
        color: 'success',
      },
    ]
  }

  mounted() {
    this.carregarConsultas()
    this.carregarPacientes()
    this.carregarIndicadores()
  }

  async carregarConsultas() {
    this.consultasCarregando = true
    try {
      this.consultas = await consultaService.listar()
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarConsultas') as string), erro: true })
    } finally {
      this.consultasCarregando = false
    }
  }

  async carregarPacientes() {
    this.pacientesCarregando = true
    try {
      this.pacientes = await pacienteService.listar()
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarPacientes') as string), erro: true })
    } finally {
      this.pacientesCarregando = false
    }
  }

  async carregarIndicadores() {
    this.indicadoresCarregando = true
    try {
      const relatorio = await pacienteService.relatorio()
      this.relatorioLinhas = relatorio.linhas
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarIndicadores') as string), erro: true })
    } finally {
      this.indicadoresCarregando = false
    }
  }

  novaConsulta() {
    this.$router.push('/consultas/novo')
  }

  novoPaciente() {
    this.$router.push('/pacientes/novo')
  }
}
</script>

<style scoped lang="scss">
.home__header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
}

.home__title {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0;
}

.home__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.home__section {
  margin-bottom: 20px;
}

.home__section-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: rgb(var(--v-theme-on-surface-variant));
  margin: 4px 0 12px;
}

.home__analytics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;

  @media (max-width: 960px) {
    grid-template-columns: 1fr;
  }
}
</style>
