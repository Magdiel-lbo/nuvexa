<template>
  <v-card variant="flat" color="surface-variant" class="filters-card">
    <v-card-text class="d-flex flex-wrap align-end ga-6">
      <slot />
      <v-btn v-if="showClear" variant="text" density="comfortable" @click="$emit('clear')">
        {{ clearLabel }}
      </v-btn>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'

@Component({ name: 'NuvexaFiltersCard', emits: ['clear'] })
export default class NuvexaFiltersCard extends Vue {
  @Prop({ default: false })
  showClear!: boolean

  @Prop({ required: true })
  clearLabel!: string
}
</script>

<style scoped lang="scss">
.filters-card {
  border-radius: 12px;
}
</style>
