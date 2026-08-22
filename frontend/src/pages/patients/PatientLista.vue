<template>
  <div class="patient-lista">
    <div class="patient-lista__header">
      <h1 class="patient-lista__title">{{ $t('paciente.titulo') }}</h1>
      <v-btn color="primary" prepend-icon="mdi-plus" size="large" @click="novo">{{ $t('paciente.novo') }}</v-btn>
    </div>

    <v-text-field
      v-model="busca"
      :placeholder="$t('paciente.buscarPlaceholder')"
      density="compact"
      variant="underlined"
      single-line
      hide-details
      clearable
      append-inner-icon="mdi-magnify"
      class="nuvexa-field patient-lista__search"
      @keyup.enter="carregar"
      @click:append-inner="carregar"
      @click:clear="onClearBusca"
    />

    <v-card variant="flat" color="surface-variant" class="patient-lista__table-card">
      <v-data-table :headers="headers" :items="pacientes" :loading="loading" :no-data-text="$t('paciente.nenhumEncontrado')">
        <template #item.gender="{ item }">{{ rotulos[item.gender] ?? item.gender }}</template>
        <template #item.goal="{ item }">{{ rotulos[item.goal] ?? item.goal }}</template>
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
            <v-tooltip :text="$t('acao.excluir')" location="top">
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
import patientService from '../../service/patient-service'
import type { PatientResponse } from '../../types/patient'
import { extrairMensagemErro } from '../../util/api-util'
import { useAppStore } from '../../store/app.store'
import { carregarRotulosEnum } from '../../util/enum-rotulos'

@Component({ name: 'PatientLista' })
export default class PatientLista extends Vue {
  pacientes: PatientResponse[] = []
  rotulos: Record<string, string> = {}
  busca = ''
  loading = false

  get headers() {
    return [
      { title: this.$t('paciente.nome'), key: 'name' },
      { title: this.$t('paciente.sexo'), key: 'gender' },
      { title: this.$t('paciente.objetivo'), key: 'goal' },
      { title: this.$t('paciente.imc'), key: 'bmi' },
      { title: '', key: 'acoes', sortable: false, align: 'end' as const },
    ]
  }

  get appStore() {
    return useAppStore()
  }

  async mounted() {
    this.rotulos = await carregarRotulosEnum()
    await this.carregar()
  }

  async carregar() {
    this.loading = true
    try {
      this.pacientes = await patientService.listar(this.busca || undefined)
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
    this.$router.push('/patients/new')
  }

  visualizar(id: number) {
    this.$router.push(`/patients/${id}`)
  }

  editar(id: number) {
    this.$router.push(`/patients/${id}/edit`)
  }

  async excluir(id: number) {
    if (!confirm(this.$t('paciente.confirmarExclusao') as string)) {
      return
    }
    try {
      await patientService.remover(id)
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

.patient-lista__header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
}

.patient-lista__title {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0;
}

.patient-lista__search {
  flex: 1 1 240px;
  max-width: 320px;
  margin-bottom: 24px;
}

.patient-lista__table-card {
  border-radius: 12px;
}
</style>
