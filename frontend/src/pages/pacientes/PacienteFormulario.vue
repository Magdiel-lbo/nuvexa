<template>
  <div class="paciente-formulario">
    <template v-if="isView">
      <p v-if="carregando">...</p>
      <PacienteDetalhe
        v-else-if="paciente"
        :paciente="paciente"
        :perfil="perfil"
        :avaliacao-recente="avaliacaoRecente"
        :avaliacoes="avaliacoes"
        :consultas="consultas"
        :planos="planos"
        :prontuarios="prontuarios"
        :rotulos-enum="rotulos"
        :rotulos-avaliacao="rotulosAvaliacao"
        :rotulos-prontuario="rotulosProntuario"
        :rotulos-plano-alimentar="rotulosPlanoAlimentar"
        @voltar="voltar"
        @abrir-prontuario="irParaProntuario"
        @nova-consulta="irParaNovaConsulta"
        @abrir-consulta="irParaConsulta"
        @nova-avaliacao="irParaNovaAvaliacao"
        @abrir-avaliacao="irParaAvaliacao"
        @ver-avaliacoes="irParaAvaliacoes"
        @novo-plano="irParaNovoPlano"
        @abrir-plano="irParaPlano"
        @novo-registro="irParaNovoRegistro"
        @abrir-registro="irParaRegistro"
      />
    </template>

    <template v-else>
      <div class="paciente-formulario__header">
        <v-btn icon="mdi-arrow-left" variant="text" :aria-label="$t('acao.voltar')" @click="voltar" />
        <div>
          <h1 class="paciente-formulario__title">{{ titulo }}</h1>
          <p v-if="paciente" class="paciente-formulario__subtitle">
            {{ rotulos[paciente.sexo] ?? paciente.sexo }} · {{ paciente.idade }} {{ $t('paciente.detalhe.anos') }}
          </p>
        </div>
      </div>

      <p v-if="carregando">...</p>

      <v-card v-else variant="flat" color="surface-variant" class="paciente-formulario__card">
        <v-card-text class="pt-4">
          <PacienteForm
            v-model="form"
            :submit-label="(isEdicao ? $t('acao.salvar') : $t('acao.criar')) as string"
            :loading="salvando"
            :criacao="isCriacao"
            @submit="salvar"
            @cancel="voltar"
          />
        </v-card-text>
      </v-card>
    </template>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import PacienteForm from './components/PacienteForm.vue'
import type { PacienteFormModel } from './components/PacienteForm.vue'
import PacienteDetalhe from './components/PacienteDetalhe.vue'
import pacienteService from '../../service/paciente-service'
import perfilNutricionalService from '../../nutricao/services/perfil-nutricional-service'
import consultaService from '../../service/consulta-service'
import avaliacaoService from '../../nutricao/services/avaliacao-service'
import planoAlimentarService from '../../nutricao/services/plano-alimentar-service'
import prontuarioService from '../../service/prontuario-service'
import { extrairMensagemErro } from '../../util/api-util'
import { useAppStore } from '../../store/app.store'
import { carregarRotulosEnum } from '../../nutricao/utils/enum-rotulos'
import { carregarRotulosAvaliacao } from '../../nutricao/utils/avaliacao-rotulos'
import { carregarRotulosProntuario } from '../../util/prontuario-rotulos'
import { carregarRotulosPlanoAlimentar } from '../../nutricao/utils/plano-alimentar-rotulos'
import type { PacienteResponse } from '../../types/paciente'
import type { PerfilNutricionalResponse } from '../../nutricao/types/perfil-nutricional'
import type { Avaliacao } from '../../nutricao/types/avaliacao'
import type { Consulta } from '../../types/consulta'
import type { PlanoAlimentar } from '../../nutricao/types/plano-alimentar'
import type { Prontuario } from '../../types/prontuario'

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
@Component({ name: 'PacienteFormulario', components: { PacienteForm, PacienteDetalhe } })
export default class PacienteFormulario extends Vue {
  paciente: PacienteResponse | null = null
  perfil: PerfilNutricionalResponse | null = null
  avaliacaoRecente: Avaliacao | null = null
  avaliacoes: Avaliacao[] = []
  consultas: Consulta[] = []
  planos: PlanoAlimentar[] = []
  prontuarios: Prontuario[] = []
  form: PacienteFormModel = formModelPadrao()
  rotulos: Record<string, string> = {}
  rotulosAvaliacao: Record<string, string> = {}
  rotulosProntuario: Record<string, string> = {}
  rotulosPlanoAlimentar: Record<string, string> = {}
  carregando = false
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

    this.carregando = true
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
      if (this.isView) {
        await this.carregarDetalhe(paciente.id, perfil ?? null)
      }
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarPaciente') as string), erro: true })
      this.voltar()
    } finally {
      this.carregando = false
    }
  }

  async carregarDetalhe(pacienteId: number, perfil: PerfilNutricionalResponse | null) {
    const [avaliacoes, consultas, planos, prontuarios, rotulosAvaliacao, rotulosProntuario, rotulosPlanoAlimentar] = await Promise.all([
      avaliacaoService.buscarPorPaciente(pacienteId).catch(() => []),
      consultaService.buscarPorPaciente(pacienteId).catch(() => []),
      planoAlimentarService.buscarPorPaciente(pacienteId).catch(() => []),
      prontuarioService.buscarPorPaciente(pacienteId).catch(() => []),
      carregarRotulosAvaliacao(),
      carregarRotulosProntuario(),
      carregarRotulosPlanoAlimentar(),
    ])
    this.avaliacoes = avaliacoes
    this.consultas = consultas
    this.planos = planos
    this.prontuarios = prontuarios
    this.rotulosAvaliacao = rotulosAvaliacao
    this.rotulosProntuario = rotulosProntuario
    this.rotulosPlanoAlimentar = rotulosPlanoAlimentar
    this.avaliacaoRecente = perfil?.avaliacaoAtualId ? ((await avaliacaoService.buscarPorId(perfil.avaliacaoAtualId)) ?? null) : null
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

  voltar() {
    this.$router.push('/pacientes')
  }

  irParaProntuario() {
    const paciente = this.paciente
    if (!paciente) {
      return
    }
    this.$router.push({ path: '/prontuarios', query: { q: paciente.nome } })
  }

  irParaNovaConsulta() {
    this.$router.push({ path: '/consultas/novo', query: { pacienteId: String(this.pacienteId) } })
  }

  irParaConsulta(id: number) {
    this.$router.push(`/consultas/${id}`)
  }

  irParaNovaAvaliacao() {
    this.$router.push({ path: '/avaliacoes/novo', query: { pacienteId: String(this.pacienteId) } })
  }

  irParaAvaliacao(id: number) {
    this.$router.push(`/avaliacoes/${id}`)
  }

  irParaAvaliacoes() {
    const paciente = this.paciente
    if (!paciente) {
      return
    }
    this.$router.push({ path: '/avaliacoes', query: { q: paciente.nome } })
  }

  irParaNovoPlano() {
    this.$router.push({ path: '/nutricao/novo', query: { pacienteId: String(this.pacienteId) } })
  }

  irParaPlano(id: number) {
    this.$router.push(`/nutricao/${id}`)
  }

  irParaNovoRegistro() {
    this.$router.push({ path: '/prontuarios/novo', query: { pacienteId: String(this.pacienteId) } })
  }

  irParaRegistro(id: number) {
    this.$router.push(`/prontuarios/${id}`)
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
</style>
