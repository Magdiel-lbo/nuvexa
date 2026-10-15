<template>
  <div class="minha-empresa">
    <div class="minha-empresa__header">
      <h1 class="minha-empresa__title">{{ $t('minhaEmpresa.titulo') }}</h1>
      <p class="minha-empresa__subtitle">{{ $t('minhaEmpresa.subtitulo') }}</p>
    </div>

    <p v-if="carregando">...</p>

    <v-card v-else variant="flat" color="surface-variant" class="minha-empresa__card">
      <v-card-text class="pt-4">
        <v-form ref="form" v-model="formValido" @submit.prevent="salvar">
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.nome" :label="$t('empresa.campos.nomeFantasia')" :rules="[obrigatorio]" />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.razaoSocial" :label="$t('empresa.campos.razaoSocial')" />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.documento" :label="$t('empresa.campos.cnpj')" />
            </v-col>
          </v-row>
          <v-btn color="primary" type="submit" class="mt-4" :loading="salvando">{{ $t('acao.salvar') }}</v-btn>
        </v-form>
      </v-card-text>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import empresaService from '../../service/empresa-service'
import { extrairMensagemErro } from '../../util/api-util'
import { useAppStore } from '../../store/app.store'
import { useContextoStore } from '../../core/contexto/contexto.store'

@Component({ name: 'MinhaEmpresa' })
export default class MinhaEmpresa extends Vue {
  form = { nome: '', razaoSocial: '', documento: '' }
  formValido = true
  carregando = false
  salvando = false

  get appStore() {
    return useAppStore()
  }

  obrigatorio = (valor: unknown) => (valor !== null && valor !== undefined && valor !== '') || (this.$t('validacao.obrigatorio') as string)

  async created() {
    this.carregando = true
    try {
      const empresa = await empresaService.minhaEmpresa()
      this.form = { nome: empresa.nome, razaoSocial: empresa.razaoSocial ?? '', documento: empresa.documento ?? '' }
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarEmpresas') as string), erro: true })
    } finally {
      this.carregando = false
    }
  }

  async salvar() {
    const resultado = await (this.$refs.form as any).validate()
    if (!resultado.valid) {
      return
    }
    this.salvando = true
    try {
      await empresaService.atualizarMinhaEmpresa({
        nome: this.form.nome.trim(),
        razaoSocial: this.form.razaoSocial.trim() || null,
        documento: this.form.documento.trim() || null,
      })
      await useContextoStore().carregar()
      this.appStore.setToast({ mensagem: this.$t('sucesso.salvo') as string, erro: false })
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.salvarEmpresa') as string), erro: true })
    } finally {
      this.salvando = false
    }
  }
}
</script>

<style scoped lang="scss">
.minha-empresa__header {
  margin-bottom: 24px;
}

.minha-empresa__title {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.minha-empresa__subtitle {
  color: rgb(var(--v-theme-on-surface-variant));
  margin: 4px 0 0;
}

.minha-empresa__card {
  border-radius: 12px;
}
</style>
