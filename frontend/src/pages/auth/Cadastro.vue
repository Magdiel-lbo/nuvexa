<template>
  <div>
    <h1 class="authf-title">{{ $t('auth.criarConta') }}</h1>
    <p class="authf-subtitle">{{ $t('auth.criarSubtitulo') }}</p>

    <form class="authf-fields" @submit.prevent="cadastrar">
      <div class="authf-field">
        <label class="authf-label" for="register-nome">{{ $t('auth.nomeCompleto') }}</label>
        <input
          id="register-nome"
          v-model="nome"
          type="text"
          autocomplete="name"
          placeholder="Dra. Ana Ribeiro"
          class="authf-input"
          :class="{ 'authf-input--error': tentouEnviar && erros.nome }"
        />
        <span v-if="tentouEnviar && erros.nome" class="authf-error">{{ erros.nome }}</span>
      </div>

      <div class="authf-field">
        <label class="authf-label" for="register-email">{{ $t('auth.emailProfissional') }}</label>
        <input
          id="register-email"
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
        <label class="authf-label" for="register-registro">{{ $t('auth.registroProfissional') }}</label>
        <input
          id="register-registro"
          v-model="registroProfissional"
          type="text"
          placeholder="CRM"
          class="authf-input"
        />
      </div>

      <div class="authf-field">
        <label class="authf-label" for="register-senha">{{ $t('auth.senha') }}</label>
        <input
          id="register-senha"
          v-model="senha"
          type="password"
          autocomplete="new-password"
          placeholder="Mínimo 8 caracteres"
          class="authf-input"
          :class="{ 'authf-input--error': tentouEnviar && erros.senha }"
        />
        <span v-if="tentouEnviar && erros.senha" class="authf-error">{{ erros.senha }}</span>
      </div>

      <label class="authf-checkbox-label authf-checkbox-label--terms">
        <input v-model="aceitouTermos" type="checkbox" class="authf-checkbox" />
        <span class="authf-checkbox-text authf-checkbox-text--terms">
          {{ $t('auth.concordoCom') }} <a href="#" @click.prevent>{{ $t('auth.termosDeUso') }}</a>
          {{ $t('auth.eA') }} <a href="#" @click.prevent>{{ $t('auth.politicaPrivacidade') }}</a>.
        </span>
      </label>
      <span v-if="tentouEnviar && erros.termos" class="authf-error">{{ erros.termos }}</span>

      <button type="submit" class="authf-btn" :disabled="carregando">
        {{ carregando ? $t('auth.criarMinhaConta') + '…' : $t('auth.criarMinhaConta') }}
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
      {{ $t('auth.jaTenhoConta') }}
      <router-link class="authf-link" to="/login">{{ $t('auth.entrar') }}</router-link>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import authService from '../../core/auth/auth-service'
import { useAuthStore } from '../../core/auth/auth.store'
import { useAppStore } from '../../store/app.store'
import { extrairMensagemErro } from '../../util/api-util'

@Component({ name: 'Cadastro' })
export default class Cadastro extends Vue {
  nome = ''
  email = ''
  registroProfissional = ''
  senha = ''
  aceitouTermos = false
  carregando = false
  tentouEnviar = false

  get erros() {
    const erros: Record<string, string> = {}
    if (!this.nome) {
      erros.nome = this.$t('validacao.obrigatorio') as string
    }
    if (!this.email) {
      erros.email = this.$t('validacao.obrigatorio') as string
    } else if (!/.+@.+\..+/.test(this.email)) {
      erros.email = this.$t('validacao.emailInvalido') as string
    }
    if (!this.senha) {
      erros.senha = this.$t('validacao.obrigatorio') as string
    } else if (this.senha.length < 8) {
      erros.senha = this.$t('validacao.senhaMinima') as string
    }
    if (!this.aceitouTermos) {
      erros.termos = this.$t('validacao.obrigatorio') as string
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

  async cadastrar() {
    this.tentouEnviar = true
    if (Object.keys(this.erros).length > 0) {
      return
    }

    this.carregando = true
    try {
      const response = await authService.cadastrar({ nome: this.nome, email: this.email, senha: this.senha })
      this.authStore.setSession(response.token, response.perfil)
      this.appStore.setToast({ mensagem: this.$t('sucesso.contaCriada') as string, erro: false })
      this.$router.push('/')
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.registrar') as string), erro: true })
    } finally {
      this.carregando = false
    }
  }
}
</script>
