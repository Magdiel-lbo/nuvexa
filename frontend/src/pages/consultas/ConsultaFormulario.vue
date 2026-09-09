<template>
  <div class="consulta-formulario">
    <template v-if="isView">
      <p v-if="carregando">...</p>
      <ConsultaDetalhe
        v-else-if="consulta"
        :consulta="consulta"
        :perfil="perfil"
        :ultima-consulta-data="ultimaConsultaData"
        :rotulos-enum="rotulosEnum"
        :excluindo="excluindo"
        :atualizando-status="atualizandoStatus"
        @voltar="voltar"
        @editar="irParaEdicao"
        @abrir-prontuario="irParaProntuario"
        @mudar-status="mudarStatus"
        @excluir="excluirConsulta"
        @agendar-retorno="agendarRetorno"
      />
    </template>

    <template v-else>
      <div class="consulta-formulario__header">
        <v-btn icon="mdi-arrow-left" variant="text" :aria-label="$t('acao.voltar')" @click="voltar" />
        <div>
          <h1 class="consulta-formulario__title">{{ titulo }}</h1>
          <p v-if="consulta" class="consulta-formulario__subtitle">{{ consulta.pacienteNome }}</p>
        </div>
      </div>

      <p v-if="carregando">...</p>

      <v-card v-else-if="form" variant="flat" color="surface-variant" class="consulta-formulario__card">
        <v-card-text class="pt-4">
          <ConsultaForm
            v-model="form"
            :submit-label="(isCriacao ? $t('acao.criar') : $t('acao.salvar')) as string"
            :loading="salvando"
            :mostrar-selecao-paciente="isCriacao"
            @submit="onSubmit"
            @cancel="voltar"
          />
        </v-card-text>
      </v-card>
    </template>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import ConsultaForm from './components/ConsultaForm.vue'
import type { ConsultaFormModel } from './components/ConsultaForm.vue'
import ConsultaDetalhe from './components/ConsultaDetalhe.vue'
import consultaService from '../../service/consulta-service'
import perfilNutricionalService from '../../nutricao/services/perfil-nutricional-service'
import { carregarRotulosEnum } from '../../nutricao/utils/enum-rotulos'
import type { Consulta, ConsultaStatus } from '../../types/consulta'
import type { PerfilNutricionalResponse } from '../../nutricao/types/perfil-nutricional'
import { useAppStore } from '../../store/app.store'
import { extrairMensagemErro } from '../../util/api-util'

function toDatetimeLocal(iso: string): string {
  return iso.slice(0, 16)
}

function fromDatetimeLocal(value: string): string {
  return value.length === 16 ? `${value}:00` : value
}

function formModelPadrao(): ConsultaFormModel {
  return {
    pacienteId: null,
    profissionalId: null,
    dataHora: '',
    duracaoMinutos: 30,
    tipo: 'PRIMEIRA_CONSULTA',
    status: 'AGENDADA',
    observacoes: null,
    motivo: null,
  }
}

/**
 * Uma única tela para criar, editar e visualizar consulta — mesmo padrão de
 * PacienteFormulario.vue (modo decidido pela rota: /consultas/novo,
 * /consultas/:id/editar, /consultas/:id).
 */
@Component({ name: 'ConsultaFormulario', components: { ConsultaForm, ConsultaDetalhe } })
export default class ConsultaFormulario extends Vue {
  consulta: Consulta | null = null
  form: ConsultaFormModel | null = null
  perfil: PerfilNutricionalResponse | null = null
  rotulosEnum: Record<string, string> = {}
  ultimaConsultaData: string | null = null
  carregando = false
  salvando = false
  excluindo = false
  atualizandoStatus = false

  get appStore() {
    return useAppStore()
  }

  get consultaId(): number | null {
    const id = this.$route.params.id as string | undefined
    return id ? Number(id) : null
  }

  get isCriacao(): boolean {
    return this.consultaId === null
  }

  get isView(): boolean {
    return this.$route.name === 'consulta-visualizar'
  }

  get titulo(): string {
    if (this.isCriacao) return this.$t('consulta.novo') as string
    return this.isView ? (this.$t('consulta.detalhes') as string) : (this.$t('consulta.editar') as string)
  }

