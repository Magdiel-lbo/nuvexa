<template>
  <component :is="layout">
    <router-view />
  </component>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import DefaultLayout from './layouts/DefaultLayout.vue'
import AuthLayout from './layouts/AuthLayout.vue'

@Component({
  name: 'App',
  components: { DefaultLayout, AuthLayout },
})
export default class App extends Vue {
  get layout() {
    return this.$route.meta.public ? 'AuthLayout' : 'DefaultLayout'
  }
}
</script>
