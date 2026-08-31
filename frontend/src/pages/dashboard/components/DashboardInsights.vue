<template>
  <v-card variant="flat" color="surface-variant" class="insights">
    <v-card-title>{{ $t('home.analytics.insights.titulo') }}</v-card-title>
    <v-card-text>
      <div v-if="loading" class="insights__loading">
        <v-progress-circular indeterminate color="primary" />
      </div>

      <ul v-else-if="mensagens.length > 0" class="insights__lista">
        <li v-for="(mensagem, index) in mensagens" :key="index" class="insights__item">
          <v-icon icon="mdi-lightbulb-outline" size="18" color="primary" class="insights__icone" />
          <span>{{ mensagem }}</span>
        </li>
      </ul>

      <p v-else class="insights__vazio">{{ $t('home.analytics.insights.semDados') }}</p>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import type { Consulta } from '../../../types/consulta'
import type { PacienteResponse } from '../../../nutricao/types/paciente'

/**
 * Janela fixa de 30 dias para todos os insights, independente do período selecionado
 * nos filtros de "Análise de consultas"/"Crescimento de pacientes" acima — assim o texto
 * não muda só porque o usuário está explorando outro período nos gráficos.
 */
const JANELA_DIAS = 30

/** Só menciona variação de consultas se for >=10%, para não gerar ruído com poucas consultas. */
const LIMIAR_VARIACAO_PERCENTUAL = 10

/** Só menciona cancelamento se houver amostra mínima (5 consultas) e proporção >=15%. */
const LIMIAR_CANCELAMENTO_AMOSTRA_MINIMA = 5
const LIMIAR_CANCELAMENTO_PERCENTUAL = 0.15

/** Só menciona faltas a partir de 3 no período, para não alarmar por 1-2 casos isolados. */
const LIMIAR_FALTAS_MINIMO = 3

@Component({ name: 'DashboardInsights' })
export default class DashboardInsights extends Vue {
  @Prop({ required: true })
  consultas!: Consulta[]

  @Prop({ required: true })
  pacientes!: PacienteResponse[]

  @Prop({ default: false })
  loading!: boolean

  private consultasNoIntervalo(dias: number, deslocamento: number): Consulta[] {
    const fim = new Date()
    fim.setDate(fim.getDate() - deslocamento)
    const inicio = new Date(fim)
    inicio.setDate(inicio.getDate() - dias)
    return this.consultas.filter((consulta) => {
      const data = new Date(consulta.dataHora)
      return data >= inicio && data < fim
    })
  }

  get aguardandoConfirmacao(): number {
    return this.consultas.filter((consulta) => consulta.status === 'AGENDADA').length
  }

  get consultasJanela(): Consulta[] {
    return this.consultasNoIntervalo(JANELA_DIAS, 0)
  }

  get consultasJanelaAnterior(): Consulta[] {
    return this.consultasNoIntervalo(JANELA_DIAS, JANELA_DIAS)
  }

  get novosPacientesJanela(): number {
    const limite = new Date()
    limite.setDate(limite.getDate() - JANELA_DIAS)
    return this.pacientes.filter((paciente) => new Date(paciente.criadoEm) >= limite).length
  }

  get mensagens(): string[] {
    const lista: string[] = []

    if (this.aguardandoConfirmacao > 0) {
      lista.push(
        this.aguardandoConfirmacao === 1
          ? 'Você tem 1 consulta aguardando confirmação.'
          : `Você tem ${this.aguardandoConfirmacao} consultas aguardando confirmação.`,
      )
    }

    const faltas = this.consultasJanela.filter((consulta) => consulta.status === 'FALTOU').length
    if (faltas >= LIMIAR_FALTAS_MINIMO) {
      lista.push(`${faltas} pacientes não compareceram às consultas nos últimos ${JANELA_DIAS} dias.`)
    }

    const canceladas = this.consultasJanela.filter((consulta) => consulta.status === 'CANCELADA').length
    if (
      this.consultasJanela.length >= LIMIAR_CANCELAMENTO_AMOSTRA_MINIMA &&
      canceladas / this.consultasJanela.length >= LIMIAR_CANCELAMENTO_PERCENTUAL
    ) {
      const percentual = Math.round((canceladas / this.consultasJanela.length) * 100)
      lista.push(`${percentual}% das consultas dos últimos ${JANELA_DIAS} dias foram canceladas.`)
    }

    if (this.consultasJanelaAnterior.length > 0) {
      const variacao = Math.round(
        ((this.consultasJanela.length - this.consultasJanelaAnterior.length) / this.consultasJanelaAnterior.length) *
          100,
      )
      if (Math.abs(variacao) >= LIMIAR_VARIACAO_PERCENTUAL) {
        const direcao = variacao > 0 ? 'aumentaram' : 'diminuíram'
        lista.push(`As consultas ${direcao} ${Math.abs(variacao)}% em relação aos ${JANELA_DIAS} dias anteriores.`)
      }
    }

    if (this.novosPacientesJanela > 0) {
      lista.push(
        this.novosPacientesJanela === 1
          ? `1 novo paciente nos últimos ${JANELA_DIAS} dias.`
          : `${this.novosPacientesJanela} novos pacientes nos últimos ${JANELA_DIAS} dias.`,
      )
    }

    return lista
  }
}
</script>

<style scoped lang="scss">
.insights {
  border-radius: 12px;
  height: 100%;
}

.insights__loading {
  display: flex;
  justify-content: center;
  padding: 32px 0;
}

.insights__lista {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.insights__item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 0.9rem;
}

.insights__icone {
  margin-top: 2px;
  flex-shrink: 0;
}

.insights__vazio {
  font-size: 0.9rem;
  color: rgb(var(--v-theme-on-surface-variant));
  margin: 0;
}
</style>
