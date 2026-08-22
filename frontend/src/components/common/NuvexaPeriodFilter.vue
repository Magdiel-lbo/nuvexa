<template>
  <v-row class="nuvexa-period-filter">
    <v-col cols="12" md="6">
      <NuvexaSelect
        :model-value="modelValue.preset"
        @update:model-value="onPresetChange($event)"
        :items="presetOptions"
        :label="label ?? ($t('dashboardAnalytics.filtros.periodo') as string)"
        :disabled="disabled"
      />
    </v-col>

    <v-col cols="3">
      <NuvexaDateField
        :model-value="isCustom ? modelValue.startDate : todayIso"
        @update:model-value="onStartDateChange($event)"
        :label="$t('filtroComum.periodo.dataInicio') as string"
        :max="modelValue.endDate"
        :disabled="disabled || !isCustom"
      />
    </v-col>

    <v-col cols="3">
      <NuvexaDateField
        :model-value="isCustom ? modelValue.endDate : todayIso"
        @update:model-value="onEndDateChange($event)"
        :label="$t('filtroComum.periodo.dataFim') as string"
        :min="modelValue.startDate"
        :max="todayIso"
        :disabled="disabled || !isCustom"
      />
    </v-col>

    <v-col v-if="errorMessage" cols="12" role="alert" class="nuvexa-period-filter__error">{{ errorMessage }}</v-col>
  </v-row>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import NuvexaSelect from './NuvexaSelect.vue'
import NuvexaDateField from './NuvexaDateField.vue'
import { MAX_CUSTOM_RANGE_DAYS, daysBetweenInclusive, presetRange, subtractDays, todayIsoDate } from '../../util/period-range'
import type { NuvexaPeriodPreset, NuvexaPeriodValue } from '../../types/nuvexa-filters'

interface PresetOption {
  value: NuvexaPeriodPreset
  label: string
}

const PRESETS: NuvexaPeriodPreset[] = ['today', 'yesterday', 'last_3', 'last_7', 'last_30', 'last_90', 'custom']

@Component({ name: 'NuvexaPeriodFilter', components: { NuvexaSelect, NuvexaDateField }, emits: ['update:modelValue'] })
export default class NuvexaPeriodFilter extends Vue {
  @Prop({ required: true })
  modelValue!: NuvexaPeriodValue

  @Prop({ default: null })
  label!: string | null

  @Prop({ default: false })
  disabled!: boolean

  errorMessage = ''

  get todayIso(): string {
    return todayIsoDate()
  }

  get isCustom(): boolean {
    return this.modelValue.preset === 'custom'
  }

  get presetOptions(): PresetOption[] {
    return PRESETS.map((value) => ({ value, label: this.$t(`filtroComum.periodo.${value}`) as string }))
  }

  onPresetChange(preset: NuvexaPeriodPreset) {
    this.errorMessage = ''

    if (preset === 'custom') {
      this.$emit('update:modelValue', {
        preset,
        startDate: this.modelValue.startDate,
        endDate: this.modelValue.endDate,
      })
      return
    }

    this.$emit('update:modelValue', { preset, ...presetRange(preset) })
  }

  onStartDateChange(startDate: string) {
    this.applyCustomRange(startDate, this.modelValue.endDate)
  }

  onEndDateChange(endDate: string) {
    this.applyCustomRange(this.modelValue.startDate, endDate)
  }

  applyCustomRange(startDate: string, endDate: string) {
    this.errorMessage = ''

    if (endDate > this.todayIso) {
      endDate = this.todayIso
      this.errorMessage = this.$t('filtroComum.periodo.erroDataFimFutura') as string
    }

    if (startDate > endDate) {
      startDate = endDate
      this.errorMessage = this.$t('filtroComum.periodo.erroDataInicioMaiorFim') as string
    } else if (daysBetweenInclusive(startDate, endDate) > MAX_CUSTOM_RANGE_DAYS) {
      startDate = subtractDays(endDate, MAX_CUSTOM_RANGE_DAYS - 1)
      this.errorMessage = this.$t('filtroComum.periodo.erroIntervaloMaximo') as string
    }

    this.$emit('update:modelValue', { preset: 'custom', startDate, endDate })
  }
}
</script>

<style scoped lang="scss">
.nuvexa-period-filter {
  flex-basis: 100%;
  align-items: flex-end;
}

.nuvexa-period-filter__error {
  font-size: 0.78rem;
  color: rgb(var(--v-theme-error));
}
</style>
