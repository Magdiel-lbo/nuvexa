<template>
  <div class="consulta-editar">
    <div class="consulta-editar__header">
      <v-btn icon="mdi-arrow-left" variant="text" :aria-label="$t('acao.voltar')" @click="voltar" />
      <div>
        <h1 class="consulta-editar__title">{{ titulo }}</h1>
        <p v-if="consulta" class="consulta-editar__subtitle">{{ consulta.patientName }}</p>
      </div>
    </div>

    <v-card v-if="form" variant="flat" color="surface-variant" class="consulta-editar__card">
      <v-card-text class="pt-4">
        <ConsultaForm
          v-model="form"
          :submit-label="$t('acao.salvar') as string"
          :readonly="isView"
          @submit="onSubmit"
          @cancel="voltar"
        />
      </v-card-text>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import ConsultaForm from './components/ConsultaForm.vue'
import type { ConsultaFormModel } from './components/ConsultaForm.vue'
import { mockConsultas } from '../../mocks/consultas.mock'
import type { Consulta } from '../../types/consulta'
import { useAppStore } from '../../store/app.store'

function toDatetimeLocal(iso: string): string {
  return iso.slice(0, 16)
}

function fromDatetimeLocal(value: string): string {
  return value.length === 16 ? `${value}:00` : value
}

@Component({ name: 'ConsultaEditar', components: { ConsultaForm } })
export default class ConsultaEditar extends Vue {
  consulta: Consulta | null = null
  form: ConsultaFormModel | null = null

  get appStore() {
    return useAppStore()
  }

  get isView(): boolean {
    return this.$route.name === 'consulta-visualizar'
  }

  get titulo(): string {
    return this.isView ? (this.$t('consulta.detalhes') as string) : (this.$t('consulta.editar') as string)
  }

  created() {
    const id = Number(this.$route.params.id)
    const consulta = mockConsultas.find((c) => c.id === id)
    if (!consulta) {
      this.$router.replace('/consultas')
      return
    }
    this.consulta = consulta
    this.form = {
      date: toDatetimeLocal(consulta.date),
      durationMinutes: consulta.durationMinutes,
      type: consulta.type,
      status: consulta.status,
      notes: consulta.notes,
    }
  }

  onSubmit() {
    if (!this.consulta || !this.form) {
      return
    }
    Object.assign(this.consulta, {
      date: fromDatetimeLocal(this.form.date),
      durationMinutes: this.form.durationMinutes,
      type: this.form.type,
      status: this.form.status,
      notes: this.form.notes,
    })
    this.appStore.setToast({ mensagem: this.$t('sucesso.salvo') as string, erro: false })
    this.voltar()
  }

  voltar() {
    this.$router.push('/consultas')
  }
}
</script>

<style scoped lang="scss">
.consulta-editar__header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.consulta-editar__title {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.consulta-editar__subtitle {
  color: rgb(var(--v-theme-on-surface-variant));
  margin: 0;
}

.consulta-editar__card {
  border-radius: 12px;
}
</style>
