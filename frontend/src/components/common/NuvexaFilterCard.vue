<template>
  <v-card variant="flat" color="surface-variant" class="nuvexa-filter-card">
    <v-card-text class="nuvexa-filter-card__content">
      <div class="nuvexa-filter-card__title">
        <v-icon :icon="titleIcon" size="16" color="primary" />
        <span>{{ title }}</span>
      </div>

      <div class="nuvexa-filter-card__row">
        <NuvexaField
          class="nuvexa-filter-card__search"
          :model-value="search"
          @update:model-value="$emit('update:search', $event)"
          @keyup.enter="$emit('submit')"
          @clear="$emit('clear-search')"
          :label="searchLabel"
          :placeholder="searchPlaceholder"
          prepend-icon="mdi-magnify"
          clearable
        />

        <slot />
      </div>

      <div v-if="$slots.chipGroups" class="nuvexa-filter-card__chip-groups">
        <slot name="chipGroups" />
      </div>

      <div class="nuvexa-filter-card__footer">
        <div class="nuvexa-filter-card__footer-actions">
          <NuvexaButton variant="primary" icon="mdi-magnify" @click="$emit('submit')">{{ searchButtonLabel }}</NuvexaButton>
          <NuvexaButton variant="secondary" @click="$emit('clear')">{{ clearLabel }}</NuvexaButton>
        </div>
        <div v-if="$slots.extra" class="nuvexa-filter-card__footer-extra">
          <slot name="extra" />
        </div>
      </div>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import NuvexaField from './NuvexaField.vue'
import NuvexaButton from './NuvexaButton.vue'

@Component({
  name: 'NuvexaFilterCard',
  components: { NuvexaField, NuvexaButton },
  emits: ['update:search', 'clear-search', 'submit', 'clear'],
})
export default class NuvexaFilterCard extends Vue {
  @Prop({ required: true })
  title!: string

  @Prop({ default: 'mdi-tune-variant' })
  titleIcon!: string

  @Prop({ default: '' })
  search!: string

  @Prop({ default: '' })
  searchLabel!: string

  @Prop({ default: null })
  searchPlaceholder!: string | null

  @Prop({ required: true })
  clearLabel!: string

  @Prop({ required: true })
  searchButtonLabel!: string
}
</script>

<style scoped lang="scss">
.nuvexa-filter-card {
  border-radius: 10px;
}

.nuvexa-filter-card__content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.nuvexa-filter-card__title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.845rem;
  font-weight: 600;
}

.nuvexa-filter-card__row {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px 24px;
  align-items: end;

  @media (min-width: 960px) {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

.nuvexa-filter-card__row :deep(.nuvexa-field) {
  max-width: none;
  width: 100%;
  flex: none;
}

.nuvexa-filter-card__search {
  max-width: none;
}

.nuvexa-filter-card__chip-groups {
  border-top: 1px solid rgb(var(--v-theme-outline));
  padding-top: 18px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 22px;
}

.nuvexa-filter-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
}

.nuvexa-filter-card__footer-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
