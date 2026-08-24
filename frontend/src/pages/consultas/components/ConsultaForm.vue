<template>
  <v-form ref="form" v-model="formValido" :disabled="readonly" @submit.prevent="salvar">
    <v-row>
      <v-col v-if="mostrarSelecaoPaciente" cols="12">
        <v-select
          v-model="model.pacienteId"
          :items="pacienteOptions"
          item-title="label"
          item-value="value"
          :label="$t('dashboardConsultas.tabela.paciente')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-text-field
          v-model="model.dataHora"
          type="datetime-local"
          :label="$t('consulta.dataHora')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-text-field
          v-model.number="model.duracaoMinutos"
          type="number"
          :label="$t('consulta.duracao')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-select
          v-model="model.tipo"
          :items="tipoOptions"
          item-title="label"
          item-value="value"
          :label="$t('dashboardConsultas.filtros.tipo')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-select
          v-model="model.status"
          :items="statusOptions"
          item-title="label"
          item-value="value"
          :label="$t('dashboardConsultas.filtros.status')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12">
        <v-textarea v-model="model.observacoes" :label="$t('consulta.observacoes')" rows="3" />
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
import type { ConsultaStatus, ConsultaTipo } from '../../../types/consulta'

export interface ConsultaFormModel {
  pacienteId: number | null
  dataHora: string
  duracaoMinutos: number
  tipo: ConsultaTipo
  status: ConsultaStatus
  observacoes: string | null
}

@Component({ name: 'ConsultaForm' })
export default class ConsultaForm extends Vue {
  @VModel({ required: true })
  model!: ConsultaFormModel

  @Prop({ default: '' })
  submitLabel!: string

  @Prop({ default: false })
  loading!: boolean

  @Prop({ default: false })
  readonly!: boolean

  @Prop({ default: false })
  mostrarSelecaoPaciente!: boolean

  @Prop({ default: () => [] })
  pacienteOptions!: { value: number; label: string }[]

  formValido = true

  get rules() {
    return {
      obrigatorio: (v: unknown) => (v !== null && v !== undefined && v !== '') || this.$t('validacao.obrigatorio'),
    }
  }

  get tipoOptions() {
    return ['PRIMEIRA_CONSULTA', 'RETORNO', 'AVALIACAO'].map((value) => ({
      value,
      label: this.$t(`consulta.tipo.${value}`),
    }))
  }

  get statusOptions() {
    return ['AGENDADA', 'CONFIRMADA', 'REALIZADA', 'CANCELADA', 'FALTOU'].map((value) => ({
      value,
      label: this.$t(`consulta.status.${value}`),
    }))
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