  async created() {
    this.carregando = true
    try {
      if (this.isCriacao) {
        this.form = formModelPadrao()
        const pacienteId = Number(this.$route.query.pacienteId)
        if (pacienteId) {
          this.form.pacienteId = pacienteId
        }
        return
      }

      const consulta = await consultaService.buscarPorId(this.consultaId as number)
      if (!consulta) {
        this.$router.replace('/consultas')
        return
      }
      this.consulta = consulta
      this.form = {
        pacienteId: consulta.pacienteId,
        profissionalId: consulta.profissionalId,
        dataHora: toDatetimeLocal(consulta.dataHora),
        duracaoMinutos: consulta.duracaoMinutos,
        tipo: consulta.tipo,
        status: consulta.status,
        observacoes: consulta.observacoes,
        motivo: consulta.motivo,
      }
      if (this.isView) {
        await this.carregarDetalhe(consulta)
      }
    } finally {
      this.carregando = false
    }
  }

  async carregarDetalhe(consulta: Consulta) {
    const [perfil, rotulosEnum, outrasConsultas] = await Promise.all([
      perfilNutricionalService.buscarPorPaciente(consulta.pacienteId),
      carregarRotulosEnum(),
      consultaService.buscarPorPaciente(consulta.pacienteId).catch(() => []),
    ])
    this.perfil = perfil ?? null
    this.rotulosEnum = rotulosEnum
    const anteriores = outrasConsultas
      .filter((item) => item.id !== consulta.id && new Date(item.dataHora) < new Date(consulta.dataHora))
      .sort((a, b) => new Date(b.dataHora).getTime() - new Date(a.dataHora).getTime())
    this.ultimaConsultaData = anteriores[0]?.dataHora ?? null
  }

  async onSubmit() {
    if (!this.form) {
      return
    }
    this.salvando = true
    try {
      if (this.isCriacao) {
        await this.criar()
      } else {
        await this.atualizar()
      }
      this.appStore.setToast({ mensagem: this.$t('sucesso.consultaSalva') as string, erro: false })
      this.voltar()
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.salvarConsulta') as string), erro: true })
    } finally {
      this.salvando = false
    }
  }

  private async criar() {
    const form = this.form as ConsultaFormModel
    if (!form.pacienteId || !form.profissionalId) {
      return
    }
    await consultaService.criar({
      pacienteId: form.pacienteId,
      profissionalId: form.profissionalId,
      dataHora: fromDatetimeLocal(form.dataHora),
      duracaoMinutos: form.duracaoMinutos,
      tipo: form.tipo,
      status: form.status,
      observacoes: form.observacoes,
      motivo: form.motivo,
    })
  }

  private async atualizar() {
    const form = this.form as ConsultaFormModel
    if (!form.profissionalId) {
      return
    }
    await consultaService.atualizar((this.consulta as Consulta).id, {
      profissionalId: form.profissionalId,
      dataHora: fromDatetimeLocal(form.dataHora),
      duracaoMinutos: form.duracaoMinutos,
      tipo: form.tipo,
      status: form.status,
      observacoes: form.observacoes,
      motivo: form.motivo,
    })
  }

  irParaEdicao() {
    this.$router.push(`/consultas/${this.consultaId}/editar`)
  }

  irParaProntuario() {
    const consulta = this.consulta
    if (!consulta) {
      return
    }
    this.$router.push({ path: '/prontuarios', query: { q: consulta.pacienteNome } })
  }

  agendarRetorno() {
    const consulta = this.consulta
    if (!consulta) {
      return
    }
    this.$router.push({ path: '/consultas/novo', query: { pacienteId: String(consulta.pacienteId) } })
  }

  async mudarStatus(status: ConsultaStatus) {
    const consulta = this.consulta
    if (!consulta) {
      return
    }
    this.atualizandoStatus = true
    try {
      this.consulta = await consultaService.atualizarStatus(consulta, status)
      this.appStore.setToast({ mensagem: this.$t('sucesso.statusConsultaAtualizado') as string, erro: false })
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.salvarConsulta') as string), erro: true })
    } finally {
      this.atualizandoStatus = false
    }
  }

  async excluirConsulta() {
    const consulta = this.consulta
    if (!consulta) {
      return
    }
    this.excluindo = true
    try {
      await consultaService.excluir(consulta.id)
      this.appStore.setToast({ mensagem: this.$t('sucesso.consultaExcluida') as string, erro: false })
      this.voltar()
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.excluirConsulta') as string), erro: true })
    } finally {
      this.excluindo = false
    }
  }

  voltar() {
    this.$router.push('/consultas')
  }
}
</script>

<style scoped lang="scss">
.consulta-formulario__header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.consulta-formulario__title {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.consulta-formulario__subtitle {
  color: rgb(var(--v-theme-on-surface-variant));
  margin: 0;
}

.consulta-formulario__card {
  border-radius: 12px;
}
</style>
