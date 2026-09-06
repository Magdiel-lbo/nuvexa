<template>
  <svg :width="size" :height="size" viewBox="0 0 64 64" fill="none" aria-hidden="true">
    <g stroke="currentColor" stroke-width="3" stroke-linecap="round">
      <line x1="32" y1="32" x2="32" y2="13" />
      <line x1="32" y1="32" x2="48.5" y2="41.5" />
      <line x1="32" y1="32" x2="15.5" y2="41.5" />
    </g>
    <circle cx="32" cy="32" r="7.5" fill="currentColor" />
    <circle cx="32" cy="13" r="5.5" fill="currentColor" />
    <circle cx="48.5" cy="41.5" r="5.5" fill="currentColor" />
    <circle cx="15.5" cy="41.5" r="5.5" fill="currentColor" />
    <circle cx="32" cy="32" r="26" stroke="currentColor" stroke-width="2" opacity="0.28" />
  </svg>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'

@Component({ name: 'BrandMark' })
export default class BrandMark extends Vue {
  @Prop({ default: 32 })
  size!: number
}
</script>
