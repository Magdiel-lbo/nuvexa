<template>
  <div class="dashboard-filters-bar">
    <NuvexaPeriodFilter :model-value="periodValue" @update:model-value="filtersStore.setPeriod($event)" />

    <NuvexaStatusFilter
      :model-value="filtersStore.patientStatuses"
      @update:model-value="filtersStore.setPatientStatuses($event)"
      multiple
    />
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import NuvexaPeriodFilter from '../../../components/common/NuvexaPeriodFilter.vue'
import NuvexaStatusFilter from '../../../components/common/NuvexaStatusFilter.vue'
import { useDashboardFiltersStore } from '../../../store/dashboard-filters.store'
import type { NuvexaPeriodValue } from '../../../types/nuvexa-filters'

@Component({ name: 'DashboardFiltersBar', components: { NuvexaPeriodFilter, NuvexaStatusFilter } })
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
}
</script>

<style scoped lang="scss">
.dashboard-filters-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 32px;
  margin-bottom: 24px;

  :deep(.nuvexa-field) {
    flex: 1 1 240px;
    max-width: none;
  }
}
</style>
