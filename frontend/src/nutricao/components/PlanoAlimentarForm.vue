<template>
  <v-form ref="form" v-model="formValido" :disabled="readonly" @submit.prevent="salvar">
    <v-row>
      <v-col v-if="mostrarSelecaoPaciente" cols="6">
        <NuvexaPacienteSelect v-model="model.pacienteId" :label="$t('planoAlimentar.paciente') as string" />
      </v-col>

      <v-col cols="6">
        <NuvexaProfissionalSelect v-model="model.autorId" :label="$t('planoAlimentar.autor') as string" />
      </v-col>

      <v-col cols="12" md="6">
        <v-text-field v-model="model.nome" :label="$t('planoAlimentar.nome')" :rules="[rules.obrigatorio]" />
      </v-col>

      <v-col cols="12" md="6">
        <v-text-field v-model="model.dataInicio" type="date" :label="$t('planoAlimentar.dataInicio')" :rules="[rules.obrigatorio]" />
      </v-col>

      <v-col cols="12" md="6">
        <v-text-field v-model.number="model.calorias" type="number" :label="$t('planoAlimentar.calorias')" :rules="[rules.obrigatorio]" />
      </v-col>

      <v-col cols="12" md="6">
        <v-text-field
          v-model.number="model.refeicoesPorDia"
          type="number"
          :label="$t('planoAlimentar.refeicoesPorDia')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-select
          v-model="model.status"
          :items="opcoesStatus"
          item-title="rotulo"
          item-value="valor"
          :label="$t('planoAlimentar.status')"
          :rules="[rules.obrigatorio]"
        />
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
import planoAlimentarService from '../services/plano-alimentar-service'
import NuvexaProfissionalSelect from '../../components/common/NuvexaProfissionalSelect.vue'
import NuvexaPacienteSelect from '../../components/common/NuvexaPacienteSelect.vue'
import type { EnumOpcao } from '../../util/enum-rotulos'
import type { StatusPlanoAlimentar } from '../types/plano-alimentar'

export interface PlanoAlimentarFormModel {
  pacienteId: number | null
  autorId: number | null
  nome: string
  dataInicio: string
  calorias: number | null
  refeicoesPorDia: number | null
  status: StatusPlanoAlimentar
}

@Component({ name: 'PlanoAlimentarForm', components: { NuvexaProfissionalSelect, NuvexaPacienteSelect } })
export default class PlanoAlimentarForm extends Vue {
  @VModel({ required: true })
  model!: PlanoAlimentarFormModel

  @Prop({ default: '' })
  submitLabel!: string

  @Prop({ default: false })
  loading!: boolean

  @Prop({ default: false })
  readonly!: boolean

  @Prop({ default: false })
  mostrarSelecaoPaciente!: boolean

  formValido = true
  opcoesStatus: EnumOpcao[] = []

  get rules() {
    return {
      obrigatorio: (v: unknown) => (v !== null && v !== undefined && v !== '') || this.$t('validacao.obrigatorio'),
    }
  }

  async created() {
    const enums = await planoAlimentarService.enums()
    this.opcoesStatus = enums.status
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
