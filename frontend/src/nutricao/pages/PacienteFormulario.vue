<template>
  <div class="paciente-formulario">
    <h1>{{ isEdicao ? $t('paciente.editar') : $t('paciente.novo') }}</h1>
    <p v-if="loading">...</p>
    <PacienteForm
      v-else
      v-model="form"
      :submit-label="(isEdicao ? $t('acao.salvar') : $t('acao.criar')) as string"
      :loading="salvando"
      @submit="salvar"
      @cancel="cancelar"
    />
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import PacienteForm from '../components/PacienteForm.vue'
import pacienteService from '../services/paciente-service'
import { extrairMensagemErro } from '../../util/api-util'
import { useAppStore } from '../../store/app.store'
import type { PacienteCreateRequest } from '../types/paciente'

@Component({
  name: 'PacienteFormulario',
  components: { PacienteForm },
})
export default class PacienteFormulario extends Vue {
  form: PacienteCreateRequest = {
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

  loading = false
  salvando = false

  get pacienteId(): number | null {
    const id = this.$route.params.id as string | undefined
    return id ? Number(id) : null
  }

  get isEdicao(): boolean {
    return this.pacienteId !== null
  }

  get appStore() {
    return useAppStore()
  }

  async mounted() {
    if (!this.pacienteId) {
      return
    }
    this.loading = true
    try {
      const paciente = await pacienteService.buscarPorId(this.pacienteId)
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
      this.$router.push('/pacientes')
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.salvarPaciente') as string), erro: true })
    } finally {
      this.salvando = false
    }
  }

  cancelar() {
    this.$router.push('/pacientes')
  }
}
</script>

