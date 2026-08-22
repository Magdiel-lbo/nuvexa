<template>
  <div class="dashboard-filters-bar">
    <NuvexaPeriodFilter :model-value="periodValue" @update:model-value="filtersStore.setPeriod($event)" />

    <NuvexaSelect
      :model-value="filtersStore.comparison"
      @update:model-value="filtersStore.setComparison($event)"
      :items="comparisonOptions"
      :label="$t('dashboardAnalytics.filtros.comparacao') as string"
    />

    <NuvexaYesNoFilter
      :model-value="filtersStore.onlyActivePatients"
      @update:model-value="filtersStore.setOnlyActivePatients($event)"
      :label="$t('dashboardAnalytics.filtros.somentePacientesAtivos') as string"
    />
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import NuvexaSelect from '../../../components/common/NuvexaSelect.vue'
import NuvexaPeriodFilter from '../../../components/common/NuvexaPeriodFilter.vue'
import NuvexaYesNoFilter from '../../../components/common/NuvexaYesNoFilter.vue'
import { useDashboardFiltersStore } from '../../../store/dashboard-filters.store'
import type { DashboardComparison } from '../../../types/dashboard-analytics'
import type { NuvexaPeriodValue } from '../../../types/nuvexa-filters'

@Component({ name: 'DashboardFiltersBar', components: { NuvexaSelect, NuvexaPeriodFilter, NuvexaYesNoFilter } })
export default class DashboardFiltersBar extends Vue {
  get filtersStore() {
    return useDashboardFiltersStore()
  }

  get periodValue(): NuvexaPeriodValue {
    return {
      preset: this.filtersStore.preset,
      startDate: this.filtersStore.startDate,
      endDate: this.filtersStore.endDate,
    }
  }

  get comparisonOptions(): { value: DashboardComparison; label: string }[] {
    const comparisons: DashboardComparison[] = ['none', 'previous_period', 'previous_year']
    return comparisons.map((value) => ({
      value,
      label: this.$t(`dashboardAnalytics.filtros.comparacaoOpcoes.${value}`) as string,
    }))
  }
}
</script>

<style scoped lang="scss">
.dashboard-filters-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 32px;
  margin-bottom: 24px;
}
</style>
