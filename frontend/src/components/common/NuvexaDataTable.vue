<template>
  <div class="nuvexa-data-table">
    <v-data-table
      :headers="headers"
      :items="items"
      :item-value="itemKey"
      :loading="loading"
      :page="page"
      @update:page="$emit('update:page', $event)"
      :items-per-page="itemsPerPage"
      hide-default-footer
      class="nuvexa-data-table__grid"
    >
      <template v-for="(_, slot) in $slots" #[slot]="scope">
        <slot :name="slot" v-bind="scope ?? {}" />
      </template>
    </v-data-table>

    <div v-if="items.length > 0" class="nuvexa-data-table__footer">
      <div class="nuvexa-data-table__per-page">
        <span>{{ itemsPerPageLabel }}</span>
        <NuvexaSelect
          :model-value="itemsPerPage"
          @update:model-value="$emit('update:itemsPerPage', Number($event))"
          :items="itemsPerPageOptions"
          item-title="label"
          item-value="value"
        />
      </div>
      <span class="nuvexa-data-table__page-label">{{ pageLabel }}</span>
      <div class="nuvexa-data-table__nav">
        <v-btn icon="mdi-chevron-left" variant="text" density="comfortable" :disabled="page <= 1" @click="$emit('update:page', page - 1)" />
        <v-btn icon="mdi-chevron-right" variant="text" density="comfortable" :disabled="page >= pageCount" @click="$emit('update:page', page + 1)" />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import NuvexaSelect from './NuvexaSelect.vue'

export interface NuvexaTableHeader {
  title: string
  key: string
  align?: 'start' | 'center' | 'end'
  sortable?: boolean
}

@Component({ name: 'NuvexaDataTable', components: { NuvexaSelect }, emits: ['update:page', 'update:itemsPerPage'] })
export default class NuvexaDataTable extends Vue {
  @Prop({ required: true })
  headers!: NuvexaTableHeader[]

  @Prop({ required: true })
  items!: unknown[]

  @Prop({ default: 'id' })
  itemKey!: string

  @Prop({ type: Boolean, default: false })
  loading!: boolean

  @Prop({ required: true })
  page!: number

  @Prop({ required: true })
  itemsPerPage!: number

  @Prop({ default: () => [10, 25, 50] })
  itemsPerPageOptionsRaw!: number[]

  @Prop({ default: '' })
  itemsPerPageLabel!: string

  @Prop({ default: 'de' })
  ofLabel!: string

  @Prop({ required: true })
  totalItems!: number

  get itemsPerPageOptions() {
    return this.itemsPerPageOptionsRaw.map((value) => ({ value, label: String(value) }))
  }

  get pageCount(): number {
    return Math.max(1, Math.ceil(this.totalItems / this.itemsPerPage))
  }

  get pageLabel(): string {
    if (this.totalItems === 0) return '0'
    const start = (this.page - 1) * this.itemsPerPage + 1
    const end = Math.min(this.page * this.itemsPerPage, this.totalItems)
    return `${start}–${end} ${this.ofLabel} ${this.totalItems}`
  }
}
</script>

<style scoped lang="scss">
.nuvexa-data-table {
  display: flex;
  flex-direction: column;
}

.nuvexa-data-table__grid {
  border-radius: 12px;
  background: transparent !important;
}

.nuvexa-data-table__footer {
  padding: 16px 8px 4px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 22px;
  flex-wrap: wrap;
}

.nuvexa-data-table__per-page {
  display: flex;
  align-items: center;
  gap: 12px;

  span {
    font-size: 0.78rem;
    color: rgb(var(--v-theme-on-surface-variant));
    white-space: nowrap;
  }

  :deep(.nuvexa-field) {
    min-width: 70px;
  }
}

.nuvexa-data-table__page-label {
  font-size: 0.78rem;
  color: rgb(var(--v-theme-on-surface-variant));
  white-space: nowrap;
}

.nuvexa-data-table__nav {
  display: flex;
  gap: 2px;
}
</style>
