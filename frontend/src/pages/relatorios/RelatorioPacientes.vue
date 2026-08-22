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
            @update:model-value="carregar"
          />
          <v-btn variant="outlined" prepend-icon="mdi-file-excel" :loading="exportando" @click="exportarExcel">
            {{ $t('relatorioPacientes.exportarExcel') }}
          </v-btn>
        </div>

        <v-data-table :headers="headers" :items="linhasFormatadas" :loading="carregando" item-value="id" />
      </v-card-text>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import patientService from '../../service/patient-service'
import type { EnumOption, PatientReportResponse } from '../../types/patient'

function toLabelMap(options: EnumOption[]): Record<string, string> {
  return Object.fromEntries(options.map((option) => [option.value, option.label]))
}

@Component({ name: 'RelatorioPacientes' })
export default class RelatorioPacientes extends Vue {
  busca = ''
  carregando = false
  exportando = false
  relatorio: PatientReportResponse = { columns: [], rows: [] }
  generoLabels: Record<string, string> = {}
  objetivoLabels: Record<string, string> = {}
  nivelAtividadeLabels: Record<string, string> = {}

  get headers() {
    return [...this.relatorio.columns]
      .sort((a, b) => a.order - b.order)
      .map((coluna) => ({ title: coluna.label, key: coluna.key }))
  }

  get linhasFormatadas() {
    return this.relatorio.rows.map((linha) => ({
      ...linha,
      gender: this.generoLabels[linha.gender] ?? linha.gender,
      goal: this.objetivoLabels[linha.goal] ?? linha.goal,
      activityLevel: this.nivelAtividadeLabels[linha.activityLevel] ?? linha.activityLevel,
    }))
  }

  async created() {
    const enums = await patientService.enums()
    this.generoLabels = toLabelMap(enums.genders)
    this.objetivoLabels = toLabelMap(enums.goals)
    this.nivelAtividadeLabels = toLabelMap(enums.activityLevels)
    await this.carregar()
  }

  async carregar() {
    this.carregando = true
    try {
      this.relatorio = await patientService.relatorio({ search: this.busca || undefined })
    } finally {
      this.carregando = false
    }
  }

  async exportarExcel() {
    this.exportando = true
    try {
      const blob = await patientService.relatorioExcel({ search: this.busca || undefined })
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
