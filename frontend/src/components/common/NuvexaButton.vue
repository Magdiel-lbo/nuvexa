<template>
  <v-btn
    :color="color"
    :variant="btnVariant"
    :icon="variant === 'icon' ? icon ?? undefined : undefined"
    :prepend-icon="variant !== 'icon' ? (icon ?? undefined) : undefined"
    :size="btnSize"
    :style="{ height: heightPx }"
    :loading="loading"
    :disabled="disabled"
    :block="block"
    class="nuvexa-button"
    :class="`nuvexa-button--${variant}`"
    @click="$emit('click', $event)"
  >
    <slot />
  </v-btn>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'

export type NuvexaButtonVariant = 'primary' | 'secondary' | 'ghost' | 'text' | 'danger' | 'icon'
export type NuvexaButtonSize = 'sm' | 'md' | 'lg'

const HEIGHTS: Record<NuvexaButtonSize, number> = { sm: 34, md: 44, lg: 52 }

@Component({ name: 'NuvexaButton', emits: ['click'] })
export default class NuvexaButton extends Vue {
  @Prop({ default: 'primary' })
  variant!: NuvexaButtonVariant

  @Prop({ default: 'md' })
  size!: NuvexaButtonSize

  @Prop({ default: null })
  icon!: string | null

  @Prop({ type: Boolean, default: false })
  loading!: boolean

  @Prop({ type: Boolean, default: false })
  disabled!: boolean

  @Prop({ type: Boolean, default: false })
  block!: boolean

  get color(): string | undefined {
    if (this.variant === 'primary') return 'primary'
    if (this.variant === 'danger') return 'error'
    return undefined
  }

  get btnVariant(): string {
    switch (this.variant) {
      case 'primary':
      case 'danger':
        return 'flat'
      case 'secondary':
        return 'outlined'
      case 'ghost':
      case 'text':
        return 'text'
      case 'icon':
        return 'text'
      default:
        return 'flat'
    }
  }

  get btnSize(): NuvexaButtonSize {
    return this.variant === 'icon' && this.size === 'md' ? 'md' : this.size
  }

  get heightPx(): string {
    if (this.variant === 'icon') return `${this.size === 'sm' ? 34 : 44}px`
    return `${HEIGHTS[this.size]}px`
  }
}
</script>

<style scoped lang="scss">
.nuvexa-button {
  text-transform: none;
  font-weight: 500;
  letter-spacing: 0;
}

.nuvexa-button--secondary {
  border-color: rgb(var(--v-theme-outline));
  color: rgb(var(--v-theme-on-surface-variant));

  &:hover {
    border-color: rgb(var(--v-theme-primary));
    color: rgb(var(--v-theme-primary));
  }
}

.nuvexa-button--icon {
  min-width: 0;
  width: v-bind(heightPx);
  padding: 0;
}
</style>
