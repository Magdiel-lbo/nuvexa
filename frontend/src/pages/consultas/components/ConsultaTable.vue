<template>
  <v-data-table
    :headers="headers"
    :items="consultas"
    :items-per-page="10"
    mobile-breakpoint="sm"
    class="consulta-table"
  >
    <template #item.pacienteNome="{ item }">
      <div class="consulta-cell">
        <v-avatar :color="avatarColor(item.id)" variant="tonal" size="36">
          <span class="consulta-cell__initials">{{ initials(item.pacienteNome) }}</span>
        </v-avatar>
        <span class="consulta-cell__name">{{ item.pacienteNome }}</span>
      </div>
    </template>

    <template #item.dataHora="{ item }">
      <span>{{ formatDateTime(item.dataHora) }}</span>
    </template>

    <template #item.tipo="{ item }">
      <v-chip size="small" variant="tonal" color="secondary">{{ $t(`consulta.tipo.${item.tipo}`) }}</v-chip>
    </template>

    <template #item.status="{ item }">
      <v-chip size="small" variant="tonal" :color="statusColor(item.status)">
        {{ $t(`consulta.status.${item.status}`) }}
      </v-chip>
    </template>

    <template #item.actions="{ item }">
      <div class="d-flex justify-end ga-1">
        <v-tooltip :text="$t('dashboardPacientes.acoes.visualizar')" location="top">
          <template #activator="{ props }">
            <v-btn v-bind="props" icon="mdi-view-grid-outline" variant="text" density="comfortable" size="small" @click="$emit('view', item)" />
          </template>
        </v-tooltip>
        <v-tooltip :text="$t('acao.editar')" location="top">
          <template #activator="{ props }">
            <v-btn v-bind="props" icon="mdi-pencil-outline" variant="text" density="comfortable" size="small" @click="$emit('edit', item)" />
          </template>
        </v-tooltip>
        <v-menu>
          <template #activator="{ props }">
            <v-btn v-bind="props" icon="mdi-dots-vertical" variant="text" density="comfortable" size="small" :aria-label="$t('dashboardPacientes.acoes.maisOpcoes')" />
          </template>
          <v-list density="compact">
            <v-list-item prepend-icon="mdi-close-circle-outline" :title="$t('dashboardConsultas.acoes.cancelar')" @click="$emit('cancel', item)" />
            <v-list-item prepend-icon="mdi-delete-outline" :title="$t('acao.excluir')" @click="$emit('delete', item)" />
          </v-list>
        </v-menu>
      </div>
    </template>
  </v-data-table>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import type { Consulta, ConsultaStatus } from '../../../types/consulta'
import { consultaStatusColor } from '../../../util/consulta-status'

const AVATAR_COLORS = ['primary', 'secondary', 'success', 'warning', 'error']

@Component({ name: 'ConsultaTable', emits: ['view', 'edit', 'cancel', 'delete'] })
export default class ConsultaTable extends Vue {
  @Prop({ required: true })
  consultas!: Consulta[]

  get headers() {
    return [
      { title: this.$t('dashboardConsultas.tabela.paciente'), key: 'pacienteNome' },
      { title: this.$t('dashboardConsultas.tabela.dataHora'), key: 'dataHora' },
      { title: this.$t('dashboardConsultas.filtros.tipo'), key: 'tipo' },
      { title: this.$t('dashboardConsultas.filtros.status'), key: 'status' },
      { title: '', key: 'actions', sortable: false, align: 'end' as const },
    ]
  }

  initials(name: string): string {
    const parts = name.trim().split(/\s+/)
    const first = parts[0]?.[0] ?? ''
    const last = parts.length > 1 ? parts[parts.length - 1][0] : ''
    return (first + last).toUpperCase()
  }

  avatarColor(id: number): string {
    return AVATAR_COLORS[id % AVATAR_COLORS.length]
  }

  statusColor(status: ConsultaStatus): string {
    return consultaStatusColor(status)
  }

  formatDateTime(iso: string): string {
    const date = new Date(iso)
    const dateLabel = date.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' })
    const timeLabel = date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
    return `${dateLabel} · ${timeLabel}`
  }
}
</script>

<style scoped lang="scss">
.consulta-table {
  border-radius: 12px;
  overflow: hidden;
}

.consulta-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.consulta-cell__initials {
  font-size: 0.8rem;
  font-weight: 600;
}

.consulta-cell__name {
  font-weight: 500;
}
</style>
