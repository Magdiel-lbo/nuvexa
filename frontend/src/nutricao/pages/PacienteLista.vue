<template>
  <div class="paciente-lista">
    <div class="paciente-lista__header">
      <h1 class="paciente-lista__title">{{ $t('paciente.titulo') }}</h1>
      <v-btn color="primary" prepend-icon="mdi-plus" size="large" @click="novo">{{ $t('paciente.novo') }}</v-btn>
    </div>

    <div class="paciente-lista__search-row">
      <v-text-field
        v-model="busca"
        :placeholder="$t('paciente.buscarPlaceholder')"
        density="compact"
        variant="underlined"
        single-line
        hide-details
        clearable
        append-inner-icon="mdi-magnify"
        class="nuvexa-field paciente-lista__search"
        @keyup.enter="carregar"
        @click:append-inner="carregar"
        @click:clear="onClearBusca"
      />
      <v-btn variant="outlined" color="primary" @click="carregar">{{ $t('acao.buscar') }}</v-btn>
    </div>

    <v-card variant="flat" color="surface-variant" class="paciente-lista__table-card">
      <v-data-table :headers="headers" :items="pacientes" :loading="loading" :no-data-text="$t('paciente.nenhumEncontrado')">
        <template #item.sexo="{ item }">{{ rotulos[item.sexo] ?? item.sexo }}</template>
        <template #item.objetivo="{ item }">{{ rotulos[item.objetivo] ?? item.objetivo }}</template>
        <template #item.acoes="{ item }">
          <div class="d-flex justify-end ga-1">
            <v-tooltip :text="$t('dashboardPacientes.acoes.visualizar')" location="top">
              <template #activator="{ props }">
                <v-btn v-bind="props" icon="mdi-view-grid-outline" variant="text" density="comfortable" size="small" @click="visualizar(item.id)" />
              </template>
            </v-tooltip>
            <v-tooltip :text="$t('acao.editar')" location="top">
              <template #activator="{ props }">
                <v-btn v-bind="props" icon="mdi-pencil-outline" variant="text" density="comfortable" size="small" @click="editar(item.id)" />
              </template>
            </v-tooltip>
            <v-tooltip v-if="podeExcluir" :text="$t('acao.excluir')" location="top">
              <template #activator="{ props }">
                <v-btn v-bind="props" icon="mdi-delete-outline" variant="text" density="comfortable" size="small" @click="excluir(item.id)" />
              </template>
            </v-tooltip>
          </div>
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import pacienteService from '../services/paciente-service'
import type { PacienteResponse } from '../types/paciente'
import { extrairMensagemErro } from '../../util/api-util'
import { useAppStore } from '../../store/app.store'
import { usePermissoes } from '../../core/permissions/permissoes'
import { carregarRotulosEnum } from '../utils/enum-rotulos'

@Component({ name: 'PacienteLista' })
export default class PacienteLista extends Vue {
  pacientes: PacienteResponse[] = []
  rotulos: Record<string, string> = {}
  busca = ''
  loading = false

  get headers() {
    return [
      { title: this.$t('paciente.nome'), key: 'nome' },
      { title: this.$t('paciente.sexo'), key: 'sexo' },
      { title: this.$t('paciente.objetivo'), key: 'objetivo' },
      { title: this.$t('paciente.imc'), key: 'imc' },
      { title: '', key: 'acoes', sortable: false, align: 'end' as const },
    ]
  }

  get appStore() {
    return useAppStore()
  }

  /** Só esconde o botão; quem barra de fato a exclusão é o backend. */
  get podeExcluir(): boolean {
    return usePermissoes().podeExcluirPaciente
  }

  async mounted() {
    this.rotulos = await carregarRotulosEnum()
    await this.carregar()
  }

  async carregar() {
    this.loading = true
    try {
      this.pacientes = await pacienteService.listar(this.busca || undefined)
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarPacientes') as string), erro: true })
    } finally {
      this.loading = false
    }
  }

  onClearBusca() {
    this.busca = ''
    this.carregar()
  }

  novo() {
    this.$router.push('/pacientes/novo')
  }

  visualizar(id: number) {
    this.$router.push(`/pacientes/${id}`)
  }

  editar(id: number) {
    this.$router.push(`/pacientes/${id}/editar`)
  }

  async excluir(id: number) {
    if (!confirm(this.$t('paciente.confirmarExclusao') as string)) {
      return
    }
    try {
      await pacienteService.remover(id)
      this.appStore.setToast({ mensagem: this.$t('sucesso.excluido') as string, erro: false })
      await this.carregar()
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.excluirPaciente') as string), erro: true })
    }
  }
}
</script>

<style scoped lang="scss">
@use '../../components/common/nuvexa-field.scss';

.paciente-lista__header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
}

.paciente-lista__title {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0;
}

.paciente-lista__search-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 24px;
}

.paciente-lista__search {
  flex: 1 1 240px;
  max-width: 320px;
}

.paciente-lista__table-card {
  border-radius: 12px;
}
</style>
