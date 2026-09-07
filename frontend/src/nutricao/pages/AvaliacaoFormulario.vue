<template>
  <div class="avaliacao-formulario">
    <div class="avaliacao-formulario__header">
      <v-btn icon="mdi-arrow-left" variant="text" :aria-label="$t('acao.voltar')" @click="voltar" />
      <div>
        <h1 class="avaliacao-formulario__title">{{ titulo }}</h1>
        <p v-if="avaliacao" class="avaliacao-formulario__subtitle">{{ avaliacao.pacienteNome }}</p>
      </div>
    </div>

    <p v-if="carregando">...</p>

    <v-card v-else-if="form" variant="flat" color="surface-variant" class="avaliacao-formulario__card">
      <v-card-text class="pt-4">
        <AvaliacaoForm
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
import AvaliacaoForm from '../components/AvaliacaoForm.vue'
import type { AvaliacaoFormModel } from '../components/AvaliacaoForm.vue'
import avaliacaoService from '../services/avaliacao-service'
import consultaService from '../../service/consulta-service'
import pacienteService from '../../service/paciente-service'
import type { Avaliacao } from '../types/avaliacao'
import { useAppStore } from '../../store/app.store'
import { extrairMensagemErro } from '../../util/api-util'

function formModelPadrao(): AvaliacaoFormModel {
  return {
    pacienteId: null,
    avaliadorId: null,
    data: '',
    tipo: 'BIOIMPEDANCIA',
    status: 'AGENDADA',
    peso: null,
    percentualGordura: null,
  }
}

/**
 * Uma única tela para criar, editar e visualizar avaliação — mesmo padrão de
 * ProntuarioFormulario.vue/ConsultaFormulario.vue (modo decidido pela rota:
 * /avaliacoes/novo, /avaliacoes/:id/editar, /avaliacoes/:id).
 */
@Component({ name: 'AvaliacaoFormulario', components: { AvaliacaoForm } })
export default class AvaliacaoFormulario extends Vue {
  avaliacao: Avaliacao | null = null
  form: AvaliacaoFormModel | null = null
  pacienteOptions: { value: number; label: string }[] = []
  profissionalOptions: { value: number; label: string }[] = []
  carregando = false
  salvando = false

  get appStore() {
    return useAppStore()
  }

  get avaliacaoId(): number | null {
    const id = this.$route.params.id as string | undefined
    return id ? Number(id) : null
  }

  get isCriacao(): boolean {
    return this.avaliacaoId === null
  }

  get isView(): boolean {
    return this.$route.name === 'avaliacao-visualizar'
  }

  get titulo(): string {
    if (this.isCriacao) return this.$t('avaliacao.novo') as string
    return this.isView ? (this.$t('avaliacao.detalhes') as string) : (this.$t('avaliacao.editar') as string)
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

      const avaliacao = await avaliacaoService.buscarPorId(this.avaliacaoId as number)
      if (!avaliacao) {
        this.$router.replace('/avaliacoes')
        return
      }
      this.avaliacao = avaliacao
      this.form = {
        pacienteId: avaliacao.pacienteId,
        avaliadorId: avaliacao.avaliadorId,
        data: avaliacao.data,
        tipo: avaliacao.tipo,
        status: avaliacao.status,
        peso: avaliacao.peso,
        percentualGordura: avaliacao.percentualGordura,
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
      this.appStore.setToast({ mensagem: this.$t('sucesso.avaliacaoSalva') as string, erro: false })
      this.voltar()
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.salvarAvaliacao') as string), erro: true })
    } finally {
      this.salvando = false
    }
  }

  private async criar() {
    const form = this.form as AvaliacaoFormModel
    if (!form.pacienteId || !form.avaliadorId) {
      return
    }
    await avaliacaoService.criar({
      pacienteId: form.pacienteId,
      avaliadorId: form.avaliadorId,
      data: form.data,
      tipo: form.tipo,
      status: form.status,
      peso: form.peso,
      percentualGordura: form.percentualGordura,
    })
  }

  private async atualizar() {
    const form = this.form as AvaliacaoFormModel
    if (!form.avaliadorId) {
      return
    }
    await avaliacaoService.atualizar((this.avaliacao as Avaliacao).id, {
      avaliadorId: form.avaliadorId,
      data: form.data,
      tipo: form.tipo,
      status: form.status,
      peso: form.peso,
      percentualGordura: form.percentualGordura,
    })
  }

  voltar() {
    this.$router.push('/avaliacoes')
  }
}
</script>

<style scoped lang="scss">
.avaliacao-formulario__header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.avaliacao-formulario__title {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.avaliacao-formulario__subtitle {
  color: rgb(var(--v-theme-on-surface-variant));
  margin: 0;
}

.avaliacao-formulario__card {
  border-radius: 12px;
}
</style>
