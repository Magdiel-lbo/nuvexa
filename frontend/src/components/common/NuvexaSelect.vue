<template>
  <component
    :is="autocomplete ? VAutocomplete : VSelect"
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :items="items"
    :item-title="itemTitle"
    :item-value="itemValue"
    :label="label"
    :placeholder="placeholder ?? undefined"
    :multiple="multiple"
    :chips="chips"
    :closable-chips="closableChips"
    :clearable="clearable"
    :disabled="disabled"
    :aria-label="ariaLabel ?? label"
    variant="underlined"
    density="compact"
    hide-details
    class="nuvexa-field"
  />
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import { VAutocomplete, VSelect } from 'vuetify/components'

@Component({ name: 'NuvexaSelect', emits: ['update:modelValue'] })
export default class NuvexaSelect extends Vue {
  readonly VSelect = VSelect
  readonly VAutocomplete = VAutocomplete

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

  @Prop({ type: Boolean, default: false })
  multiple!: boolean

  @Prop({ type: Boolean, default: false })
  chips!: boolean

  @Prop({ type: Boolean, default: false })
  closableChips!: boolean

  @Prop({ type: Boolean, default: false })
  clearable!: boolean

  @Prop({ type: Boolean, default: false })
  autocomplete!: boolean

  @Prop({ default: false })
  disabled!: boolean

  @Prop({ default: null })
  ariaLabel!: string | null
}
</script>

<style scoped lang="scss">
@use './nuvexa-field.scss';
</style>
