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
            :criacao="isCriacao"
            @submit="salvar"
            @cancel="voltar"
          />
        </v-card-text>
      </v-card>

      <template v-if="paciente && perfil">
        <div class="paciente-formulario__metrics">
          <v-card variant="flat" color="surface-variant" class="paciente-formulario__metric-card">
            <v-card-title>{{ $t('paciente.imc') }}</v-card-title>
            <v-card-text>
              <template v-if="perfil.imc !== null">
                <span class="paciente-formulario__metric-value">{{ perfil.imc.toFixed(1) }}</span>
                <span class="paciente-formulario__metric-hint">{{ perfil.classificacaoImc }}</span>
              </template>
              <span v-else class="paciente-formulario__metric-hint">{{ $t('paciente.detalhe.semAvaliacao') }}</span>
            </v-card-text>
          </v-card>

          <v-card variant="flat" color="surface-variant" class="paciente-formulario__metric-card">
            <v-card-title>{{ $t('paciente.detalhe.taxaMetabolicaBasal') }}</v-card-title>
            <v-card-text>
              <template v-if="perfil.taxaMetabolicaBasal !== null">
                <span class="paciente-formulario__metric-value">{{ Math.round(perfil.taxaMetabolicaBasal) }}</span>
                <span class="paciente-formulario__metric-hint">kcal</span>
              </template>
              <span v-else class="paciente-formulario__metric-hint">{{ $t('paciente.detalhe.semAvaliacao') }}</span>
            </v-card-text>
          </v-card>

          <v-card variant="flat" color="surface-variant" class="paciente-formulario__metric-card">
            <v-card-title>{{ $t('paciente.detalhe.gastoCaloricoTotal') }}</v-card-title>
            <v-card-text>
              <template v-if="perfil.gastoCaloricoDiario !== null">
                <span class="paciente-formulario__metric-value">{{ Math.round(perfil.gastoCaloricoDiario) }}</span>
                <span class="paciente-formulario__metric-hint">kcal</span>
              </template>
              <span v-else class="paciente-formulario__metric-hint">{{ $t('paciente.detalhe.semAvaliacao') }}</span>
            </v-card-text>
          </v-card>
        </div>

        <p v-if="perfil.peso !== null && perfil.avaliacaoAtualId" class="paciente-formulario__peso-atual">
          {{ $t('paciente.detalhe.pesoAtual') }}: <strong>{{ perfil.peso }} kg</strong>
          <v-btn variant="text" density="compact" color="primary" @click="corrigirPeso">
            {{ $t('acao.corrigir') }}
          </v-btn>
        </p>

        <PacienteConsultaHistorico :paciente-id="paciente.id" />
      </template>
    </template>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import PacienteForm from './components/PacienteForm.vue'
import type { PacienteFormModel } from './components/PacienteForm.vue'
import PacienteConsultaHistorico from './components/PacienteConsultaHistorico.vue'
import pacienteService from '../../service/paciente-service'
import perfilNutricionalService from '../../nutricao/services/perfil-nutricional-service'
import { extrairMensagemErro } from '../../util/api-util'
import { useAppStore } from '../../store/app.store'
import { carregarRotulosEnum } from '../../nutricao/utils/enum-rotulos'
import type { PacienteResponse } from '../../types/paciente'
import type { PerfilNutricionalResponse } from '../../nutricao/types/perfil-nutricional'

function formModelPadrao(): PacienteFormModel {
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
 * ConsultaFormulario.vue/ProntuarioFormulario.vue (modo decidido pela rota: /pacientes/novo,
 * /pacientes/:id/editar, /pacientes/:id).
 *
 * Paciente (core) e PerfilNutricional (nutricao) são recursos separados no backend — esta tela
 * compõe os dois num único formulário porque, hoje, todo paciente é criado/editado junto com seu
 * perfil nutricional. Se o perfil tiver sido removido (ver "Excluir" na listagem, que só apaga o
 * perfil, preservando o Paciente), `perfil` vem `null` e salvar() cria um novo em vez de atualizar.
 */
@Component({ name: 'PacienteFormulario', components: { PacienteForm, PacienteConsultaHistorico } })
export default class PacienteFormulario extends Vue {
  paciente: PacienteResponse | null = null
  perfil: PerfilNutricionalResponse | null = null
  form: PacienteFormModel = formModelPadrao()
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
      const [paciente, perfil] = await Promise.all([
        pacienteService.buscarPorId(this.pacienteId as number),
        perfilNutricionalService.buscarPorPaciente(this.pacienteId as number),
      ])
      this.paciente = paciente
      this.perfil = perfil ?? null
      this.form = {
        nome: paciente.nome,
        dataNascimento: paciente.dataNascimento,
        sexo: paciente.sexo,
        altura: perfil?.altura ?? 0,
        peso: perfil?.peso ?? 0,
        objetivo: perfil?.objetivo ?? 'MANUTENCAO_PESO',
        nivelAtividade: perfil?.nivelAtividade ?? 'SEDENTARIO',
        caloriasDiariasManuais: perfil?.caloriasDiariasManuais ?? null,
        observacoes: perfil?.observacoes ?? null,
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
    try {
      const perfilPayloadBase = {
        altura: this.form.altura,
        objetivo: this.form.objetivo,
        nivelAtividade: this.form.nivelAtividade,
        caloriasDiariasManuais: this.form.caloriasDiariasManuais || null,
        observacoes: this.form.observacoes || null,
      }

      if (this.isCriacao) {
        const paciente = await pacienteService.criar({ nome: this.form.nome, dataNascimento: this.form.dataNascimento, sexo: this.form.sexo })
        await perfilNutricionalService.criar(paciente.id, { ...perfilPayloadBase, pesoInicial: this.form.peso })
      } else {
        const pacienteId = this.pacienteId as number
        await Promise.all([
          pacienteService.atualizar(pacienteId, { nome: this.form.nome, dataNascimento: this.form.dataNascimento, sexo: this.form.sexo }),
          this.perfil
            ? perfilNutricionalService.atualizar(pacienteId, perfilPayloadBase)
            : perfilNutricionalService.criar(pacienteId, { ...perfilPayloadBase, pesoInicial: this.form.peso }),
        ])
      }
      this.appStore.setToast({ mensagem: this.$t('sucesso.salvo') as string, erro: false })
      this.voltar()
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.salvarPaciente') as string), erro: true })
    } finally {
      this.salvando = false
    }
  }

  corrigirPeso() {
    if (this.perfil?.avaliacaoAtualId) {
      this.$router.push({ name: 'avaliacao-editar', params: { id: this.perfil.avaliacaoAtualId } })
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

.paciente-formulario__peso-atual {
  margin-top: 16px;
  color: rgb(var(--v-theme-on-surface-variant));
}
</style>
