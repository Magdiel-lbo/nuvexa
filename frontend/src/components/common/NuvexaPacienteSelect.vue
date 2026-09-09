<template>
  <v-autocomplete
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :items="opcoes"
    item-title="label"
    item-value="value"
    :label="label"
    :rules="[rules.obrigatorio]"
  />
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import pacienteService from '../../service/paciente-service'
import { useAppStore } from '../../store/app.store'
import { extrairMensagemErro } from '../../util/api-util'

/**
 * Seletor de paciente da organização atual — mesma lista usada por Consulta, Avaliação,
 * Prontuário e Plano Alimentar na criação; só o rótulo muda por tela.
 */
@Component({ name: 'NuvexaPacienteSelect', emits: ['update:modelValue'] })
export default class NuvexaPacienteSelect extends Vue {
  @Prop({ required: true })
  modelValue!: number | null

  @Prop({ required: true })
  label!: string

  opcoes: { value: number; label: string }[] = []

  get appStore() {
    return useAppStore()
  }

  get rules() {
    return {
      obrigatorio: (v: unknown) => (v !== null && v !== undefined && v !== '') || this.$t('validacao.obrigatorio'),
    }
  }

  async created() {
    try {
      const pacientes = await pacienteService.listar()
      this.opcoes = pacientes.map((paciente) => ({ value: paciente.id, label: paciente.nome }))
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarPacientes') as string), erro: true })
    }
  }
}
</script>
