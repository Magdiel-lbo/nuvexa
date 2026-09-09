<template>
  <v-select
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
import consultaService from '../../service/consulta-service'
import { useAppStore } from '../../store/app.store'
import { extrairMensagemErro } from '../../util/api-util'

/**
 * Seletor de profissional vinculado à organização atual — mesma lista usada por Consulta
 * (profissional), Avaliação (avaliador) e Prontuário (autor); só o rótulo muda por tela.
 */
@Component({ name: 'NuvexaProfissionalSelect', emits: ['update:modelValue'] })
export default class NuvexaProfissionalSelect extends Vue {
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
      const profissionais = await consultaService.listarProfissionais()
      this.opcoes = profissionais.map((profissional) => ({ value: profissional.id, label: profissional.nome }))
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarProfissionais') as string), erro: true })
    }
  }
}
</script>
