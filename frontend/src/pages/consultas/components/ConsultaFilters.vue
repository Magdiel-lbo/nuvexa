<template>
  <NuvexaFiltersCard :show-clear="hasActiveFilters" :clear-label="$t('dashboardPacientes.filtros.limpar') as string" @clear="$emit('clear')">
    <v-row class="filters-card__row">
      <v-col cols="12" md="6">
        <v-text-field
          :model-value="search"
          @update:model-value="$emit('update:search', $event)"
          :placeholder="$t('dashboardConsultas.filtros.buscarPlaceholder')"
          prepend-inner-icon="mdi-magnify"
          density="compact"
          hide-details
          clearable
          single-line
          class="nuvexa-field"
        />
      </v-col>

      <v-col cols="12" md="6">
        <NuvexaSelect
          :model-value="status"
          @update:model-value="$emit('update:status', $event)"
          :items="statusOptions"
          :label="$t('dashboardConsultas.filtros.status') as string"
          multiple
          chips
          closable-chips
          clearable
        />
      </v-col>

      <v-col cols="12" md="6">
        <NuvexaSelect
          :model-value="type"
          @update:model-value="$emit('update:type', $event)"
          :items="typeOptions"
          :label="$t('dashboardConsultas.filtros.tipo') as string"
          multiple
          chips
          closable-chips
          clearable
        />
      </v-col>
    </v-row>
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

  @Prop({ default: () => [] })
  status!: ConsultaStatus[]

  @Prop({ default: () => [] })
  type!: ConsultaTipo[]

  get statusOptions() {
    const statuses: ConsultaStatus[] = ['AGENDADA', 'CONFIRMADA', 'REALIZADA', 'CANCELADA', 'FALTOU']
    return statuses.map((value) => ({ value, label: this.$t(`consulta.status.${value}`) }))
  }

  get typeOptions() {
    const types: ConsultaTipo[] = ['PRIMEIRA_CONSULTA', 'RETORNO', 'AVALIACAO']
    return types.map((value) => ({ value, label: this.$t(`consulta.tipo.${value}`) }))
  }

  get hasActiveFilters(): boolean {
    return !!this.search || this.status.length > 0 || this.type.length > 0
  }
}
</script>

<style scoped lang="scss">
@use '../../../components/common/nuvexa-field.scss';

.filters-card__row {
  width: 100%;
}

:deep(.nuvexa-field) {
  max-width: none;
}
</style>
