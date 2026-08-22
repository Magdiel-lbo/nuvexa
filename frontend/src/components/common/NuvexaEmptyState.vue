<template>
  <div class="empty-state">
    <v-avatar size="72" color="primary" variant="tonal" class="mb-4">
      <v-icon :icon="icon" size="36" />
    </v-avatar>

    <h3 class="empty-state__title">{{ title }}</h3>
    <p class="empty-state__description">{{ description }}</p>

    <div class="d-flex ga-2">
      <v-btn v-if="actionLabel" color="primary" prepend-icon="mdi-plus" @click="$emit('action')">{{ actionLabel }}</v-btn>
      <v-btn v-if="secondaryActionLabel" variant="text" @click="$emit('secondary-action')">{{ secondaryActionLabel }}</v-btn>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'

@Component({ name: 'NuvexaEmptyState', emits: ['action', 'secondary-action'] })
export default class NuvexaEmptyState extends Vue {
  @Prop({ required: true })
  icon!: string

  @Prop({ required: true })
  title!: string

  @Prop({ required: true })
  description!: string

  @Prop({ default: null })
  actionLabel!: string | null

  @Prop({ default: null })
  secondaryActionLabel!: string | null
}
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 64px 24px;
}

.empty-state__title {
  font-size: 1.15rem;
  font-weight: 600;
  margin-bottom: 8px;
}

.empty-state__description {
  max-width: 380px;
  color: rgb(var(--v-theme-on-surface-variant));
  margin-bottom: 20px;
}
</style>
