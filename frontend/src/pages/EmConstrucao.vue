<template>
  <div class="em-construcao">
    <v-avatar size="72" color="primary" variant="tonal" class="mb-4">
      <v-icon icon="mdi-hammer-wrench" size="34" />
    </v-avatar>
    <h1 class="em-construcao__title">{{ $route.meta.title }}</h1>
    <p class="em-construcao__description">{{ $t('dashboard.emConstrucao') }}</p>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'

@Component({ name: 'EmConstrucao' })
export default class EmConstrucao extends Vue {}
</script>

<style scoped lang="scss">
.em-construcao {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 96px 24px;
}

.em-construcao__title {
  font-size: 1.35rem;
  font-weight: 700;
  margin-bottom: 8px;
}

.em-construcao__description {
  color: rgb(var(--v-theme-on-surface-variant));
}
</style>
