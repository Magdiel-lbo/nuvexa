<template>
  <div>
    <h1 class="authf-title">{{ $t('auth.redefinirSenha') }}</h1>
    <p class="authf-subtitle">{{ $t('auth.redefinirSubtitulo') }}</p>

    <div v-if="!token" class="authf-alert">{{ $t('auth.linkInvalido') }}</div>

    <form v-else class="authf-fields" @submit.prevent="redefinir">
      <div class="authf-field">
        <label class="authf-label" for="reset-nova-senha">{{ $t('auth.novaSenha') }}</label>
        <input
          id="reset-nova-senha"
          v-model="novaSenha"
          type="password"
          autocomplete="new-password"
          placeholder="Mínimo 8 caracteres"
          class="authf-input"
          :class="{ 'authf-input--error': tentouEnviar && erros.novaSenha }"
        />
        <span v-if="tentouEnviar && erros.novaSenha" class="authf-error">{{ erros.novaSenha }}</span>
      </div>

      <div class="authf-field">
        <label class="authf-label" for="reset-confirmar-senha">{{ $t('auth.confirmarSenha') }}</label>
        <input
          id="reset-confirmar-senha"
          v-model="confirmarSenha"
          type="password"
          autocomplete="new-password"
          placeholder="••••••••"
          class="authf-input"
          :class="{ 'authf-input--error': tentouEnviar && erros.confirmarSenha }"
        />
        <span v-if="tentouEnviar && erros.confirmarSenha" class="authf-error">{{ erros.confirmarSenha }}</span>
      </div>

      <button type="submit" class="authf-btn" :disabled="carregando">
        {{ carregando ? $t('auth.redefinirSenha') + '…' : $t('auth.redefinirSenha') }}
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

@Component({ name: 'RedefinirSenha' })
export default class RedefinirSenha extends Vue {
  novaSenha = ''
  confirmarSenha = ''
  carregando = false
  tentouEnviar = false

  get erros() {
    const erros: Record<string, string> = {}
    if (!this.novaSenha) {
      erros.novaSenha = this.$t('validacao.obrigatorio') as string
    } else if (this.novaSenha.length < 8) {
      erros.novaSenha = this.$t('validacao.senhaMinima') as string
    }
    if (!this.confirmarSenha) {
      erros.confirmarSenha = this.$t('validacao.obrigatorio') as string
    } else if (this.confirmarSenha !== this.novaSenha) {
      erros.confirmarSenha = this.$t('validacao.senhasNaoConferem') as string
    }
    return erros
  }

  get token(): string {
    return (this.$route.query.token as string) || ''
  }

  get appStore() {
    return useAppStore()
  }

  async redefinir() {
    this.tentouEnviar = true
    if (Object.keys(this.erros).length > 0) {
      return
    }

    this.carregando = true
    try {
      await authService.redefinirSenha({ token: this.token, novaSenha: this.novaSenha })
      this.appStore.setToast({ mensagem: this.$t('sucesso.senhaRedefinida') as string, erro: false })
      this.$router.push('/login')
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.redefinirSenha') as string), erro: true })
    } finally {
      this.carregando = false
    }
  }
}
</script>
