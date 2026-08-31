<template>
  <v-card variant="flat" color="surface-variant" class="today-agenda">
    <v-card-title class="today-agenda__title">{{ $t('home.agenda.titulo') }}</v-card-title>
    <v-card-text>
      <div v-if="loading" class="today-agenda__loading">
        <v-progress-circular indeterminate color="primary" />
      </div>

      <NuvexaEmptyState
        v-else-if="consultas.length === 0"
        icon="mdi-calendar-blank-outline"
        :title="$t('home.agenda.vazio.titulo') as string"
        :description="$t('home.agenda.vazio.descricao') as string"
        :action-label="$t('consulta.novo') as string"
        @action="novaConsulta"
      />

      <template v-else>
        <div
          v-for="consulta in consultasVisiveis"
          :key="consulta.id"
          class="today-agenda__item"
          @click="abrirConsulta(consulta)"
        >
          <v-avatar :color="avatarColor(consulta.id)" variant="tonal" size="36">
            <span class="today-agenda__initials">{{ initials(consulta.pacienteNome) }}</span>
          </v-avatar>

          <span class="today-agenda__hora">{{ formatHora(consulta.dataHora) }}</span>
          <span class="today-agenda__nome">{{ consulta.pacienteNome }}</span>

          <v-spacer />

          <v-chip size="small" variant="tonal" color="secondary">{{ $t(`consulta.tipo.${consulta.tipo}`) }}</v-chip>
          <v-chip size="small" variant="tonal" :color="statusColor(consulta.status)">
            {{ $t(`consulta.status.${consulta.status}`) }}
          </v-chip>
        </div>

        <div v-if="consultas.length > maxVisiveis" class="today-agenda__ver-todas">
          <v-btn variant="text" color="primary" append-icon="mdi-arrow-right" @click="verConsultas">
            {{ $t('home.agenda.verTodas') }}
          </v-btn>
        </div>
      </template>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import NuvexaEmptyState from '../../../components/common/NuvexaEmptyState.vue'
import type { Consulta, ConsultaStatus } from '../../../types/consulta'
import { consultaStatusColor } from '../../../util/consulta-status'

const AVATAR_COLORS = ['primary', 'secondary', 'success', 'warning', 'error']

@Component({ name: 'DashboardTodayAgenda', components: { NuvexaEmptyState } })
export default class DashboardTodayAgenda extends Vue {
  @Prop({ required: true })
  consultas!: Consulta[]

  @Prop({ default: false })
  loading!: boolean

  maxVisiveis = 5

  get consultasVisiveis(): Consulta[] {
    return this.consultas.slice(0, this.maxVisiveis)
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

  formatHora(iso: string): string {
    return new Date(iso).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
  }

  abrirConsulta(consulta: Consulta) {
    this.$router.push(`/consultas/${consulta.id}`)
  }

  verConsultas() {
    this.$router.push('/consultas')
  }

  novaConsulta() {
    this.$router.push('/consultas/novo')
  }
}
</script>

<style scoped lang="scss">
.today-agenda {
  border-radius: 12px;
}

.today-agenda__title {
  font-size: 1.1rem;
  font-weight: 600;
}

.today-agenda__loading {
  display: flex;
  justify-content: center;
  padding: 32px 0;
}

.today-agenda__item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 8px;
  border-radius: 8px;
  cursor: pointer;
  flex-wrap: wrap;

  &:hover {
    background: rgba(var(--v-theme-on-surface), 0.04);
  }

  & + & {
    border-top: 1px solid rgba(var(--v-theme-on-surface), 0.08);
  }
}

.today-agenda__initials {
  font-size: 0.8rem;
  font-weight: 600;
}

.today-agenda__hora {
  font-weight: 700;
  font-size: 0.95rem;
}

.today-agenda__nome {
  font-weight: 500;
}

.today-agenda__ver-todas {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}
</style>
