<template>
  <div class="consulta-formulario">
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
          :readonly="isView"
          :mostrar-selecao-paciente="isCriacao"
          :paciente-options="pacienteOptions"
          @submit="onSubmit"
          @cancel="voltar"
        />
      </v-card-text>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import ConsultaForm from './components/ConsultaForm.vue'
import type { ConsultaFormModel } from './components/ConsultaForm.vue'
import consultaService from '../../service/consulta-service'
import pacienteService from '../../verticals/nutricao/services/paciente-service'
import type { Consulta } from '../../types/consulta'
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
    dataHora: '',
    duracaoMinutos: 30,
    tipo: 'PRIMEIRA_CONSULTA',
    status: 'AGENDADA',
    observacoes: null,
  }
}

/**
 * Uma única tela para criar, editar e visualizar consulta — mesmo padrão de
 * PacienteFormulario.vue (modo decidido pela rota: /consultas/novo,
 * /consultas/:id/editar, /consultas/:id).
 */
@Component({ name: 'ConsultaFormulario', components: { ConsultaForm } })
export default class ConsultaFormulario extends Vue {
  consulta: Consulta | null = null
  form: ConsultaFormModel | null = null
  pacienteOptions: { value: number; label: string }[] = []
  carregando = false
  salvando = false

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
        await this.carregarPacientes()
        this.form = formModelPadrao()
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
        dataHora: toDatetimeLocal(consulta.dataHora),
        duracaoMinutos: consulta.duracaoMinutos,
        tipo: consulta.tipo,
        status: consulta.status,
        observacoes: consulta.observacoes,
      }
    } finally {
      this.carregando = false
    }
  }

  async carregarPacientes() {
    try {
      const pacientes = await pacienteService.listar()
      this.pacienteOptions = pacientes.map((paciente) => ({ value: paciente.id, label: paciente.nome }))
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarPacientes') as string), erro: true })
    }
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
    if (!form.pacienteId) {
      return
    }
    await consultaService.criar({
      pacienteId: form.pacienteId,
      dataHora: fromDatetimeLocal(form.dataHora),
      duracaoMinutos: form.duracaoMinutos,
      tipo: form.tipo,
      status: form.status,
      observacoes: form.observacoes,
    })
  }

  private async atualizar() {
    const form = this.form as ConsultaFormModel
    await consultaService.atualizar((this.consulta as Consulta).id, {
      dataHora: fromDatetimeLocal(form.dataHora),
      duracaoMinutos: form.duracaoMinutos,
      tipo: form.tipo,
      status: form.status,
      observacoes: form.observacoes,
    })
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
