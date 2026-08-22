<template>
  <div class="patient-formulario">
    <h1>{{ isEdicao ? $t('paciente.editar') : $t('paciente.novo') }}</h1>
    <p v-if="loading">...</p>
    <PatientForm
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
import PatientForm from './components/PatientForm.vue'
import patientService from '../../service/patient-service'
import { extrairMensagemErro } from '../../util/api-util'
import { useAppStore } from '../../store/app.store'
import type { PatientCreateRequest } from '../../types/patient'

@Component({
  name: 'PatientFormulario',
  components: { PatientForm },
})
export default class PatientFormulario extends Vue {
  form: PatientCreateRequest = {
    name: '',
    birthDate: '',
    gender: 'FEMALE',
    height: 0,
    weight: 0,
    goal: 'MAINTAIN_WEIGHT',
    activityLevel: 'SEDENTARY',
    manualDailyCalories: null,
    notes: null,
  }

  loading = false
  salvando = false

  get patientId(): number | null {
    const id = this.$route.params.id as string | undefined
    return id ? Number(id) : null
  }

  get isEdicao(): boolean {
    return this.patientId !== null
  }

  get appStore() {
    return useAppStore()
  }

  async mounted() {
    if (!this.patientId) {
      return
    }
    this.loading = true
    try {
      const paciente = await patientService.buscarPorId(this.patientId)
      this.form = {
        name: paciente.name,
        birthDate: paciente.birthDate,
        gender: paciente.gender,
        height: paciente.height,
        weight: paciente.weight,
        goal: paciente.goal,
        activityLevel: paciente.activityLevel,
        manualDailyCalories: paciente.manualDailyCalories,
        notes: paciente.notes,
      }
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarPaciente') as string), erro: true })
    } finally {
      this.loading = false
    }
  }

  async salvar() {
    this.salvando = true
    const payload: PatientCreateRequest = {
      ...this.form,
      manualDailyCalories: this.form.manualDailyCalories || null,
      notes: this.form.notes || null,
    }
    try {
      await patientService.salvar(this.patientId, payload)
      this.appStore.setToast({ mensagem: this.$t('sucesso.salvo') as string, erro: false })
      this.$router.push('/patients')
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.salvarPaciente') as string), erro: true })
    } finally {
      this.salvando = false
    }
  }

  cancelar() {
    this.$router.push('/patients')
  }
}
</script>

