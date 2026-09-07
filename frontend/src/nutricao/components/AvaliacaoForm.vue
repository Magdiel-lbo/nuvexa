<template>
  <v-form ref="form" v-model="formValido" :disabled="readonly" @submit.prevent="salvar">
    <v-row>
      <v-col v-if="mostrarSelecaoPaciente" cols="6">
        <v-autocomplete
          v-model="model.pacienteId"
          :items="pacienteOptions"
          item-title="label"
          item-value="value"
          :label="$t('avaliacao.paciente')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="6">
        <v-select
          v-model="model.avaliadorId"
          :items="profissionalOptions"
          item-title="label"
          item-value="value"
          :label="$t('avaliacao.avaliador')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-text-field v-model="model.data" type="date" :label="$t('avaliacao.data')" :rules="[rules.obrigatorio]" />
      </v-col>

      <v-col cols="12" md="6">
        <v-select
          v-model="model.tipo"
          :items="opcoesTipo"
          item-title="rotulo"
          item-value="valor"
          :label="$t('avaliacao.tipo')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-select
          v-model="model.status"
          :items="opcoesStatus"
          item-title="rotulo"
          item-value="valor"
          :label="$t('avaliacao.status')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>


      <v-col cols="12" md="6">
        <v-text-field v-model.number="model.peso" type="number" step="0.1" :label="$t('avaliacao.peso')" />
      </v-col>

      <v-col cols="12" md="6">
        <v-text-field v-model.number="model.percentualGordura" type="number" step="0.1" :label="$t('avaliacao.percentualGordura')" />
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
import avaliacaoService from '../services/avaliacao-service'
import type { EnumOpcao } from '../../util/enum-rotulos'
import type { StatusAvaliacao, TipoAvaliacao } from '../types/avaliacao'

export interface AvaliacaoFormModel {
  pacienteId: number | null
  avaliadorId: number | null
  data: string
  tipo: TipoAvaliacao
  status: StatusAvaliacao
  peso: number | null
  percentualGordura: number | null
}

@Component({ name: 'AvaliacaoForm' })
export default class AvaliacaoForm extends Vue {
  @VModel({ required: true })
  model!: AvaliacaoFormModel

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

  @Prop({ default: () => [] })
  profissionalOptions!: { value: number; label: string }[]

  formValido = true
  opcoesTipo: EnumOpcao[] = []
  opcoesStatus: EnumOpcao[] = []

  get rules() {
    return {
      obrigatorio: (v: unknown) => (v !== null && v !== undefined && v !== '') || this.$t('validacao.obrigatorio'),
    }
  }

  async created() {
    const enums = await avaliacaoService.enums()
    this.opcoesTipo = enums.tipos
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
