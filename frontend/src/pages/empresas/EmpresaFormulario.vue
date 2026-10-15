<template>
  <div class="empresa-formulario">
    <div class="empresa-formulario__header">
      <v-btn icon="mdi-arrow-left" variant="text" :aria-label="$t('acao.voltar')" @click="voltar" />
      <div>
        <h1 class="empresa-formulario__title">{{ isCriacao ? $t('empresa.nova') : $t('empresa.editar') }}</h1>
        <p v-if="empresa" class="empresa-formulario__subtitle">{{ rotulo('tipos', empresa.tipo) }} · {{ rotulo('especialidades', empresa.especialidade) }}</p>
      </div>
    </div>

    <p v-if="carregando">...</p>

    <template v-else>
      <v-card variant="flat" color="surface-variant" class="empresa-formulario__card">
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

              <template v-if="isCriacao">
                <v-col cols="12" md="6">
                  <v-select v-model="form.tipo" :items="enums.tipos" item-title="rotulo" item-value="valor" :label="$t('empresa.campos.tipo')" :rules="[obrigatorio]" />
                </v-col>
                <v-col cols="12" md="6">
                  <v-select
                    v-model="form.especialidade"
                    :items="enums.especialidades"
                    item-title="rotulo"
                    item-value="valor"
                    :label="$t('empresa.campos.especialidade')"
                    :rules="[obrigatorio]"
                  />
                </v-col>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="form.emailProprietario"
                    type="email"
                    :label="$t('empresa.campos.emailProprietario')"
                    :hint="$t('empresa.campos.emailProprietarioHint') as string"
                    persistent-hint
                  />
                </v-col>
              </template>
            </v-row>

            <div class="d-flex ga-2 mt-4">
              <v-btn variant="outlined" @click="voltar">{{ $t('acao.cancelar') }}</v-btn>
              <v-btn color="primary" type="submit" :loading="salvando">{{ isCriacao ? $t('acao.criar') : $t('acao.salvar') }}</v-btn>
            </div>
          </v-form>
        </v-card-text>
      </v-card>

      <v-card v-if="empresa && !empresa.proprietarioAtivo" variant="flat" color="surface-variant" class="empresa-formulario__card mt-4">
        <v-card-text class="pt-4">
          <v-alert type="warning" variant="tonal" density="compact" class="mb-4">{{ $t('empresa.semProprietario') }}</v-alert>
          <div class="d-flex ga-2 align-end">
            <v-text-field v-model="emailConvite" type="email" :label="$t('empresa.campos.emailProprietario')" class="flex-grow-1" />
            <v-btn color="primary" :loading="convidando" :disabled="!emailConvite" @click="convidarProprietario">{{ $t('empresa.convidarProprietario') }}</v-btn>
          </div>
        </v-card-text>
      </v-card>
    </template>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import empresaService from '../../service/empresa-service'
import type { Empresa, EmpresaEnums } from '../../types/empresa'
import { extrairMensagemErro } from '../../util/api-util'
import { useAppStore } from '../../store/app.store'

@Component({ name: 'EmpresaFormulario' })
export default class EmpresaFormulario extends Vue {
  empresa: Empresa | null = null
  enums: EmpresaEnums = { tipos: [], especialidades: [], status: [] }
  form = { nome: '', razaoSocial: '', documento: '', tipo: 'CLINICA', especialidade: 'NUTRICAO', emailProprietario: '' }
  emailConvite = ''
  formValido = true
  carregando = false
  salvando = false
  convidando = false

  get appStore() {
    return useAppStore()
  }

  get empresaId(): number | null {
    const id = this.$route.params.id as string | undefined
    return id ? Number(id) : null
  }

  get isCriacao(): boolean {
    return this.empresaId === null
  }

  obrigatorio = (valor: unknown) => (valor !== null && valor !== undefined && valor !== '') || (this.$t('validacao.obrigatorio') as string)

  rotulo(grupo: keyof EmpresaEnums, valor: string): string {
    return this.enums[grupo].find((opcao) => opcao.valor === valor)?.rotulo ?? valor
  }

  async created() {
    this.carregando = true
    try {
      this.enums = await empresaService.enums()
      if (!this.isCriacao) {
        await this.carregarEmpresa()
      }
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarEmpresas') as string), erro: true })
      this.voltar()
    } finally {
      this.carregando = false
    }
  }

  async carregarEmpresa() {
    const empresa = await empresaService.buscarPorId(this.empresaId as number)
    this.empresa = empresa
    this.form.nome = empresa.nome
    this.form.razaoSocial = empresa.razaoSocial ?? ''
    this.form.documento = empresa.documento ?? ''
  }

  async salvar() {
    const resultado = await (this.$refs.form as any).validate()
    if (!resultado.valid) {
      return
    }
    this.salvando = true
    try {
      const base = { nome: this.form.nome.trim(), razaoSocial: this.form.razaoSocial.trim() || null, documento: this.form.documento.trim() || null }
      if (this.isCriacao) {
        await empresaService.criar({
          ...base,
          tipo: this.form.tipo as Empresa['tipo'],
          especialidade: this.form.especialidade,
          emailProprietario: this.form.emailProprietario.trim() || null,
        })
      } else {
        await empresaService.atualizar(this.empresaId as number, base)
      }
      this.appStore.setToast({ mensagem: this.$t('sucesso.salvo') as string, erro: false })
      this.voltar()
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.salvarEmpresa') as string), erro: true })
    } finally {
      this.salvando = false
    }
  }

  async convidarProprietario() {
    this.convidando = true
    try {
      await empresaService.convidarProprietario(this.empresaId as number, this.emailConvite.trim())
      this.appStore.setToast({ mensagem: this.$t('empresa.conviteEnviado') as string, erro: false })
      this.emailConvite = ''
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.convidar') as string), erro: true })
    } finally {
      this.convidando = false
    }
  }

  voltar() {
    this.$router.push('/empresa')
  }
}
</script>

<style scoped lang="scss">
.empresa-formulario__header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.empresa-formulario__title {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.empresa-formulario__subtitle {
  color: rgb(var(--v-theme-on-surface-variant));
  margin: 0;
}

.empresa-formulario__card {
  border-radius: 12px;
}
</style>
