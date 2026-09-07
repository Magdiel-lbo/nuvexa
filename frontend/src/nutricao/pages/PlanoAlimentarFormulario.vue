<template>
  <div class="plano-alimentar-formulario">
    <div class="plano-alimentar-formulario__header">
      <v-btn icon="mdi-arrow-left" variant="text" :aria-label="$t('acao.voltar')" @click="voltar" />
      <div>
        <h1 class="plano-alimentar-formulario__title">{{ titulo }}</h1>
        <p v-if="plano" class="plano-alimentar-formulario__subtitle">{{ plano.pacienteNome }}</p>
      </div>
    </div>

    <p v-if="carregando">...</p>

    <v-card v-else-if="form" variant="flat" color="surface-variant" class="plano-alimentar-formulario__card">
      <v-card-text class="pt-4">
        <PlanoAlimentarForm
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
import PlanoAlimentarForm from '../components/PlanoAlimentarForm.vue'
import type { PlanoAlimentarFormModel } from '../components/PlanoAlimentarForm.vue'
import planoAlimentarService from '../services/plano-alimentar-service'
import consultaService from '../../service/consulta-service'
import pacienteService from '../../service/paciente-service'
import type { PlanoAlimentar } from '../types/plano-alimentar'
import { useAppStore } from '../../store/app.store'
import { extrairMensagemErro } from '../../util/api-util'

function formModelPadrao(): PlanoAlimentarFormModel {
  return {
    pacienteId: null,
    autorId: null,
    nome: '',
    dataInicio: '',
    calorias: null,
    refeicoesPorDia: null,
    status: 'RASCUNHO',
  }
}

/**
 * Uma única tela para criar, editar e visualizar plano alimentar — mesmo padrão de
 * ProntuarioFormulario.vue/AvaliacaoFormulario.vue (modo decidido pela rota: /nutricao/novo,
 * /nutricao/:id/editar, /nutricao/:id).
 */
@Component({ name: 'PlanoAlimentarFormulario', components: { PlanoAlimentarForm } })
export default class PlanoAlimentarFormulario extends Vue {
  plano: PlanoAlimentar | null = null
  form: PlanoAlimentarFormModel | null = null
  pacienteOptions: { value: number; label: string }[] = []
  profissionalOptions: { value: number; label: string }[] = []
  carregando = false
  salvando = false

  get appStore() {
    return useAppStore()
  }

  get planoId(): number | null {
    const id = this.$route.params.id as string | undefined
    return id ? Number(id) : null
  }

  get isCriacao(): boolean {
    return this.planoId === null
  }

  get isView(): boolean {
    return this.$route.name === 'nutricao-visualizar'
  }

  get titulo(): string {
    if (this.isCriacao) return this.$t('planoAlimentar.novo') as string
    return this.isView ? (this.$t('planoAlimentar.detalhes') as string) : (this.$t('planoAlimentar.editar') as string)
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

      const plano = await planoAlimentarService.buscarPorId(this.planoId as number)
      if (!plano) {
        this.$router.replace('/nutricao')
        return
      }
      this.plano = plano
      this.form = {
        pacienteId: plano.pacienteId,
        autorId: plano.autorId,
        nome: plano.nome,
        dataInicio: plano.dataInicio,
        calorias: plano.calorias,
        refeicoesPorDia: plano.refeicoesPorDia,
        status: plano.status,
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
      this.appStore.setToast({ mensagem: this.$t('sucesso.planoAlimentarSalvo') as string, erro: false })
      this.voltar()
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.salvarPlanoAlimentar') as string), erro: true })
    } finally {
      this.salvando = false
    }
  }

  private async criar() {
    const form = this.form as PlanoAlimentarFormModel
    if (!form.pacienteId || !form.autorId || form.calorias == null || form.refeicoesPorDia == null) {
      return
    }
    await planoAlimentarService.criar({
      pacienteId: form.pacienteId,
      autorId: form.autorId,
      nome: form.nome,
      dataInicio: form.dataInicio,
      calorias: form.calorias,
      refeicoesPorDia: form.refeicoesPorDia,
      status: form.status,
    })
  }

  private async atualizar() {
    const form = this.form as PlanoAlimentarFormModel
    if (!form.autorId || form.calorias == null || form.refeicoesPorDia == null) {
      return
    }
    await planoAlimentarService.atualizar((this.plano as PlanoAlimentar).id, {
      autorId: form.autorId,
      nome: form.nome,
      dataInicio: form.dataInicio,
      calorias: form.calorias,
      refeicoesPorDia: form.refeicoesPorDia,
      status: form.status,
    })
  }

  voltar() {
    this.$router.push('/nutricao')
  }
}
</script>

<style scoped lang="scss">
.plano-alimentar-formulario__header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.plano-alimentar-formulario__title {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.plano-alimentar-formulario__subtitle {
  color: rgb(var(--v-theme-on-surface-variant));
  margin: 0;
}

.plano-alimentar-formulario__card {
  border-radius: 12px;
}
</style>
