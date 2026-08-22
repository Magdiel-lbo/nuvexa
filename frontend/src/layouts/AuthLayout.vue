<template>
  <v-app>
    <v-main class="auth-page">
      <div class="auth-stage">
        <div class="auth-stage__bg" aria-hidden="true" />
        <div class="auth-stage__dots" aria-hidden="true" />
        <span class="auth-stage__ring auth-stage__ring--top" aria-hidden="true" />
        <span class="auth-stage__ring auth-stage__ring--bottom" aria-hidden="true" />

        <div class="auth-grid">
          <div class="auth-brand">
            <div class="auth-brand__logo">
              <svg width="34" height="34" viewBox="0 0 64 64" fill="none" class="auth-brand__mark" aria-hidden="true">
                <g stroke="currentColor" stroke-width="3" stroke-linecap="round">
                  <line x1="32" y1="32" x2="32" y2="13" />
                  <line x1="32" y1="32" x2="48.5" y2="41.5" />
                  <line x1="32" y1="32" x2="15.5" y2="41.5" />
                </g>
                <circle cx="32" cy="32" r="7.5" fill="currentColor" />
                <circle cx="32" cy="13" r="5.5" fill="currentColor" />
                <circle cx="48.5" cy="41.5" r="5.5" fill="currentColor" />
                <circle cx="15.5" cy="41.5" r="5.5" fill="currentColor" />
                <circle cx="32" cy="32" r="26" stroke="currentColor" stroke-width="2" opacity="0.28" />
              </svg>
              <div class="auth-brand__wordmark">
                <span class="auth-brand__name">Nuvexa</span>
                <span class="auth-brand__vertical">{{ $t('auth.marcaVertical') }}</span>
              </div>
            </div>

            <div class="auth-brand__hero">
              <h1>{{ heroTitle }}</h1>
              <p>{{ heroBody }}</p>
            </div>

            <div class="auth-brand__badges">
              <div class="auth-brand__badge">
                <span class="auth-brand__badge-label">{{ $t('auth.especialidadesLabel') }}</span>
                <span class="auth-brand__badge-value">{{ $t('auth.especialidadesValor') }}</span>
              </div>
              <div class="auth-brand__badge">
                <span class="auth-brand__badge-label">{{ $t('auth.segurancaLabel') }}</span>
                <span class="auth-brand__badge-value">{{ $t('auth.segurancaValor') }}</span>
              </div>
            </div>
          </div>

          <div class="auth-form-panel">
            <div class="auth-form-panel__mobile-brand">
              <svg width="30" height="30" viewBox="0 0 64 64" fill="none" class="auth-brand__mark" aria-hidden="true">
                <g stroke="currentColor" stroke-width="3" stroke-linecap="round">
                  <line x1="32" y1="32" x2="32" y2="13" />
                  <line x1="32" y1="32" x2="48.5" y2="41.5" />
                  <line x1="32" y1="32" x2="15.5" y2="41.5" />
                </g>
                <circle cx="32" cy="32" r="7.5" fill="currentColor" />
                <circle cx="32" cy="13" r="5.5" fill="currentColor" />
                <circle cx="48.5" cy="41.5" r="5.5" fill="currentColor" />
                <circle cx="15.5" cy="41.5" r="5.5" fill="currentColor" />
                <circle cx="32" cy="32" r="26" stroke="currentColor" stroke-width="2" opacity="0.28" />
              </svg>
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

@Component({ name: 'AuthLayout' })
export default class AuthLayout extends Vue {
  get appStore() {
    return useAppStore()
  }

  get heroTitle(): string {
    const key = this.$route.meta.heroTitleKey as string | undefined
    return key ? (this.$t(key) as string) : ''
  }

  get heroBody(): string {
    const key = this.$route.meta.heroBodyKey as string | undefined
    return key ? (this.$t(key) as string) : ''
  }
}
</script>

<style scoped>
.auth-page {
  --ff-bg: #040a11;
  --ff-accent: #37d59f;
  --ff-accent-hover: #5ce0b3;
  --ff-secondary: #002f58;
  --ff-stage-bg: #060d15;
  --ff-card-bg: rgba(13, 21, 28, 0.88);
  --ff-card-border: rgba(43, 52, 61, 0.85);
  --ff-field-bg: #1a2026;
  --ff-field-border: #2b343d;
  --ff-text: #eaeff5;
  --ff-text-muted: #77818c;
  --ff-text-dim: #5f6a75;

  position: relative;
  min-height: 100vh;
  overflow-x: hidden;
  overflow-y: auto;
  background: var(--ff-bg);
  font-family: 'Manrope', Helvetica, Arial, sans-serif;
  padding: 40px 24px 56px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-stage {
  position: relative;
  width: 100%;
  max-width: 1180px;
  min-height: 640px;
  border-radius: 24px;
  overflow: hidden;
  background: var(--ff-stage-bg);
  border: 1px solid #1a2026;
  box-shadow: 0 40px 80px -20px rgba(0, 0, 0, 0.7);
}

.auth-stage__bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(820px 600px at 86% -12%, rgba(55, 213, 159, 0.17), transparent 60%),
    radial-gradient(720px 560px at -8% 110%, rgba(0, 47, 88, 0.5), transparent 62%);
}

.auth-stage__dots {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(119, 129, 140, 0.32) 1px, transparent 1px);
  background-size: 28px 28px;
  mask-image: radial-gradient(circle at 26% 44%, black, transparent 72%);
}

.auth-stage__ring {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.auth-stage__ring--top {
  width: 460px;
  height: 460px;
  border: 1px solid rgba(55, 213, 159, 0.15);
  top: -170px;
  right: -110px;
}

.auth-stage__ring--bottom {
  width: 300px;
  height: 300px;
  border: 1px solid rgba(0, 47, 88, 0.7);
  bottom: -110px;
  left: 4%;
}

.auth-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 440px;
  min-height: 640px;
}

.auth-brand {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 32px;
  padding: 56px 56px 48px;
}

.auth-brand__logo,
.auth-form-panel__mobile-brand {
  display: flex;
  align-items: center;
  gap: 12px;
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
  font-size: 21px;
  font-weight: 700;
  color: var(--ff-text);
}

.auth-brand__vertical {
  font-size: 21px;
  font-weight: 500;
  color: var(--ff-text-muted);
}

.auth-brand__hero {
  max-width: 420px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.auth-brand__hero h1 {
  margin: 0;
  font-size: 38px;
  line-height: 1.15;
  font-weight: 700;
  color: var(--ff-text);
  letter-spacing: -0.02em;
}

.auth-brand__hero p {
  margin: 0;
  font-size: 15px;
  line-height: 1.6;
  color: #8b959f;
}

.auth-brand__badges {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
}

.auth-brand__badge {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.auth-brand__badge-label {
  font-size: 12px;
  color: var(--ff-text-dim);
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.auth-brand__badge-value {
  font-size: 14px;
  color: #aab4be;
  font-weight: 500;
}

.auth-form-panel {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  justify-content: center;
  gap: 24px;
  padding: 40px 48px 40px 8px;
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
  .auth-page {
    padding: 24px 16px 40px;
  }

  .auth-stage {
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
    padding: 40px 20px;
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
