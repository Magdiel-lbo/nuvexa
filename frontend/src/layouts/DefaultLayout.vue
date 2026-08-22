<template>
  <v-app>
    <v-navigation-drawer
      :model-value="drawerModelValue"
      @update:model-value="onDrawerUpdate"
      :rail="isRail"
      rail-width="76"
      width="264"
      color="surface"
      border="0"
      class="app-sidebar"
    >
      <router-link to="/" class="app-sidebar__brand" :class="{ 'app-sidebar__brand--rail': isRail }">
        <BrandMark :size="30" class="app-sidebar__brand-icon" />
        <span v-if="!isRail" class="app-sidebar__brand-name">{{ $t('app.nome') }}</span>
      </router-link>

      <v-divider />

      <v-list nav density="comfortable" class="app-sidebar__list">
        <v-list-item
          to="/"
          prepend-icon="mdi-view-dashboard-outline"
          :title="isRail ? undefined : $t('menu.dashboard')"
          rounded="lg"
        >
          <v-tooltip v-if="isRail" activator="parent" location="end">{{ $t('menu.dashboard') }}</v-tooltip>
        </v-list-item>

        <v-list-subheader v-if="!isRail">{{ $t('menu.grupoAtendimento') }}</v-list-subheader>
        <v-divider v-else class="my-2 mx-4" />

        <v-list-item
          v-for="item in atendimentoItems"
          :key="item.to"
          :to="item.to"
          :prepend-icon="item.icon"
          :title="isRail ? undefined : item.label"
          rounded="lg"
        >
          <v-tooltip v-if="isRail" activator="parent" location="end">{{ item.label }}</v-tooltip>
        </v-list-item>

        <v-list-subheader v-if="!isRail">{{ $t('menu.grupoSistema') }}</v-list-subheader>
        <v-divider v-else class="my-2 mx-4" />

        <v-list-item
          v-for="item in sistemaItems"
          :key="item.to"
          :to="item.to"
          :prepend-icon="item.icon"
          :title="isRail ? undefined : item.label"
          rounded="lg"
        >
          <v-tooltip v-if="isRail" activator="parent" location="end">{{ item.label }}</v-tooltip>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-app-bar color="surface" density="comfortable" border="0" elevation="0" class="app-header">
      <v-btn
        icon="mdi-menu"
        variant="text"
        :aria-label="isRail ? $t('menu.expandir') : $t('menu.recolher')"
        @click="toggleSidebar"
      />

      <div v-if="pageTitle" class="topbar-title">
        <span class="topbar-title__text">{{ pageTitle }}</span>
      </div>

      <v-spacer />

      <template v-if="authStore.isAuthenticated">
        <v-btn icon="mdi-bell-outline" variant="text" :aria-label="$t('menu.notificacoes')" />
      </template>

      <v-btn
        icon
        variant="text"
        :aria-label="themeStore.isDark ? $t('tema.claro') : $t('tema.escuro')"
        @click="themeStore.toggleTheme()"
      >
        <v-icon>{{ themeStore.isDark ? 'mdi-white-balance-sunny' : 'mdi-weather-night' }}</v-icon>
      </v-btn>

      <v-menu v-if="authStore.isAuthenticated">
        <template #activator="{ props }">
          <v-btn variant="text" v-bind="props" class="user-menu-activator">
            <v-avatar size="30" color="primary" variant="tonal" class="mr-2">
              <v-icon icon="mdi-account" size="18" />
            </v-avatar>
            <span class="user-menu-activator__role">{{ roleLabel }}</span>
          </v-btn>
        </template>
        <v-list density="compact">
          <v-list-item prepend-icon="mdi-logout" :title="$t('auth.sair')" @click="sair" />
        </v-list>
      </v-menu>
    </v-app-bar>

    <v-main>
      <v-container fluid class="main-container">
        <div class="page-shell">
          <slot />
        </div>
      </v-container>
    </v-main>

    <v-snackbar v-model="appStore.toast.show" :color="appStore.toast.erro ? 'error' : 'success'">
      {{ appStore.toast.mensagem }}
    </v-snackbar>
  </v-app>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import { useAppStore } from '../store/app.store'
