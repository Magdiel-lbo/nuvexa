<template>
  <NuvexaSelect
    :model-value="modelValue.preset"
    @update:model-value="onPresetChange($event)"
    :items="presetOptions"
    :label="label ?? ($t('dashboardAnalytics.filtros.periodo') as string)"
    :disabled="disabled"
  />
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import NuvexaSelect from './NuvexaSelect.vue'
import { presetRange } from '../../util/period-range'
import type { NuvexaPeriodPreset, NuvexaPeriodValue } from '../../types/nuvexa-filters'

interface PresetOption {
  value: NuvexaPeriodPreset
  label: string
}

const PRESETS: NuvexaPeriodPreset[] = ['last_3', 'last_7', 'last_30', 'last_90', 'last_180']

@Component({ name: 'NuvexaPeriodFilter', components: { NuvexaSelect }, emits: ['update:modelValue'] })
export default class NuvexaPeriodFilter extends Vue {
  @Prop({ required: true })
  modelValue!: NuvexaPeriodValue

  @Prop({ default: null })
  label!: string | null

  @Prop({ default: false })
  disabled!: boolean

  get presetOptions(): PresetOption[] {
    return PRESETS.map((value) => ({ value, label: this.$t(`filtroComum.periodo.${value}`) as string }))
  }

  onPresetChange(preset: NuvexaPeriodPreset) {
    this.$emit('update:modelValue', { preset, ...presetRange(preset) })
  }
}
</script>
