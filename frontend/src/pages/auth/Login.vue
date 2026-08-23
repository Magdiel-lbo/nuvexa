<template>
  <div>
    <h1 class="authf-title">{{ $t('auth.entrar') }}</h1>
    <p class="authf-subtitle">{{ $t('auth.entrarSubtitulo') }}</p>

    <form class="authf-fields" @submit.prevent="entrar">
      <div class="authf-field">
        <label class="authf-label" for="login-email">{{ $t('auth.email') }}</label>
        <input
          id="login-email"
          v-model="email"
          type="email"
          autocomplete="email"
          placeholder="voce@clinica.com.br"
          class="authf-input"
          :class="{ 'authf-input--error': tentouEnviar && erros.email }"
        />
        <span v-if="tentouEnviar && erros.email" class="authf-error">{{ erros.email }}</span>
      </div>

      <div class="authf-field">
        <label class="authf-label" for="login-password">{{ $t('auth.senha') }}</label>
        <input
          id="login-password"
          v-model="senha"
          type="password"
          autocomplete="current-password"
          placeholder="••••••••"
          class="authf-input"
          :class="{ 'authf-input--error': tentouEnviar && erros.senha }"
        />
        <span v-if="tentouEnviar && erros.senha" class="authf-error">{{ erros.senha }}</span>
      </div>

      <div class="authf-row">
        <label class="authf-checkbox-label">
          <input v-model="manterConectado" type="checkbox" class="authf-checkbox" />
          <span class="authf-checkbox-text">{{ $t('auth.manterConectado') }}</span>
        </label>
        <router-link class="authf-link" to="/esqueci-senha">{{ $t('auth.esqueciSenha') }}</router-link>
      </div>

      <button type="submit" class="authf-btn" :disabled="carregando">
        {{ carregando ? $t('auth.entrar') + '…' : $t('auth.entrar') }}
      </button>

      <div class="authf-divider">
        <span class="authf-divider__line" />
        <span class="authf-divider__text">{{ $t('auth.ou') }}</span>
        <span class="authf-divider__line" />
      </div>

      <button type="button" class="authf-google-btn" @click="entrarComGoogle">
        {{ $t('auth.entrarComGoogle') }}
      </button>
    </form>

    <div class="authf-footer">
      {{ $t('auth.aindaNaoTemConta') }}
      <router-link class="authf-link" to="/cadastrar">{{ $t('auth.criarConta') }}</router-link>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import authService from '../../service/auth-service'
import { useAuthStore } from '../../store/auth.store'
import { useAppStore } from '../../store/app.store'
import { extrairMensagemErro } from '../../util/api-util'

@Component({ name: 'Login' })
export default class Login extends Vue {
  email = ''
  senha = ''
  manterConectado = false
  carregando = false
  tentouEnviar = false

  get erros() {
    const erros: Record<string, string> = {}
    if (!this.email) {
      erros.email = this.$t('validacao.obrigatorio') as string
    } else if (!/.+@.+\..+/.test(this.email)) {
      erros.email = this.$t('validacao.emailInvalido') as string
    }
    if (!this.senha) {
      erros.senha = this.$t('validacao.obrigatorio') as string
    }
    return erros
  }

  get authStore() {
    return useAuthStore()
  }

  get appStore() {
    return useAppStore()
  }

  entrarComGoogle() {
    this.appStore.setToast({ mensagem: this.$t('auth.googleEmBreve') as string, erro: false })
  }

  async entrar() {
    this.tentouEnviar = true
    if (Object.keys(this.erros).length > 0) {
      return
    }

    this.carregando = true
    try {
      const response = await authService.login({ email: this.email, senha: this.senha })
      this.authStore.setSession(response.token, response.perfil)
      const redirect = (this.$route.query.redirect as string) || '/'
      this.$router.push(redirect)
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.login') as string), erro: true })
    } finally {
      this.carregando = false
    }
  }
}
</script>