import { useAuthStore } from '../store/auth.store'
import { useThemeStore } from '../store/theme.store'
import BrandMark from '../components/common/BrandMark.vue'

interface NavItem {
  to: string
  label: string
  icon: string
}

const MOBILE_BREAKPOINT = 960

@Component({ name: 'DefaultLayout', components: { BrandMark } })
export default class DefaultLayout extends Vue {
  isMobile = false
  rail = false
  mobileDrawerOpen = false

  get appStore() {
    return useAppStore()
  }

  get authStore() {
    return useAuthStore()
  }

  get themeStore() {
    return useThemeStore()
  }

  get isRail(): boolean {
    return !this.isMobile && this.rail
  }

  get drawerModelValue(): boolean {
    return this.isMobile ? this.mobileDrawerOpen : true
  }

  get pageTitle(): string | undefined {
    return this.$route.meta.title
  }

  get roleLabel(): string {
    const role = this.authStore.role
    return role ? (this.$t(`role.${role}`) as string) : ''
  }

  get atendimentoItems(): NavItem[] {
    return [
      { to: '/patients', label: this.$t('menu.pacientes') as string, icon: 'mdi-account-group-outline' },
      { to: '/agenda', label: this.$t('menu.agenda') as string, icon: 'mdi-calendar-month-outline' },
      { to: '/consultas', label: this.$t('menu.consultas') as string, icon: 'mdi-calendar-check-outline' },
      { to: '/nutricao', label: this.$t('menu.nutricao') as string, icon: 'mdi-food-apple-outline' },
      { to: '/avaliacoes', label: this.$t('menu.avaliacoes') as string, icon: 'mdi-clipboard-pulse-outline' },
    ]
  }

  get sistemaItems(): NavItem[] {
    return [
      { to: '/relatorios', label: this.$t('menu.relatorios') as string, icon: 'mdi-chart-line' },
      { to: '/configuracoes', label: this.$t('menu.configuracoes') as string, icon: 'mdi-cog-outline' },
    ]
  }

  mounted() {
    this.updateIsMobile()
    window.addEventListener('resize', this.updateIsMobile)
  }

  beforeUnmount() {
    window.removeEventListener('resize', this.updateIsMobile)
  }

  updateIsMobile() {
    this.isMobile = window.innerWidth < MOBILE_BREAKPOINT
  }

  toggleSidebar() {
    if (this.isMobile) {
      this.mobileDrawerOpen = !this.mobileDrawerOpen
    } else {
      this.rail = !this.rail
    }
  }

  onDrawerUpdate(value: boolean) {
    if (this.isMobile) {
      this.mobileDrawerOpen = value
    }
  }

  sair() {
    this.authStore.logout()
    this.$router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.app-sidebar {
  border-right: 1px solid rgba(var(--v-theme-on-surface), 0.08);
}

.app-header {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.16) !important;
}

.app-sidebar__brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  text-decoration: none;
  color: inherit;
}

.app-sidebar__brand--rail {
  justify-content: center;
  padding: 16px 0;
}

.app-sidebar__brand-icon {
  color: rgb(var(--v-theme-primary));
  flex-shrink: 0;
}

.app-sidebar__brand-name {
  font-weight: 700;
  font-size: 1.05rem;
  white-space: nowrap;
}

.app-sidebar__list {
  padding: 8px;
}

.topbar-title__text {
  font-size: 1.05rem;
  font-weight: 600;
  margin-left: 8px;
}

.user-menu-activator {
  text-transform: none;
}

.user-menu-activator__role {
  font-size: 0.85rem;
}

.main-container {
  padding: 16px;

  @media (max-width: 600px) {
    padding: 8px;
  }
}

.page-shell {
  background: rgb(var(--v-theme-surface));
  border-radius: 16px;
  padding: 24px;
  min-height: calc(100vh - 96px);
  max-width: 1320px;
  margin: 0 auto;

  @media (max-width: 600px) {
    border-radius: 12px;
    padding: 16px;
  }
}
</style>
