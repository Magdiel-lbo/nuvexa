# Nuvexa — Handoff de identidade (SVG + Vue 3 / Vuetify 3)

## 1. Tokens de cor

| Token | Hex | Uso |
|---|---|---|
| `background` | `#040a11` | fundo escuro |
| `surface` | `#0d151c` | cards, painéis |
| `surface-variant` | `#1a2026` | inputs |
| `outline` | `#2b343d` | bordas |
| `on-surface` | `#eaeff5` | texto primário (dark) |
| `on-surface-variant` | `#77818c` | texto secundário |
| `primary` (dark) | `#37d59f` | ações, marca |
| `primary` (light) | `#0f7f5c` | marca sobre fundo claro |
| `secondary` | `#002f58` | azul de apoio / sufixo de vertical |
| `background-light` | `#f4f7f6` | fundo claro |

---

## 2. Símbolo (mark) — SVG standalone

Plataforma = núcleo central + 3 nós conectados. Funciona em qualquer vertical.
`currentColor` permite herdar a cor do contexto (verde, branco ou preto).

`src/assets/nuvexa-mark.svg`

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" width="64" height="64" fill="none">
  <g stroke="currentColor" stroke-width="3" stroke-linecap="round">
    <line x1="32" y1="32" x2="32" y2="13"/>
    <line x1="32" y1="32" x2="48.5" y2="41.5"/>
    <line x1="32" y1="32" x2="15.5" y2="41.5"/>
  </g>
  <circle cx="32" cy="32" r="7.5" fill="currentColor"/>
  <circle cx="32" cy="13" r="5.5" fill="currentColor"/>
  <circle cx="48.5" cy="41.5" r="5.5" fill="currentColor"/>
  <circle cx="15.5" cy="41.5" r="5.5" fill="currentColor"/>
  <circle cx="32" cy="32" r="26" stroke="currentColor" stroke-width="2" opacity="0.28"/>
</svg>
```

- Sobre fundo escuro: `color: #37d59f`
- Sobre fundo claro: `color: #0f7f5c`
- Monocromático: `color: #fff` ou `#000`

---

## 3. Favicon (versão simplificada, legível a 16px)

Sem o anel externo e sem os traços finos — só o núcleo e os 3 nós.

`public/favicon.svg`

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" width="64" height="64" fill="none">
  <rect width="64" height="64" rx="14" fill="#040a11"/>
  <circle cx="32" cy="30" r="10" fill="#37d59f"/>
  <circle cx="32" cy="11" r="6" fill="#37d59f"/>
  <circle cx="50" cy="45" r="6" fill="#37d59f"/>
  <circle cx="14" cy="45" r="6" fill="#37d59f"/>
</svg>
```

```html
<link rel="icon" type="image/svg+xml" href="/favicon.svg" />
```

---

## 4. Componente Vue — `NuvexaLogo.vue`

Cobre mark isolado, lockup e sufixo de vertical (Nutri, Odonto…).

`src/components/NuvexaLogo.vue`

```vue
<template>
  <div class="nuvexa-logo" :style="{ color: markColor }">
    <svg :width="size" :height="size" viewBox="0 0 64 64" fill="none" aria-hidden="true">
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

    <span v-if="showWordmark" class="nuvexa-wordmark" :style="{ fontSize: size * 0.62 + 'px' }">
      <strong :style="{ color: wordColor }">Nuvexa</strong>
      <em v-if="vertical" :style="{ color: verticalColor }">{{ vertical }}</em>
    </span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useTheme } from 'vuetify'

const props = defineProps({
  size: { type: Number, default: 32 },
  showWordmark: { type: Boolean, default: true },
  vertical: { type: String, default: '' },      // 'Nutri' | 'Odonto' | 'Oftalmo' | ...
  mono: { type: Boolean, default: false }        // versão de uma tinta
})

const theme = useTheme()
const isDark = computed(() => theme.global.current.value.dark)

const markColor = computed(() =>
  props.mono ? 'currentColor' : (isDark.value ? '#37d59f' : '#0f7f5c'))
const wordColor = computed(() =>
  props.mono ? 'currentColor' : (isDark.value ? '#eaeff5' : '#101820'))
const verticalColor = computed(() =>
  props.mono ? 'currentColor' : (isDark.value ? '#77818c' : '#4b5661'))
</script>

<style scoped>
.nuvexa-logo { display: inline-flex; align-items: center; gap: 0.42em; }
.nuvexa-wordmark { display: inline-flex; align-items: baseline; gap: 0.3em; line-height: 1; letter-spacing: 0.01em; }
.nuvexa-wordmark strong { font-weight: 700; }
.nuvexa-wordmark em { font-weight: 500; font-style: normal; }
</style>
```

Uso:

```vue
<NuvexaLogo :size="28" />                        <!-- navbar -->
<NuvexaLogo :size="44" vertical="Nutri" />       <!-- vertical -->
<NuvexaLogo :size="40" :show-wordmark="false" /> <!-- só o mark -->
<NuvexaLogo :size="32" mono class="text-white" /><!-- monocromático -->
```

---

## 5. Tema Vuetify

`src/plugins/vuetify.js`

```js
import { createVuetify } from 'vuetify'

