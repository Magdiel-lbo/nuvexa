<template>
  <v-form ref="form" v-model="formValido" :disabled="readonly" @submit.prevent="salvar">
    <v-row>
      <v-col cols="6">
        <v-text-field v-model="model.name" :label="$t('paciente.nome')" :rules="[rules.obrigatorio]" />
      </v-col>

      <v-col cols="12" md="6">
        <v-text-field
          v-model="model.birthDate"
          type="date"
          :label="$t('paciente.dataNascimento')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-select
          v-model="model.gender"
          :items="sexoOptions"
          item-title="label"
          item-value="value"
          :label="$t('paciente.sexo')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-text-field
          v-model.number="model.height"
          type="number"
          step="0.01"
          :label="$t('paciente.altura')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-text-field
          v-model.number="model.weight"
          type="number"
          step="0.01"
          :label="$t('paciente.peso')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-select
          v-model="model.goal"
          :items="objetivoOptions"
          item-title="label"
          item-value="value"
          :label="$t('paciente.objetivo')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-select
          v-model="model.activityLevel"
          :items="nivelAtividadeOptions"
          item-title="label"
          item-value="value"
          :label="$t('paciente.nivelAtividade')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-text-field
          v-model.number="model.manualDailyCalories"
          type="number"
          step="0.01"
          :label="$t('paciente.gastoCaloricoManual')"
        />
      </v-col>

      <v-col cols="6">
        <v-textarea v-model="model.notes" :label="$t('paciente.observacoes')" rows="3" />
      </v-col>
    </v-row>

    <div v-if="!readonly" class="d-flex ga-2 mt-4">
      <v-btn variant="outlined" @click="cancelar">{{ $t('acao.cancelar') }}</v-btn>
      <v-btn color="primary" type="submit" :loading="loading">{{ submitLabel }}</v-btn>
    </div>
  </v-form>
</template>

<script lang="ts">
import { Component, Prop, VModel, Emit, Vue } from 'vue-facing-decorator'
import patientService from '../../../service/patient-service'
import type { EnumOption, PatientCreateRequest } from '../../../types/patient'

@Component({ name: 'PatientForm' })
export default class PatientForm extends Vue {
  @VModel({ required: true })
  model!: PatientCreateRequest

  @Prop({ default: '' })
  submitLabel!: string

  @Prop({ default: false })
  loading!: boolean

  @Prop({ default: false })
  readonly!: boolean

  formValido = true
  sexoOptions: EnumOption[] = []
  objetivoOptions: EnumOption[] = []
  nivelAtividadeOptions: EnumOption[] = []

  get rules() {
    return {
      obrigatorio: (v: unknown) => (v !== null && v !== undefined && v !== '') || this.$t('validacao.obrigatorio'),
    }
  }

  async created() {
    const enums = await patientService.enums()
    this.sexoOptions = enums.genders
    this.objetivoOptions = enums.goals
    this.nivelAtividadeOptions = enums.activityLevels
  }

  @Emit('cancel')
  cancelar() {}

  async salvar() {
    const result = await (this.$refs.form as any).validate()
    if (!result.valid) {
      return
    }
    this.emitSubmit()
  }

  @Emit('submit')
  emitSubmit() {}
}
</script>
