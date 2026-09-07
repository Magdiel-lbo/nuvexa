<template>
  <div class="prontuario-formulario">
    <div class="prontuario-formulario__header">
      <v-btn icon="mdi-arrow-left" variant="text" :aria-label="$t('acao.voltar')" @click="voltar" />
      <div>
        <h1 class="prontuario-formulario__title">{{ titulo }}</h1>
        <p v-if="prontuario" class="prontuario-formulario__subtitle">{{ prontuario.pacienteNome }}</p>
      </div>
    </div>

    <p v-if="carregando">...</p>

    <v-card v-else-if="form" variant="flat" color="surface-variant" class="prontuario-formulario__card">
      <v-card-text class="pt-4">
        <ProntuarioForm
          v-model="form"
          :submit-label="(isCriacao ? $t('acao.criar') : $t('acao.salvar')) as string"
          :loading="salvando"
          :readonly="isView"
          :mostrar-selecao-paciente="isCriacao"
          :paciente-options="pacienteOptions"
          :profissional-options="profissionalOptions"
          @submit="onSubmit"
          @cancel="voltar"
        />
      </v-card-text>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import ProntuarioForm from './components/ProntuarioForm.vue'
import type { ProntuarioFormModel } from './components/ProntuarioForm.vue'
import prontuarioService from '../../service/prontuario-service'
import consultaService from '../../service/consulta-service'
import pacienteService from '../../service/paciente-service'
import type { Prontuario } from '../../types/prontuario'
import { useAppStore } from '../../store/app.store'
import { extrairMensagemErro } from '../../util/api-util'

function formModelPadrao(): ProntuarioFormModel {
  return {
    pacienteId: null,
    autorId: null,
    secao: 'ANAMNESE',
    status: 'RASCUNHO',
    conteudo: null,
    comAnexo: false,
  }
}

/**
 * Uma única tela para criar, editar e visualizar prontuário — mesmo padrão de
 * ConsultaFormulario.vue/PacienteFormulario.vue (modo decidido pela rota:
 * /prontuarios/novo, /prontuarios/:id/editar, /prontuarios/:id).
 */
@Component({ name: 'ProntuarioFormulario', components: { ProntuarioForm } })
export default class ProntuarioFormulario extends Vue {
  prontuario: Prontuario | null = null
  form: ProntuarioFormModel | null = null
  pacienteOptions: { value: number; label: string }[] = []
  profissionalOptions: { value: number; label: string }[] = []
  carregando = false
  salvando = false

  get appStore() {
    return useAppStore()
  }

  get prontuarioId(): number | null {
    const id = this.$route.params.id as string | undefined
    return id ? Number(id) : null
  }

  get isCriacao(): boolean {
    return this.prontuarioId === null
  }

  get isView(): boolean {
    return this.$route.name === 'prontuario-visualizar'
  }

  get titulo(): string {
    if (this.isCriacao) return this.$t('prontuario.novo') as string
    return this.isView ? (this.$t('prontuario.detalhes') as string) : (this.$t('prontuario.editar') as string)
  }

  async created() {
    this.carregando = true
    try {
      await this.carregarProfissionais()

      if (this.isCriacao) {
        await this.carregarPacientes()
        this.form = formModelPadrao()
        return
      }

      const prontuario = await prontuarioService.buscarPorId(this.prontuarioId as number)
      if (!prontuario) {
        this.$router.replace('/prontuarios')
        return
      }
      this.prontuario = prontuario
      this.form = {
        pacienteId: prontuario.pacienteId,
        autorId: prontuario.autorId,
        secao: prontuario.secao,
        status: prontuario.status,
        conteudo: prontuario.conteudo,
        comAnexo: prontuario.comAnexo,
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

  async carregarProfissionais() {
    try {
      const profissionais = await consultaService.listarProfissionais()
      this.profissionalOptions = profissionais.map((profissional) => ({ value: profissional.id, label: profissional.nome }))
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarProfissionais') as string), erro: true })
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
      this.appStore.setToast({ mensagem: this.$t('sucesso.prontuarioSalvo') as string, erro: false })
      this.voltar()
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.salvarProntuario') as string), erro: true })
    } finally {
      this.salvando = false
    }
  }

  private async criar() {
    const form = this.form as ProntuarioFormModel
    if (!form.pacienteId || !form.autorId) {
      return
    }
    await prontuarioService.criar({
      pacienteId: form.pacienteId,
      autorId: form.autorId,
      secao: form.secao,
      status: form.status,
      conteudo: form.conteudo,
      comAnexo: form.comAnexo,
    })
  }

  private async atualizar() {
    const form = this.form as ProntuarioFormModel
    if (!form.autorId) {
      return
    }
    await prontuarioService.atualizar((this.prontuario as Prontuario).id, {
      autorId: form.autorId,
      secao: form.secao,
      status: form.status,
      conteudo: form.conteudo,
      comAnexo: form.comAnexo,
    })
  }

  voltar() {
    this.$router.push('/prontuarios')
  }
}
</script>

<style scoped lang="scss">
.prontuario-formulario__header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.prontuario-formulario__title {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.prontuario-formulario__subtitle {
  color: rgb(var(--v-theme-on-surface-variant));
  margin: 0;
}

.prontuario-formulario__card {
  border-radius: 12px;
}
</style>
