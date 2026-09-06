<template>
  <NuvexaSelect :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)" :items="opcoes" :label="label" />
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import NuvexaSelect from './NuvexaSelect.vue'
import { PERIODO_VALUES, periodoLabelKey } from '../../util/periodo'

@Component({ name: 'NuvexaPeriodoSelect', components: { NuvexaSelect }, emits: ['update:modelValue'] })
export default class NuvexaPeriodoSelect extends Vue {
  @Prop({ type: String, required: true })
  modelValue!: string

  @Prop({ default: '' })
  label!: string

  get opcoes() {
    return PERIODO_VALUES.map((value) => ({ value, label: this.$t(periodoLabelKey(value)) as string }))
  }
}
</script>
