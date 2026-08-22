<template>
  <v-text-field
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    type="date"
    :label="label"
    :placeholder="effectivePlaceholder"
    :min="min ?? undefined"
    :max="max ?? undefined"
    :disabled="disabled"
    :aria-label="ariaLabel ?? label"
    variant="underlined"
    density="compact"
    hide-details
    single-line
    class="nuvexa-field"
  />
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'

@Component({ name: 'NuvexaDateField', emits: ['update:modelValue'] })
export default class NuvexaDateField extends Vue {
  @Prop({ required: true })
  modelValue!: string

  @Prop({ default: '' })
  label!: string

  @Prop({ default: null })
  placeholder!: string | null

  @Prop({ default: null })
  min!: string | null

  @Prop({ default: null })
  max!: string | null

  @Prop({ default: false })
  disabled!: boolean

  @Prop({ default: null })
  ariaLabel!: string | null

  get effectivePlaceholder(): string {
    return this.placeholder ?? (this.$t('filtroComum.periodo.placeholderData') as string)
  }
}
</script>

<style scoped lang="scss">
@use './nuvexa-field.scss';
</style>
