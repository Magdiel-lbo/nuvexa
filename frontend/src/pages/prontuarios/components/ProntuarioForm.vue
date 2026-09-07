<template>
  <v-form ref="form" v-model="formValido" :disabled="readonly" @submit.prevent="salvar">
    <v-row>
      <v-col v-if="mostrarSelecaoPaciente" cols="6">
        <v-autocomplete
          v-model="model.pacienteId"
          :items="pacienteOptions"
          item-title="label"
          item-value="value"
          :label="$t('prontuario.paciente')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="6">
        <v-select
          v-model="model.autorId"
          :items="profissionalOptions"
          item-title="label"
          item-value="value"
          :label="$t('prontuario.autor')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="6" md="6">
        <v-select
          v-model="model.secao"
          :items="opcoesSecao"
          item-title="rotulo"
          item-value="valor"
          :label="$t('prontuario.secao')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-select
          v-model="model.status"
          :items="opcoesStatus"
          item-title="rotulo"
          item-value="valor"
          :label="$t('prontuario.status')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12">
        <v-textarea v-model="model.conteudo" :label="$t('prontuario.conteudo')" rows="5" />
      </v-col>

      <v-col cols="12">
        <v-checkbox v-model="model.comAnexo" :label="$t('prontuario.comAnexo')" hide-details />
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
import prontuarioService from '../../../service/prontuario-service'
import type { EnumOpcao } from '../../../util/enum-rotulos'
import type { SecaoProntuario, StatusProntuario } from '../../../types/prontuario'

export interface ProntuarioFormModel {
  pacienteId: number | null
  autorId: number | null
  secao: SecaoProntuario
  status: StatusProntuario
  conteudo: string | null
  comAnexo: boolean
}

@Component({ name: 'ProntuarioForm' })
export default class ProntuarioForm extends Vue {
  @VModel({ required: true })
  model!: ProntuarioFormModel

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
  opcoesSecao: EnumOpcao[] = []
  opcoesStatus: EnumOpcao[] = []

  get rules() {
    return {
      obrigatorio: (v: unknown) => (v !== null && v !== undefined && v !== '') || this.$t('validacao.obrigatorio'),
    }
  }

  async created() {
    const enums = await prontuarioService.enums()
    this.opcoesSecao = enums.secoes
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
