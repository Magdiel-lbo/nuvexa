<template>
  <NuvexaSelect
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :items="items"
    :label="label"
    :placeholder="placeholder"
    :disabled="disabled"
  />
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import NuvexaSelect from './NuvexaSelect.vue'
import type { NuvexaYesNoValue } from '../../types/nuvexa-filters'

interface YesNoOption {
  value: NuvexaYesNoValue
  label: string
}

@Component({ name: 'NuvexaYesNoFilter', components: { NuvexaSelect }, emits: ['update:modelValue'] })
export default class NuvexaYesNoFilter extends Vue {
  @Prop({ required: true })
  modelValue!: NuvexaYesNoValue

  @Prop({ required: true })
  label!: string

  @Prop({ default: null })
  placeholder!: string | null

  @Prop({ default: true })
  includeAll!: boolean

  @Prop({ default: false })
  disabled!: boolean

  get items(): YesNoOption[] {
    const options: YesNoOption[] = [
      { value: true, label: this.$t('filtroComum.sim') as string },
      { value: false, label: this.$t('filtroComum.nao') as string },
    ]
    if (this.includeAll) {
      options.unshift({ value: null, label: this.$t('filtroComum.todos') as string })
    }
    return options
  }
}
</script>
