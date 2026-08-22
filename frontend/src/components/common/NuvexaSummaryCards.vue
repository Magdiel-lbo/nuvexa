<template>
  <div class="summary-grid">
    <v-card v-for="item in cards" :key="item.label" variant="flat" color="surface-variant" class="summary-card">
      <v-card-text class="d-flex align-center ga-4">
        <v-avatar :color="item.color" variant="tonal" size="44" rounded="lg">
          <v-icon :icon="item.icon" size="22" />
        </v-avatar>
        <div>
          <div class="summary-card__value">{{ item.value }}</div>
          <div class="summary-card__label">{{ item.label }}</div>
        </div>
      </v-card-text>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'

export interface SummaryCardItem {
  label: string
  value: number | string
  icon: string
  color: string
}

@Component({ name: 'NuvexaSummaryCards' })
export default class NuvexaSummaryCards extends Vue {
  @Prop({ required: true })
  cards!: SummaryCardItem[]
}
</script>

<style scoped lang="scss">
.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;

  @media (max-width: 960px) {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  @media (max-width: 600px) {
    grid-template-columns: 1fr;
  }
}

.summary-card {
  border-radius: 12px;
}

.summary-card__value {
  font-size: 1.5rem;
  font-weight: 700;
  line-height: 1.2;
}

.summary-card__label {
  font-size: 0.85rem;
  color: rgb(var(--v-theme-on-surface-variant));
}
</style>
