<template>
  <NuvexaSelect
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :items="items"
    :label="label ?? ($t('dashboardPacientes.filtros.status') as string)"
    :placeholder="placeholder"
    :multiple="multiple"
    :chips="multiple"
    :closable-chips="multiple"
    :clearable="multiple"
    :disabled="disabled"
  />
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import NuvexaSelect from './NuvexaSelect.vue'
import type { NuvexaStatusMultiValue, NuvexaStatusValue } from '../../types/nuvexa-filters'

interface StatusOption {
  value: NuvexaStatusValue
  label: string
}

@Component({ name: 'NuvexaStatusFilter', components: { NuvexaSelect }, emits: ['update:modelValue'] })
export default class NuvexaStatusFilter extends Vue {
  @Prop({ required: true })
  modelValue!: NuvexaStatusValue | NuvexaStatusMultiValue

  @Prop({ default: null })
  label!: string | null

  @Prop({ default: null })
  placeholder!: string | null

  @Prop({ default: true })
  includeAll!: boolean

  @Prop({ type: Boolean, default: false })
  multiple!: boolean

  @Prop({ default: false })
  disabled!: boolean

  get items(): StatusOption[] {
    const options: StatusOption[] = [
      { value: 'ACTIVE', label: this.$t('paciente.status.ACTIVE') as string },
      { value: 'INACTIVE', label: this.$t('paciente.status.INACTIVE') as string },
    ]
    if (this.includeAll && !this.multiple) {
      options.unshift({ value: null, label: this.$t('filtroComum.todos') as string })
    }
    return options
  }
}
</script>