const nuvexaDark = {
  dark: true,
  colors: {
    background: '#040a11',
    surface: '#0d151c',
    'surface-variant': '#1a2026',
    'on-surface': '#eaeff5',
    'on-surface-variant': '#77818c',
    outline: '#2b343d',
    primary: '#37d59f',
    'primary-darken-1': '#117555',
    secondary: '#002f58',
    error: '#e5484d',
    success: '#37d59f',
    warning: '#f5a524'
  }
}

const nuvexaLight = {
  dark: false,
  colors: {
    background: '#f4f7f6',
    surface: '#ffffff',
    'surface-variant': '#eef2f1',
    'on-surface': '#101820',
    'on-surface-variant': '#4b5661',
    outline: '#d5dcdb',
    primary: '#0f7f5c',
    'primary-darken-1': '#0a5c42',
    secondary: '#002f58',
    error: '#d13438',
    success: '#0f7f5c',
    warning: '#b26a00'
  }
}

export default createVuetify({
  theme: { defaultTheme: 'nuvexaDark', themes: { nuvexaDark, nuvexaLight } },
  defaults: {
    VBtn: { rounded: 'lg', height: 48, class: 'text-none font-weight-medium' },
    VTextField: { variant: 'outlined', density: 'comfortable', color: 'primary' },
    VCard: { rounded: 'xl', flat: true }
  }
})
```

---

## 6. Tela de login (Vuetify)

`src/views/LoginView.vue`

```vue
<template>
  <v-app>
    <v-main class="login-bg">
      <div class="login-shell">
        <div class="login-brand d-none d-md-flex">
          <NuvexaLogo :size="52" />
          <p class="login-tagline">Sua clínica, conectada em uma só plataforma.</p>
        </div>

        <v-card class="login-card" color="surface">
          <v-card-text class="pa-8">
            <NuvexaLogo :size="34" class="d-md-none mb-6" />
            <h1 class="text-h6 font-weight-bold mb-6">Entrar</h1>

            <v-form @submit.prevent="onSubmit">
              <v-text-field v-model="email" label="E-mail" type="email" autocomplete="email" class="mb-2" />
              <v-text-field v-model="password" label="Senha" type="password" autocomplete="current-password" class="mb-4" />
              <v-btn type="submit" color="primary" block size="large" :loading="loading">Entrar</v-btn>
              <div class="text-center mt-4">
                <a href="#" class="text-primary text-body-2">Esqueci minha senha</a>
              </div>
            </v-form>
          </v-card-text>
        </v-card>
      </div>
    </v-main>
  </v-app>
</template>

<script setup>
import { ref } from 'vue'
import NuvexaLogo from '@/components/NuvexaLogo.vue'

const email = ref(''), password = ref(''), loading = ref(false)
function onSubmit() { /* auth */ }
</script>

<style scoped>
.login-bg {
  position: relative;
  background:
    radial-gradient(760px 560px at 88% -10%, rgba(55, 213, 159, 0.16), transparent 60%),
    radial-gradient(680px 520px at -8% 108%, rgba(0, 47, 88, 0.42), transparent 60%),
    #040a11;
}
/* grade de pontos discreta, com fade radial */
.login-bg::before {
  content: '';
  position: absolute; inset: 0;
  background-image: radial-gradient(rgba(119, 129, 140, 0.35) 1px, transparent 1px);
  background-size: 28px 28px;
  mask-image: radial-gradient(circle at 70% 40%, #000, transparent 70%);
  -webkit-mask-image: radial-gradient(circle at 70% 40%, #000, transparent 70%);
  pointer-events: none;
}
/* anel abstrato */
.login-bg::after {
  content: '';
  position: absolute; top: -140px; right: -80px;
  width: 420px; height: 420px; border-radius: 50%;
  border: 1px solid rgba(55, 213, 159, 0.18);
  pointer-events: none;
}
.login-shell {
  position: relative; z-index: 1;
  min-height: 100vh;
  display: flex; align-items: center; justify-content: center;
  gap: 96px; padding: 32px;
}
.login-brand { flex-direction: column; align-items: center; gap: 18px; }
.login-tagline { max-width: 260px; text-align: center; color: #77818c; font-size: 14px; line-height: 1.5; }
.login-card {
  width: 100%; max-width: 400px;
  background: rgba(13, 21, 28, 0.85) !important;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(43, 52, 61, 0.7);
}
</style>
```

---

## 7. Regras de marca (resumo)

- **Nuvexa** sozinho = plataforma-mãe. **Nuvexa + sufixo** = vertical (Nutri, Odonto…). O símbolo nunca muda por vertical.
- Espaço livre mínimo em volta do mark: metade da sua altura.
- Nunca aplicar o mark verde sobre fundos de baixo contraste; use a versão monocromática.
- Tamanho mínimo do lockup: 24px de altura do mark. Abaixo disso, use só o favicon.

