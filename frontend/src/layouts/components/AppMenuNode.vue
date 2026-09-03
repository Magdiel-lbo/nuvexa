<template>
  <v-list-group v-if="hasChildren" :value="item.id">
    <template #activator="{ props: activatorProps }">
      <v-list-item
        v-bind="activatorProps"
        :prepend-icon="depth === 0 ? (item.icon ?? undefined) : undefined"
        :title="isRail && depth === 0 ? undefined : label"
        rounded="lg"
      >
        <v-tooltip v-if="isRail && depth === 0" activator="parent" location="end">{{ label }}</v-tooltip>
      </v-list-item>
    </template>

    <AppMenuNode
      v-for="child in item.children"
      :key="child.id"
      :item="child"
      :depth="depth + 1"
      :is-rail="isRail"
    />
  </v-list-group>

  <v-list-item
    v-else
    :to="item.route ?? undefined"
    :prepend-icon="depth === 0 ? (item.icon ?? undefined) : undefined"
    rounded="lg"
  >
    <template v-if="depth > 0" #prepend>
      <span class="app-menu-node__bullet" aria-hidden="true" />
    </template>
    <v-list-item-title v-if="!(isRail && depth === 0)">{{ label }}</v-list-item-title>
    <v-tooltip v-if="isRail && depth === 0" activator="parent" location="end">{{ label }}</v-tooltip>
  </v-list-item>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import type { MenuItemResponse } from '../../core/menu/menu'

@Component({ name: 'AppMenuNode' })
export default class AppMenuNode extends Vue {
  @Prop({ required: true })
  item!: MenuItemResponse

  @Prop({ default: 0 })
  depth!: number

  @Prop({ default: false })
  isRail!: boolean

  get hasChildren(): boolean {
    return this.item.children.length > 0
  }

  get label(): string {
    return this.$t(this.item.labelKey) as string
  }
}
</script>

<style scoped lang="scss">
.app-menu-node__bullet {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  opacity: 0.5;
  margin-right: 16px;
  margin-left: 4px;
}
</style>
