<template>
  <v-text-field
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :label="label"
    :placeholder="placeholder ?? undefined"
    :prepend-inner-icon="prependIcon ?? undefined"
    :clearable="clearable"
    :disabled="disabled"
    :aria-label="ariaLabel ?? label"
    variant="underlined"
    density="compact"
    hide-details
    class="nuvexa-field"
    @click:clear="$emit('clear')"
  />
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'

@Component({ name: 'NuvexaField', emits: ['update:modelValue', 'clear'] })
export default class NuvexaField extends Vue {
  @Prop({ default: '' })
  modelValue!: string

  @Prop({ default: '' })
  label!: string

  @Prop({ default: null })
  placeholder!: string | null

  @Prop({ default: null })
  prependIcon!: string | null

  @Prop({ type: Boolean, default: false })
  clearable!: boolean

  @Prop({ default: false })
  disabled!: boolean

  @Prop({ default: null })
  ariaLabel!: string | null
}
</script>

<style scoped lang="scss">
@use './nuvexa-field.scss';
</style>
