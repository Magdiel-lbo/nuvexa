<template>
  <div class="paciente-formulario">
    <div class="paciente-formulario__header">
      <v-btn icon="mdi-arrow-left" variant="text" :aria-label="$t('acao.voltar')" @click="voltar" />
      <div>
        <h1 class="paciente-formulario__title">{{ titulo }}</h1>
        <p v-if="paciente" class="paciente-formulario__subtitle">
          {{ rotulos[paciente.sexo] ?? paciente.sexo }} · {{ paciente.idade }} {{ $t('paciente.detalhe.anos') }}
        </p>
      </div>
    </div>

    <p v-if="loading">...</p>

    <template v-else>
      <v-card variant="flat" color="surface-variant" class="paciente-formulario__card">
        <v-card-text class="pt-4">
          <PacienteForm
            v-model="form"
            :submit-label="(isEdicao ? $t('acao.salvar') : $t('acao.criar')) as string"
            :loading="salvando"
            :readonly="isView"
            @submit="salvar"
            @cancel="voltar"
          />
        </v-card-text>
      </v-card>

      <template v-if="paciente">
        <div class="paciente-formulario__metrics">
          <v-card variant="flat" color="surface-variant" class="paciente-formulario__metric-card">
            <v-card-title>{{ $t('paciente.imc') }}</v-card-title>
            <v-card-text>
              <span class="paciente-formulario__metric-value">{{ paciente.imc.toFixed(1) }}</span>
              <span class="paciente-formulario__metric-hint">{{ paciente.classificacaoImc }}</span>
            </v-card-text>
          </v-card>

          <v-card variant="flat" color="surface-variant" class="paciente-formulario__metric-card">
            <v-card-title>{{ $t('paciente.detalhe.taxaMetabolicaBasal') }}</v-card-title>
            <v-card-text>
              <span class="paciente-formulario__metric-value">{{ Math.round(paciente.taxaMetabolicaBasal) }}</span>
              <span class="paciente-formulario__metric-hint">kcal</span>
            </v-card-text>
          </v-card>

          <v-card variant="flat" color="surface-variant" class="paciente-formulario__metric-card">
            <v-card-title>{{ $t('paciente.detalhe.gastoCaloricoTotal') }}</v-card-title>
            <v-card-text>
              <span class="paciente-formulario__metric-value">{{ Math.round(paciente.gastoCaloricoDiario) }}</span>
              <span class="paciente-formulario__metric-hint">kcal</span>
            </v-card-text>
          </v-card>
        </div>

        <PacienteConsultaHistorico :paciente-id="paciente.id" />
      </template>
    </template>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import PacienteForm from '../components/PacienteForm.vue'
import PacienteConsultaHistorico from '../components/PacienteConsultaHistorico.vue'
import pacienteService from '../services/paciente-service'
import { extrairMensagemErro } from '../../util/api-util'
import { useAppStore } from '../../store/app.store'
import { carregarRotulosEnum } from '../utils/enum-rotulos'
import type { PacienteCreateRequest, PacienteResponse } from '../types/paciente'

function formModelPadrao(): PacienteCreateRequest {
  return {
    nome: '',
    dataNascimento: '',
    sexo: 'FEMININO',
    altura: 0,
    peso: 0,
    objetivo: 'MANUTENCAO_PESO',
    nivelAtividade: 'SEDENTARIO',
    caloriasDiariasManuais: null,
    observacoes: null,
  }
}

/**
 * Uma única tela para criar, editar e visualizar paciente — mesmo padrão de
 * ConsultaFormulario.vue (modo decidido pela rota: /pacientes/novo,
 * /pacientes/:id/editar, /pacientes/:id).
 */
@Component({ name: 'PacienteFormulario', components: { PacienteForm, PacienteConsultaHistorico } })
export default class PacienteFormulario extends Vue {
  paciente: PacienteResponse | null = null
  form: PacienteCreateRequest = formModelPadrao()
  rotulos: Record<string, string> = {}
  loading = false
  salvando = false

  get appStore() {
    return useAppStore()
  }

  get pacienteId(): number | null {
    const id = this.$route.params.id as string | undefined
    return id ? Number(id) : null
  }

  get isCriacao(): boolean {
    return this.pacienteId === null
  }

  get isEdicao(): boolean {
    return !this.isCriacao
  }

  get isView(): boolean {
    return this.$route.name === 'paciente-visualizar'
  }

  get titulo(): string {
    if (this.isCriacao) return this.$t('paciente.novo') as string
    return this.isView ? (this.$t('paciente.detalhes') as string) : (this.$t('paciente.editar') as string)
  }

  async created() {
    if (this.isCriacao) {
      this.form = formModelPadrao()
      return
    }

    this.loading = true
    try {
      this.rotulos = await carregarRotulosEnum()
      const paciente = await pacienteService.buscarPorId(this.pacienteId as number)
      this.paciente = paciente
      this.form = {
        nome: paciente.nome,
        dataNascimento: paciente.dataNascimento,
        sexo: paciente.sexo,
        altura: paciente.altura,
        peso: paciente.peso,
        objetivo: paciente.objetivo,
        nivelAtividade: paciente.nivelAtividade,
        caloriasDiariasManuais: paciente.caloriasDiariasManuais,
        observacoes: paciente.observacoes,
      }
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarPaciente') as string), erro: true })
      this.voltar()
    } finally {
      this.loading = false
    }
  }

  async salvar() {
    this.salvando = true
    const payload: PacienteCreateRequest = {
      ...this.form,
      caloriasDiariasManuais: this.form.caloriasDiariasManuais || null,
      observacoes: this.form.observacoes || null,
    }
    try {
      await pacienteService.salvar(this.pacienteId, payload)
      this.appStore.setToast({ mensagem: this.$t('sucesso.salvo') as string, erro: false })
      this.voltar()
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.salvarPaciente') as string), erro: true })
    } finally {
      this.salvando = false
    }
  }

  voltar() {
    this.$router.push('/pacientes')
  }
}
</script>

<style scoped lang="scss">
.paciente-formulario__header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.paciente-formulario__title {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.paciente-formulario__subtitle {
  color: rgb(var(--v-theme-on-surface-variant));
  margin: 0;
}

.paciente-formulario__card {
  border-radius: 12px;
}

.paciente-formulario__metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 16px;

  @media (max-width: 960px) {
    grid-template-columns: 1fr;
  }
}

.paciente-formulario__metric-card {
  border-radius: 12px;
}

.paciente-formulario__metric-value {
  font-size: 1.5rem;
  font-weight: 700;
  margin-right: 6px;
}

.paciente-formulario__metric-hint {
  color: rgb(var(--v-theme-on-surface-variant));
}
</style>
