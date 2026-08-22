<template>
  <div>
    <h1 class="authf-title">{{ $t('auth.redefinirSenha') }}</h1>
    <p class="authf-subtitle">{{ $t('auth.esqueciSubtitulo') }}</p>

    <div v-if="enviado" class="authf-fields">
      <p class="authf-checkbox-text">{{ mensagem }}</p>
    </div>

    <form v-else class="authf-fields" @submit.prevent="enviar">
      <div class="authf-field">
        <label class="authf-label" for="forgot-email">{{ $t('auth.email') }}</label>
        <input
          id="forgot-email"
          v-model="email"
          type="email"
          autocomplete="email"
          placeholder="voce@clinica.com.br"
          class="authf-input"
          :class="{ 'authf-input--error': tentouEnviar && erros.email }"
        />
        <span v-if="tentouEnviar && erros.email" class="authf-error">{{ erros.email }}</span>
      </div>

      <button type="submit" class="authf-btn" :disabled="carregando">
        {{ carregando ? $t('auth.enviarLinkRedefinicao') + '…' : $t('auth.enviarLinkRedefinicao') }}
      </button>
    </form>

    <div class="authf-footer">
      {{ $t('auth.lembrouSenha') }}
      <router-link class="authf-link" to="/login">{{ $t('auth.voltarLogin') }}</router-link>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import authService from '../../service/auth-service'
import { useAppStore } from '../../store/app.store'
import { extrairMensagemErro } from '../../util/api-util'

@Component({ name: 'ForgotPassword' })
export default class ForgotPassword extends Vue {
  email = ''
  carregando = false
  tentouEnviar = false
  enviado = false
  mensagem = ''

  get erros() {
    const erros: Record<string, string> = {}
    if (!this.email) {
      erros.email = this.$t('validacao.obrigatorio') as string
    } else if (!/.+@.+\..+/.test(this.email)) {
      erros.email = this.$t('validacao.emailInvalido') as string
    }
    return erros
  }

  get appStore() {
    return useAppStore()
  }

  async enviar() {
    this.tentouEnviar = true
    if (Object.keys(this.erros).length > 0) {
      return
    }

    this.carregando = true
    try {
      const response = await authService.esqueciSenha({ email: this.email })
      this.mensagem = response.message
      this.enviado = true
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.esqueciSenha') as string), erro: true })
    } finally {
      this.carregando = false
    }
  }
}
</script>
