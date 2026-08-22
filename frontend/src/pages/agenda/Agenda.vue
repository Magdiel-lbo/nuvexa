<template>
  <div class="agenda">
    <div class="agenda__header">
      <div>
        <h1 class="agenda__title">{{ $t('agenda.titulo') }}</h1>
        <p class="agenda__subtitle">{{ $t('agenda.subtitulo') }}</p>
      </div>
      <v-btn color="primary" prepend-icon="mdi-plus" size="large" @click="onCreate">
        {{ $t('dashboardConsultas.nova') }}
      </v-btn>
    </div>

    <v-card variant="flat" color="surface-variant" class="agenda__toolbar-card">
      <v-card-text class="agenda__toolbar">
        <div class="agenda__toolbar-nav">
          <v-btn icon="mdi-chevron-left" variant="text" density="comfortable" :aria-label="$t('agenda.anterior')" @click="prev" />
          <v-btn variant="outlined" density="comfortable" @click="hoje">{{ $t('agenda.hoje') }}</v-btn>
          <v-btn icon="mdi-chevron-right" variant="text" density="comfortable" :aria-label="$t('agenda.proximo')" @click="next" />
          <span class="agenda__toolbar-title">{{ toolbarTitle }}</span>
        </div>

        <v-btn-toggle v-model="viewType" mandatory density="comfortable" color="primary" variant="outlined" divided>
          <v-btn value="day">{{ $t('agenda.visao.dia') }}</v-btn>
          <v-btn value="week">{{ $t('agenda.visao.semana') }}</v-btn>
          <v-btn value="month">{{ $t('agenda.visao.mes') }}</v-btn>
        </v-btn-toggle>
      </v-card-text>
    </v-card>

    <v-card variant="flat" color="surface-variant" class="agenda__calendar-card">
      <v-calendar
        v-model="focus"
        :type="viewType"
        :events="events"
        :event-color="eventColor"
        color="primary"
        @click:event="onEventClick"
      >
        <template #event="{ event }">
          <v-tooltip activator="parent" location="top" max-width="280">
            <div class="agenda-event-tooltip">
              <div class="agenda-event-tooltip__patient">{{ event.consulta.patientName }}</div>
              <div>{{ $t(`consulta.status.${event.consulta.status}`) }} · {{ formatHora(event.consulta.date) }}</div>
              <div v-if="event.consulta.status === 'CANCELADA' && event.consulta.notes" class="agenda-event-tooltip__motivo">
                {{ $t('consulta.motivoCancelamento') }}: {{ event.consulta.notes }}
              </div>
            </div>
          </v-tooltip>
          <div class="pl-1 agenda-event-content">
            <strong>{{ formatHora(event.consulta.date) }}</strong> {{ event.consulta.patientName }}
          </div>
        </template>
      </v-calendar>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import { mockConsultas } from '../../mocks/consultas.mock'
import type { Consulta } from '../../types/consulta'
import { consultaStatusColor } from '../../util/consulta-status'
import { useAppStore } from '../../store/app.store'

type ViewType = 'day' | 'week' | 'month'

interface AgendaEvent {
  name: string
  start: Date
  end: Date
  color: string
  timed: boolean
  consulta: Consulta
}

function toIso(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

function capitalize(text: string): string {
  return text.charAt(0).toUpperCase() + text.slice(1)
}

@Component({ name: 'Agenda' })
export default class Agenda extends Vue {
  consultas: Consulta[] = mockConsultas
  focus: string = toIso(new Date())
  viewType: ViewType = 'month'

  get appStore() {
    return useAppStore()
  }

  get focusDate(): Date {
    return new Date(`${this.focus}T00:00:00`)
  }

  get events(): AgendaEvent[] {
    return this.consultas.map((consulta) => {
      const start = new Date(consulta.date)
      const end = new Date(start.getTime() + consulta.durationMinutes * 60000)
      return {
        name: `${consulta.patientName} · ${this.$t(`consulta.tipo.${consulta.type}`)}`,
        start,
        end,
        color: consultaStatusColor(consulta.status),
        timed: true,
        consulta,
      }
    })
  }

  get toolbarTitle(): string {
    const date = this.focusDate
    if (this.viewType === 'month') {
      return capitalize(new Intl.DateTimeFormat('pt-BR', { month: 'long', year: 'numeric' }).format(date))
    }
    if (this.viewType === 'week') {
      const start = new Date(date)
      start.setDate(start.getDate() - start.getDay())
      const end = new Date(start)
      end.setDate(start.getDate() + 6)
      const startLabel = start.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' })
      const endLabel = end.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })
      return `${startLabel} – ${endLabel}`
    }
    return capitalize(new Intl.DateTimeFormat('pt-BR', { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' }).format(date))
  }

  eventColor(event: AgendaEvent): string {
    return event.color
  }

  formatHora(iso: string): string {
    return new Date(iso).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
  }

  prev() {
    this.shift(-1)
  }

  next() {
    this.shift(1)
  }

  hoje() {
    this.focus = toIso(new Date())
  }

  shift(direction: number) {
    const date = this.focusDate
    if (this.viewType === 'month') {
      date.setMonth(date.getMonth() + direction)
    } else if (this.viewType === 'week') {
      date.setDate(date.getDate() + direction * 7)
    } else {
      date.setDate(date.getDate() + direction)
    }
    this.focus = toIso(date)
  }

  onCreate() {
    this.appStore.setToast({ mensagem: 'Layout de demonstração — agendamento ainda não integrado.', erro: false })
  }

  onEventClick(_nativeEvent: Event, info: { event: AgendaEvent }) {
    const consulta = info.event.consulta
    this.appStore.setToast({ mensagem: `${consulta.patientName} — ${this.$t(`consulta.status.${consulta.status}`)}`, erro: false })
  }
}
</script>

<style scoped lang="scss">
.agenda__header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
}

.agenda__title {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 4px;
}

.agenda__subtitle {
  color: rgb(var(--v-theme-on-surface-variant));
  margin: 0;
  max-width: 60ch;
}

.agenda__toolbar-card {
  border-radius: 12px;
  margin-bottom: 20px;
}

.agenda__toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.agenda__toolbar-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.agenda__toolbar-title {
  font-weight: 600;
  margin-left: 8px;
}

.agenda__calendar-card {
  border-radius: 12px;
  overflow: hidden;
  height: 720px;
}

.agenda-event-content {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.agenda-event-tooltip {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.agenda-event-tooltip__patient {
  font-weight: 700;
}

.agenda-event-tooltip__motivo {
  color: rgb(var(--v-theme-error));
}
</style>
