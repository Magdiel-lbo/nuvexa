<template>
  <v-chip :color="color" :variant="variant" size="small" class="nuvexa-status-chip">{{ label }}</v-chip>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'

@Component({ name: 'NuvexaStatusChip' })
export default class NuvexaStatusChip extends Vue {
  @Prop({ required: true })
  label!: string

  @Prop({ default: 'on-surface-variant' })
  color!: string

  @Prop({ default: 'tonal' })
  variant!: string
}
</script>

<style scoped lang="scss">
.nuvexa-status-chip {
  font-weight: 600;
}
</style>
