<template>
  <NuvexaFiltersCard :show-clear="hasActiveFilters" :clear-label="$t('dashboardPacientes.filtros.limpar') as string" @clear="$emit('clear')">
    <v-text-field
      :model-value="search"
      @update:model-value="$emit('update:search', $event)"
      :placeholder="$t('dashboardConsultas.filtros.buscarPlaceholder')"
      prepend-inner-icon="mdi-magnify"
      density="compact"
      hide-details
      clearable
      single-line
      class="nuvexa-field filters-card__search"
    />

    <NuvexaSelect
      :model-value="status"
      @update:model-value="$emit('update:status', $event)"
      :items="statusOptions"
      :label="$t('dashboardConsultas.filtros.status') as string"
    />

    <NuvexaSelect
      :model-value="type"
      @update:model-value="$emit('update:type', $event)"
      :items="typeOptions"
      :label="$t('dashboardConsultas.filtros.tipo') as string"
    />
  </NuvexaFiltersCard>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import NuvexaSelect from '../../../components/common/NuvexaSelect.vue'
import NuvexaFiltersCard from '../../../components/common/NuvexaFiltersCard.vue'
import type { ConsultaStatus, ConsultaTipo } from '../../../types/consulta'

@Component({
  name: 'ConsultaFilters',
  components: { NuvexaSelect, NuvexaFiltersCard },
  emits: ['update:search', 'update:status', 'update:type', 'clear'],
})
export default class ConsultaFilters extends Vue {
  @Prop({ default: '' })
  search!: string

  @Prop({ default: null })
  status!: ConsultaStatus | null

  @Prop({ default: null })
  type!: ConsultaTipo | null

  get statusOptions() {
    const statuses: ConsultaStatus[] = ['AGENDADA', 'CONFIRMADA', 'REALIZADA', 'CANCELADA', 'FALTOU']
    return [
      { value: null, label: this.$t('dashboardPacientes.filtros.todos') },
      ...statuses.map((value) => ({ value, label: this.$t(`consulta.status.${value}`) })),
    ]
  }

  get typeOptions() {
    const types: ConsultaTipo[] = ['PRIMEIRA_CONSULTA', 'RETORNO', 'AVALIACAO']
    return [
      { value: null, label: this.$t('dashboardPacientes.filtros.todos') },
      ...types.map((value) => ({ value, label: this.$t(`consulta.tipo.${value}`) })),
    ]
  }

  get hasActiveFilters(): boolean {
    return !!this.search || !!this.status || !!this.type
  }
}
</script>

<style scoped lang="scss">
@use '../../../components/common/nuvexa-field.scss';

.filters-card__search {
  flex: 1 1 240px;
  max-width: 320px;
}
</style>
