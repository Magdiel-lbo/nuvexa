<template>
  <div>
    <h1 class="authf-title">{{ $t('convite.aceitar.titulo') }}</h1>

    <p v-if="!token" class="authf-subtitle">{{ $t('convite.aceitar.semToken') }}</p>

    <template v-else-if="logado">
      <p class="authf-subtitle">{{ $t('convite.aceitar.logado') }}</p>
      <div class="authf-fields">
        <button type="button" class="authf-btn" :disabled="carregando" @click="aceitarLogado">
          {{ $t('convite.aceitar.botao') }}
        </button>
        <span v-if="erro" class="authf-error">{{ erro }}</span>
        <button type="button" class="authf-google-btn" @click="sairEEntrarComOutra">{{ $t('convite.aceitar.outraConta') }}</button>
      </div>
    </template>

    <template v-else>
      <p class="authf-subtitle">{{ $t('convite.aceitar.subtitulo') }}</p>

      <form class="authf-fields" @submit.prevent="aceitarCriandoConta">
        <div class="authf-field">
          <label class="authf-label" for="convite-nome">{{ $t('auth.nome') }}</label>
          <input
            id="convite-nome"
            v-model="nome"
            type="text"
            autocomplete="name"
            class="authf-input"
            :class="{ 'authf-input--error': tentouEnviar && erros.nome }"
          />
          <span v-if="tentouEnviar && erros.nome" class="authf-error">{{ erros.nome }}</span>
        </div>

        <div class="authf-field">
          <label class="authf-label" for="convite-senha">{{ $t('auth.senha') }}</label>
          <input
            id="convite-senha"
            v-model="senha"
            type="password"
            autocomplete="new-password"
            class="authf-input"
            :class="{ 'authf-input--error': tentouEnviar && erros.senha }"
          />
          <span v-if="tentouEnviar && erros.senha" class="authf-error">{{ erros.senha }}</span>
        </div>

        <button type="submit" class="authf-btn" :disabled="carregando">{{ $t('convite.aceitar.criarConta') }}</button>
        <span v-if="erro" class="authf-error">{{ erro }}</span>
      </form>

      <div class="authf-footer">
        {{ $t('convite.aceitar.jaTemConta') }}
        <router-link class="authf-link" :to="{ path: '/login', query: { redirect: redirectDeVolta } }">{{ $t('auth.entrar') }}</router-link>
      </div>
    </template>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import equipeService from '../../service/equipe-service'
import { useAuthStore } from '../../core/auth/auth.store'
import { useContextoStore } from '../../core/contexto/contexto.store'
import { useAppStore } from '../../store/app.store'
import { extrairMensagemErro } from '../../util/api-util'

@Component({ name: 'AceitarConvite' })
export default class AceitarConvite extends Vue {
  nome = ''
  senha = ''
  carregando = false
  tentouEnviar = false
  erro = ''

  get token(): string {
    const valor = this.$route.query.token
    return typeof valor === 'string' ? valor : ''
  }

  get logado(): boolean {
    return useAuthStore().isAuthenticated
  }

  get redirectDeVolta(): string {
    return `/aceitar-convite?token=${encodeURIComponent(this.token)}`
  }

  get erros() {
    const erros: Record<string, string> = {}
    if (!this.nome.trim()) {
      erros.nome = this.$t('validacao.obrigatorio') as string
    }
    if (this.senha.length < 8) {
      erros.senha = this.$t('validacao.senhaMinima') as string
    }
    return erros
  }

  async aceitarCriandoConta() {
    this.tentouEnviar = true
    this.erro = ''
    if (Object.keys(this.erros).length > 0) {
      return
    }
    this.carregando = true
    try {
      const resposta = await equipeService.aceitarCriandoConta({ token: this.token, nome: this.nome.trim(), senha: this.senha })
      useAuthStore().setSession(resposta.token, resposta.perfil, resposta.refreshToken)
      this.$router.push('/')
    } catch (e) {
      this.erro = extrairMensagemErro(e, this.$t('erro.aceitarConvite') as string)
    } finally {
      this.carregando = false
    }
  }

  async aceitarLogado() {
    this.carregando = true
    this.erro = ''
    try {
      const organizacao = await equipeService.aceitarLogado(this.token)
      useAppStore().setToast({ mensagem: this.$t('convite.aceitar.sucesso', { nome: organizacao.nome }) as string, erro: false })
      await useContextoStore().selecionarOrganizacao(organizacao.organizacaoId)
    } catch (e) {
      this.erro = extrairMensagemErro(e, this.$t('erro.aceitarConvite') as string)
    } finally {
      this.carregando = false
    }
  }

  sairEEntrarComOutra() {
    useAuthStore().logout()
    useContextoStore().limpar()
    this.$router.push({ path: '/login', query: { redirect: this.redirectDeVolta } })
  }
}
</script>
