<template>
  <svg :width="size" :height="size" viewBox="0 0 64 64" fill="none" aria-hidden="true">
    <defs>
      <mask :id="maskId" maskUnits="userSpaceOnUse" x="0" y="0" width="64" height="64">
        <rect width="64" height="64" fill="white" />
        <line x1="29" y1="31" x2="20" y2="22" stroke="black" stroke-width="2.6" stroke-linecap="round" />
        <line x1="29" y1="31" x2="42" y2="29" stroke="black" stroke-width="2.6" stroke-linecap="round" />
        <line x1="29" y1="31" x2="27" y2="43" stroke="black" stroke-width="2.6" stroke-linecap="round" />
        <circle cx="29" cy="31" r="3.4" fill="black" />
        <circle cx="20" cy="22" r="2.6" fill="black" />
        <circle cx="42" cy="29" r="2.6" fill="black" />
        <circle cx="27" cy="43" r="2.6" fill="black" />
      </mask>
    </defs>
    <g :mask="`url(#${maskId})`">
      <circle cx="26" cy="36" r="16" fill="currentColor" />
      <circle cx="42" cy="30" r="13" fill="currentColor" />
      <circle cx="20" cy="22" r="10" fill="currentColor" />
    </g>
  </svg>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'

let instanceCount = 0

@Component({ name: 'BrandMark' })
export default class BrandMark extends Vue {
  @Prop({ default: 32 })
  size!: number

  maskId = `nuvexa-mark-mask-${instanceCount++}`
}
</script>
