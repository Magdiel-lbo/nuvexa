<template>
  <div class="nuvexa-filter-chips">
    <div class="nuvexa-filter-chips__active">
      <template v-if="chips.length > 0">
        <div v-for="chip in chips" :key="`${chip.key}:${chip.rawValue}`" class="nuvexa-filter-chips__chip">
          <span class="nuvexa-filter-chips__chip-label">{{ chip.label }}</span>
          <span class="nuvexa-filter-chips__chip-value">{{ chip.value }}</span>
          <button type="button" class="nuvexa-filter-chips__chip-remove" :aria-label="removeLabel" @click="$emit('remove', chip)">
            <v-icon icon="mdi-close" size="10" />
          </button>
        </div>
        <button type="button" class="nuvexa-filter-chips__clear-all" @click="$emit('clear-all')">{{ clearAllLabel }}</button>
      </template>
    </div>

    <div class="nuvexa-filter-chips__trailing">
      <span class="nuvexa-filter-chips__result">{{ resultLabel }}</span>
      <div v-if="sortOptions.length > 0" class="nuvexa-filter-chips__sort">
        <button
          v-for="option in sortOptions"
          :key="option.value"
          type="button"
          class="nuvexa-filter-chips__sort-option"
          :class="{ 'nuvexa-filter-chips__sort-option--active': option.value === sortBy }"
          @click="$emit('update:sortBy', option.value)"
        >
          {{ option.label }}
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import type { ActiveChip } from '../../composables/useFilters'

export interface SortOption {
  value: string
  label: string
}

@Component({ name: 'NuvexaFilterChips', emits: ['remove', 'clear-all', 'update:sortBy'] })
export default class NuvexaFilterChips extends Vue {
  @Prop({ default: () => [] })
  chips!: ActiveChip[]

  @Prop({ default: '' })
  clearAllLabel!: string

  @Prop({ default: '' })
  removeLabel!: string

  @Prop({ default: '' })
  resultLabel!: string

  @Prop({ default: () => [] })
  sortOptions!: SortOption[]

  @Prop({ default: '' })
  sortBy!: string
}
</script>

<style scoped lang="scss">
.nuvexa-filter-chips {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding-top: 14px;
  border-top: 1px solid rgb(var(--v-theme-outline));
}

.nuvexa-filter-chips__active {
  display: flex;
  align-items: center;
  gap: 9px;
  flex-wrap: wrap;
  min-height: 30px;
}

.nuvexa-filter-chips__chip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  height: 29px;
  padding: 0 5px 0 11px;
  background: rgba(var(--v-theme-primary), 0.09);
  border: 1px solid rgba(var(--v-theme-primary), 0.24);
  border-radius: 999px;
}

.nuvexa-filter-chips__chip-label {
  font-size: 0.75rem;
  color: rgb(var(--v-theme-on-surface-variant));
}

.nuvexa-filter-chips__chip-value {
  font-size: 0.75rem;
  font-weight: 600;
  color: rgb(var(--v-theme-primary));
}

.nuvexa-filter-chips__chip-remove {
  width: 19px;
  height: 19px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: rgb(var(--v-theme-primary));
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;

  &:hover {
    background: rgba(var(--v-theme-primary), 0.2);
  }
}

.nuvexa-filter-chips__clear-all {
  font-family: inherit;
  font-size: 0.78rem;
  font-weight: 600;
  color: rgb(var(--v-theme-error));
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0 6px;
  height: 29px;
}

.nuvexa-filter-chips__trailing {
  display: flex;
  align-items: center;
  gap: 14px;
}

.nuvexa-filter-chips__result {
  font-size: 0.78rem;
  color: rgb(var(--v-theme-on-surface-variant));
  white-space: nowrap;
}

.nuvexa-filter-chips__sort {
  display: flex;
  gap: 18px;
}

.nuvexa-filter-chips__sort-option {
  font-family: inherit;
  font-size: 0.78rem;
  font-weight: 500;
  color: rgb(var(--v-theme-on-surface-variant));
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  white-space: nowrap;
  padding-bottom: 4px;
}

.nuvexa-filter-chips__sort-option--active {
  font-weight: 600;
  color: rgb(var(--v-theme-primary));
  border-bottom-color: rgb(var(--v-theme-primary));
}
</style>
