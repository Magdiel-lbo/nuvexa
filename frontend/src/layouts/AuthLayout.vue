<template>
  <v-app>
    <v-main class="auth-page">
      <div class="auth-shell">
        <div class="auth-grid">
          <div class="auth-brand">
            <div class="auth-logo">
              <BrandMark :size="64" class="auth-brand__mark" />
              <div class="auth-brand__wordmark">
                <span class="auth-brand__name">Nuvexa</span>
                <span class="auth-brand__vertical">{{ $t('auth.marcaVertical') }}</span>
              </div>
            </div>

            <p class="auth-brand__phrase">{{ $t('auth.frase') }}</p>
          </div>

          <div class="auth-form-panel">
            <div class="auth-form-panel__mobile-brand">
              <BrandMark :size="30" class="auth-brand__mark" />
              <div class="auth-brand__wordmark">
                <span class="auth-brand__name">Nuvexa</span>
                <span class="auth-brand__vertical">{{ $t('auth.marcaVertical') }}</span>
              </div>
            </div>

            <div class="auth-card">
              <slot />
            </div>
          </div>
        </div>
      </div>
    </v-main>

    <v-snackbar v-model="appStore.toast.show" :color="appStore.toast.erro ? 'error' : 'success'">
      {{ appStore.toast.mensagem }}
    </v-snackbar>
  </v-app>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import { useAppStore } from '../store/app.store'
import BrandMark from '../components/common/BrandMark.vue'

@Component({ name: 'AuthLayout', components: { BrandMark } })
export default class AuthLayout extends Vue {
  get appStore() {
    return useAppStore()
  }
}
</script>

<style scoped>
.auth-page {
  --ff-bg: #04120d;
  --ff-accent: #37d59f;
  --ff-accent-hover: #5ce0b3;
  --ff-secondary: #0b4a44;
  --ff-card-bg: rgba(12, 29, 23, 0.88);
  --ff-card-border: rgba(34, 54, 46, 0.85);
  --ff-field-bg: #14261f;
  --ff-field-border: #22362e;
  --ff-text: #e8f2ed;
  --ff-text-muted: #8fa69c;
  --ff-error: #f2666b;

  position: relative;
  min-height: 100vh;
  overflow-x: hidden;
  overflow-y: auto;
  background: var(--ff-bg);
  font-family: 'Manrope', Helvetica, Arial, sans-serif;
  display: flex;
  align-items: stretch;
  justify-content: center;
}

.auth-shell {
  position: relative;
  width: 100%;
  min-height: 100vh;
  overflow: hidden;
  background: var(--ff-bg);
}

/* glow suave no topo — único ornamento do layout simplificado */
.auth-shell::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(760px 560px at 50% -18%, rgba(55, 213, 159, 0.16), transparent 62%);
  pointer-events: none;
}

/* linha de destaque na borda superior */
.auth-shell::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(55, 213, 159, 0.5), transparent);
  pointer-events: none;
}

.auth-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 600px;
  min-height: 100vh;
}

.auth-brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 24px;
  padding: 56px 56px 48px;
}

.auth-logo,
.auth-form-panel__mobile-brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.auth-brand__mark {
  color: var(--ff-accent);
  flex-shrink: 0;
}

.auth-brand__wordmark {
  display: flex;
  align-items: baseline;
  gap: 7px;
  line-height: 1;
}

.auth-brand__name {
  font-size: 36px;
  font-weight: 700;
  color: var(--ff-text);
}

.auth-brand__vertical {
  font-size: 36px;
  font-weight: 500;
  color: var(--ff-text-muted);
}

.auth-brand__phrase {
  margin: 0 auto;
  max-width: 300px;
  font-size: 15px;
  line-height: 1.6;
  color: var(--ff-text-muted);
}

.auth-form-panel {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  justify-content: center;
  gap: 24px;
  padding: 40px 40px 40px 8px;
}

.auth-form-panel__mobile-brand {
  display: none;
  justify-content: center;
}

.auth-card {
  width: 100%;
  background: var(--ff-card-bg);
  backdrop-filter: blur(22px);
  border: 1px solid var(--ff-card-border);
  border-radius: 22px;
  padding: 36px 32px;
  display: flex;
  flex-direction: column;
  gap: 22px;
  box-sizing: border-box;
  color: var(--ff-text);
  font-family: inherit;
}

@media (max-width: 900px) {
  .auth-shell {
    min-height: 0;
  }

  .auth-grid {
    grid-template-columns: 1fr;
    min-height: 0;
  }

  .auth-brand {
    display: none;
  }

  .auth-form-panel {
    padding: 48px 20px 40px;
    gap: 28px;
  }

  .auth-form-panel__mobile-brand {
    display: flex;
  }

  .auth-card {
    padding: 28px 22px;
  }
}
</style>
