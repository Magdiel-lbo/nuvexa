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

        <v-list-subheader v-if="!isRail">{{ $t('menu.grupoRelatorios') }}</v-list-subheader>
        <v-divider v-else class="my-2 mx-4" />

        <v-list-item
          v-for="item in relatoriosItems"
          :key="item.to"
          :to="item.to"
          :prepend-icon="item.icon"
          :title="isRail ? undefined : item.label"
          rounded="lg"
        >
          <v-tooltip v-if="isRail" activator="parent" location="end">{{ item.label }}</v-tooltip>
        </v-list-item>

        <v-list-subheader v-if="!isRail">{{ $t('menu.grupoGestao') }}</v-list-subheader>
        <v-divider v-else class="my-2 mx-4" />

        <v-list-item
          v-for="item in gestaoItems"
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
            <span class="user-menu-activator__perfil">{{ perfilRotulo }}</span>
          </v-btn>
        </template>
        <v-list density="compact">
          <v-list-item
            v-if="organizacaoNome"
            prepend-icon="mdi-domain"
            :title="organizacaoNome"
            :subtitle="$t('contexto.organizacaoAtual')"
          />
          <v-divider v-if="organizacaoNome" class="my-1" />
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
import { useAuthStore } from '../core/auth/auth.store'
import { useThemeStore } from '../store/theme.store'
import { useContextoStore } from '../core/contexto/contexto.store'
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

  get contextoStore() {
    return useContextoStore()
  }

  get organizacaoNome(): string {
    return this.contextoStore.organizacaoNome
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

  get perfilRotulo(): string {
    const perfil = this.authStore.perfil
    return perfil ? (this.$t(`perfil.${perfil}`) as string) : ''
  }

  get atendimentoItems(): NavItem[] {
    return [
      { to: '/', label: this.$t('menu.dashboard') as string, icon: 'mdi-view-dashboard-outline' },
      { to: '/agenda', label: this.$t('menu.agenda') as string, icon: 'mdi-calendar-month-outline' },
      { to: '/consultas', label: this.$t('menu.consultas') as string, icon: 'mdi-calendar-check-outline' },
      { to: '/pacientes', label: this.$t('menu.pacientes') as string, icon: 'mdi-account-group-outline' },
      { to: '/prontuarios', label: this.$t('menu.prontuarios') as string, icon: 'mdi-file-document-outline' },
      { to: '/nutricao', label: this.$t('menu.nutricao') as string, icon: 'mdi-food-apple-outline' },
      { to: '/avaliacoes', label: this.$t('menu.avaliacoes') as string, icon: 'mdi-clipboard-pulse-outline' },
    ]
  }

  get relatoriosItems(): NavItem[] {
    return [
      { to: '/relatorios', label: this.$t('menu.relatoriosPacientes') as string, icon: 'mdi-account-multiple-outline' },
      { to: '/relatorios/consultas', label: this.$t('menu.relatoriosConsultas') as string, icon: 'mdi-calendar-text-outline' },
      { to: '/relatorios/evolucao-nutricional', label: this.$t('menu.relatoriosEvolucaoNutricional') as string, icon: 'mdi-trending-up' },
      { to: '/relatorios/financeiro', label: this.$t('menu.relatoriosFinanceiro') as string, icon: 'mdi-cash-multiple' },
      { to: '/relatorios/notas-fiscais', label: this.$t('menu.relatoriosNotasFiscais') as string, icon: 'mdi-receipt-text-outline' },
    ]
  }

  get gestaoItems(): NavItem[] {
    return [
      { to: '/empresa', label: this.$t('menu.empresa') as string, icon: 'mdi-domain' },
      { to: '/usuarios', label: this.$t('menu.usuarios') as string, icon: 'mdi-account-multiple-outline' },
      { to: '/vinculos-permissoes', label: this.$t('menu.vinculosPermissoes') as string, icon: 'mdi-shield-account-outline' },
      { to: '/notificacoes', label: this.$t('menu.notificacoes') as string, icon: 'mdi-bell-outline' },
      { to: '/integracoes', label: this.$t('menu.integracoes') as string, icon: 'mdi-puzzle-outline' },
      { to: '/ia', label: this.$t('menu.ia') as string, icon: 'mdi-robot-outline' },
      { to: '/financeiro', label: this.$t('menu.gestaoFinanceiro') as string, icon: 'mdi-finance' },
    ]
  }

  get sistemaItems(): NavItem[] {
    return [
      { to: '/configuracoes/minha-conta', label: this.$t('menu.minhaConta') as string, icon: 'mdi-account-circle-outline' },
      { to: '/configuracoes/preferencias', label: this.$t('menu.preferencias') as string, icon: 'mdi-tune' },
      { to: '/configuracoes/aparencia', label: this.$t('menu.aparencia') as string, icon: 'mdi-palette-outline' },
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
    this.contextoStore.limpar()
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

.user-menu-activator__perfil {
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
