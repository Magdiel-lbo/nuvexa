<template>
  <v-select
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :items="items"
    :item-title="itemTitle"
    :item-value="itemValue"
    :label="label"
    :placeholder="effectivePlaceholder"
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

@Component({ name: 'NuvexaSelect', emits: ['update:modelValue'] })
export default class NuvexaSelect extends Vue {
  @Prop({ required: true })
  modelValue!: unknown

  @Prop({ required: true })
  items!: unknown[]

  @Prop({ default: 'label' })
  itemTitle!: string

  @Prop({ default: 'value' })
  itemValue!: string

  @Prop({ default: '' })
  label!: string

  @Prop({ default: null })
  placeholder!: string | null

  @Prop({ default: false })
  disabled!: boolean

  @Prop({ default: null })
  ariaLabel!: string | null

  get effectivePlaceholder(): string {
    return this.placeholder ?? (this.$t('filtroComum.selecionar') as string)
  }
}
</script>

<style scoped lang="scss">
@use './nuvexa-field.scss';
</style>
