<template>
  <v-form ref="form" v-model="formValido" :disabled="readonly" @submit.prevent="salvar">
    <v-row>
      <v-col cols="6">
        <v-text-field v-model="model.nome" :label="$t('paciente.nome')" :rules="[rules.obrigatorio]" />
      </v-col>

      <v-col cols="12" md="6">
        <v-text-field
          v-model="model.dataNascimento"
          type="date"
          :label="$t('paciente.dataNascimento')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-select
          v-model="model.sexo"
          :items="opcoesSexo"
          item-title="rotulo"
          item-value="valor"
          :label="$t('paciente.sexo')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-text-field
          v-model.number="model.altura"
          type="number"
          step="0.01"
          :label="$t('paciente.altura')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-text-field
          v-model.number="model.peso"
          type="number"
          step="0.01"
          :label="$t('paciente.peso')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-select
          v-model="model.objetivo"
          :items="opcoesObjetivo"
          item-title="rotulo"
          item-value="valor"
          :label="$t('paciente.objetivo')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-select
          v-model="model.nivelAtividade"
          :items="opcoesNivelAtividade"
          item-title="rotulo"
          item-value="valor"
          :label="$t('paciente.nivelAtividade')"
          :rules="[rules.obrigatorio]"
        />
      </v-col>

      <v-col cols="12" md="6">
        <v-text-field
          v-model.number="model.caloriasDiariasManuais"
          type="number"
          step="0.01"
          :label="$t('paciente.gastoCaloricoManual')"
        />
      </v-col>

      <v-col cols="6">
        <v-textarea v-model="model.observacoes" :label="$t('paciente.observacoes')" rows="3" />
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
import pacienteService from '../../../service/paciente-service'
import perfilNutricionalService from '../../../nutricao/services/perfil-nutricional-service'
import type { EnumOpcao } from '../../../util/enum-rotulos'
import type { Sexo } from '../../../types/paciente'
import type { NivelAtividade, Objetivo } from '../../../nutricao/types/perfil-nutricional'

export interface PacienteFormModel {
  nome: string
  dataNascimento: string
  sexo: Sexo
  altura: number
  peso: number
  objetivo: Objetivo
  nivelAtividade: NivelAtividade
  caloriasDiariasManuais: number | null
  observacoes: string | null
}

@Component({ name: 'PacienteForm' })
export default class PacienteForm extends Vue {
  @VModel({ required: true })
  model!: PacienteFormModel

  @Prop({ default: '' })
  submitLabel!: string

  @Prop({ default: false })
  loading!: boolean

  @Prop({ default: false })
  readonly!: boolean

  formValido = true
  opcoesSexo: EnumOpcao[] = []
  opcoesObjetivo: EnumOpcao[] = []
  opcoesNivelAtividade: EnumOpcao[] = []

  get rules() {
    return {
      obrigatorio: (v: unknown) => (v !== null && v !== undefined && v !== '') || this.$t('validacao.obrigatorio'),
    }
  }

  async created() {
    const [pacienteEnums, perfilEnums] = await Promise.all([pacienteService.enums(), perfilNutricionalService.enums()])
    this.opcoesSexo = pacienteEnums.sexos
    this.opcoesObjetivo = perfilEnums.objetivos
    this.opcoesNivelAtividade = perfilEnums.niveisAtividade
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
