<template>
  <div>
    <h1 class="text-h5 mb-4">{{ $t('relatorioPacientes.titulo') }}</h1>

    <v-card>
      <v-card-text>
        <div class="d-flex ga-4 mb-4">
          <v-text-field
            v-model="busca"
            :label="$t('paciente.buscarPlaceholder')"
            prepend-inner-icon="mdi-magnify"
            clearable
            @keyup.enter="carregar"
            @click:clear="carregar"
          />
          <v-btn variant="outlined" color="primary" @click="carregar">{{ $t('acao.buscar') }}</v-btn>
          <v-btn variant="outlined" prepend-icon="mdi-file-excel" :loading="exportando" @click="exportarExcel">
            {{ $t('relatorioPacientes.exportarExcel') }}
          </v-btn>
        </div>

        <v-data-table :headers="headers" :items="linhasFormatadas" :loading="carregando" item-value="id">
          <template #item.acoes="{ item }">
            <v-tooltip :text="$t('dashboardPacientes.acoes.visualizar')" location="top">
              <template #activator="{ props }">
                <v-btn v-bind="props" icon="mdi-view-grid-outline" variant="text" density="comfortable" size="small" @click="visualizar(item.id)" />
              </template>
            </v-tooltip>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import pacienteService from '../services/paciente-service'
import type { EnumOpcao, PacienteRelatorioResponse } from '../types/paciente'

function toMapaRotulos(opcoes: EnumOpcao[]): Record<string, string> {
  return Object.fromEntries(opcoes.map((opcao) => [opcao.valor, opcao.rotulo]))
}

@Component({ name: 'RelatorioPacientes' })
export default class RelatorioPacientes extends Vue {
  busca = ''
  carregando = false
  exportando = false
  relatorio: PacienteRelatorioResponse = { colunas: [], linhas: [] }
  rotulosSexo: Record<string, string> = {}
  rotulosObjetivo: Record<string, string> = {}
  rotulosNivelAtividade: Record<string, string> = {}

  get headers() {
    const colunas = [...this.relatorio.colunas]
      .sort((a, b) => a.ordem - b.ordem)
      .map((coluna) => ({ title: coluna.rotulo, key: coluna.chave }))
    return [...colunas, { title: this.$t('acao.titulo') as string, key: 'acoes', sortable: false, align: 'end' as const }]
  }

  get linhasFormatadas() {
    return this.relatorio.linhas.map((linha) => ({
      ...linha,
      sexo: this.rotulosSexo[linha.sexo] ?? linha.sexo,
      objetivo: this.rotulosObjetivo[linha.objetivo] ?? linha.objetivo,
      nivelAtividade: this.rotulosNivelAtividade[linha.nivelAtividade] ?? linha.nivelAtividade,
    }))
  }

  async created() {
    const enums = await pacienteService.enums()
    this.rotulosSexo = toMapaRotulos(enums.sexos)
    this.rotulosObjetivo = toMapaRotulos(enums.objetivos)
    this.rotulosNivelAtividade = toMapaRotulos(enums.niveisAtividade)
    await this.carregar()
  }

  async carregar() {
    this.carregando = true
    try {
      this.relatorio = await pacienteService.relatorio({ busca: this.busca || undefined })
    } finally {
      this.carregando = false
    }
  }

  visualizar(id: number) {
    this.$router.push(`/pacientes/${id}`)
  }

  async exportarExcel() {
    this.exportando = true
    try {
      const blob = await pacienteService.relatorioExcel({ busca: this.busca || undefined })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = 'relatorio-pacientes.xlsx'
      link.click()
      URL.revokeObjectURL(url)
    } finally {
      this.exportando = false
    }
  }
}
</script>
