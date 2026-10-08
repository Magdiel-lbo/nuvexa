<template>
  <v-form ref="form" v-model="formValido" :disabled="readonly" @submit.prevent="salvar">
    <v-row>
      <v-col v-if="mostrarSelecaoPaciente" cols="6">
        <NuvexaPacienteSelect v-model="model.pacienteId" :label="$t('dashboardConsultas.tabela.paciente') as string" />
      </v-col>

      <v-col cols="6">
        <NuvexaProfissionalSelect v-model="model.profissionalId" :label="$t('consulta.profissional') as string" />
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
        <v-textarea v-model="model.motivo" :label="$t('consulta.motivo')" rows="2" />
      </v-col>

      <v-col v-if="precisaMotivoTransicao" cols="12">
        <v-textarea
          v-model="model.motivoTransicao"
          :label="$t('consulta.motivoTransicao')"
          rows="2"
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
import NuvexaProfissionalSelect from '../../../components/common/NuvexaProfissionalSelect.vue'
import NuvexaPacienteSelect from '../../../components/common/NuvexaPacienteSelect.vue'
import type { ConsultaStatus, ConsultaTipo } from '../../../types/consulta'

export interface ConsultaFormModel {
  pacienteId: number | null
  profissionalId: number | null
  dataHora: string
  duracaoMinutos: number
  tipo: ConsultaTipo
  status: ConsultaStatus
  observacoes: string | null
  motivo: string | null
  motivoTransicao: string | null
}

@Component({ name: 'ConsultaForm', components: { NuvexaProfissionalSelect, NuvexaPacienteSelect } })
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

  // Status da consulta antes desta edição — null na criação. Usado só para decidir se a
  // mudança de status atual é de fato uma transição (edição) ou o status inicial (criação),
  // já que o backend exige motivoTransicao nos dois casos quando o valor é CANCELADA/FALTOU.
  @Prop({ default: null })
  statusOriginal!: ConsultaStatus | null

  formValido = true

  get precisaMotivoTransicao(): boolean {
    const exigeParaEsteStatus = this.model.status === 'CANCELADA' || this.model.status === 'FALTOU'
    if (!exigeParaEsteStatus) {
      return false
    }
    return this.statusOriginal === null || this.statusOriginal !== this.model.status
  }

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
