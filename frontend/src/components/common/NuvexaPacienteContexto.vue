<template>
  <v-card variant="flat" color="surface-variant" class="nuvexa-paciente-contexto">
    <v-card-text class="nuvexa-paciente-contexto__bar">
      <slot name="prefixo" />

      <v-avatar color="primary" variant="tonal" size="42">
        <span class="nuvexa-paciente-contexto__iniciais">{{ iniciais }}</span>
      </v-avatar>
      <div class="nuvexa-paciente-contexto__info">
        <span class="nuvexa-paciente-contexto__nome">{{ nome }}</span>
        <span v-if="subtitulo" class="nuvexa-paciente-contexto__sub">{{ subtitulo }}</span>
      </div>

      <v-divider v-if="vitais.length > 0" vertical class="nuvexa-paciente-contexto__divisor" />

      <div v-if="vitais.length > 0" class="nuvexa-paciente-contexto__vitais">
        <div v-for="vital in vitais" :key="vital.label" class="nuvexa-paciente-contexto__vital">
          <span class="nuvexa-paciente-contexto__vital-label">{{ vital.label }}</span>
          <div class="nuvexa-paciente-contexto__vital-linha">
            <span class="nuvexa-paciente-contexto__vital-valor">{{ vital.valor }}</span>
            <span v-if="vital.delta" class="nuvexa-paciente-contexto__vital-delta">{{ vital.delta }}</span>
          </div>
        </div>
      </div>

      <div v-if="alertas.length > 0 || mostrarLinkProntuario" class="nuvexa-paciente-contexto__extras">
        <span v-for="alerta in alertas" :key="alerta.label" class="nuvexa-paciente-contexto__alerta">
          <v-icon icon="mdi-alert-circle-outline" size="13" />
          {{ alerta.label }}
        </span>
        <a
          v-if="mostrarLinkProntuario"
          href="#"
          class="nuvexa-paciente-contexto__link"
          @click.prevent="$emit('abrir-prontuario')"
        >
          {{ $t('paciente.abrirProntuario') }}
        </a>
      </div>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'

export interface NuvexaVital {
  label: string
  valor: string
  delta?: string
}

export interface NuvexaAlerta {
  label: string
}

@Component({ name: 'NuvexaPacienteContexto', emits: ['abrir-prontuario'] })
export default class NuvexaPacienteContexto extends Vue {
  @Prop({ required: true })
  nome!: string

  @Prop({ default: '' })
  subtitulo!: string

  @Prop({ default: () => [] })
  vitais!: NuvexaVital[]

  @Prop({ default: () => [] })
  alertas!: NuvexaAlerta[]

  @Prop({ default: false })
  mostrarLinkProntuario!: boolean

  get iniciais(): string {
    return this.nome
      .trim()
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((parte) => parte[0])
      .join('')
      .toUpperCase()
  }
}
</script>

<style scoped lang="scss">
.nuvexa-paciente-contexto {
  border-radius: 12px;
}

.nuvexa-paciente-contexto__bar {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
}

.nuvexa-paciente-contexto__iniciais {
  font-size: 13px;
  font-weight: 700;
}

.nuvexa-paciente-contexto__info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nuvexa-paciente-contexto__nome {
  font-size: 17px;
  font-weight: 600;
}

.nuvexa-paciente-contexto__sub {
  font-size: 12.5px;
  color: rgb(var(--v-theme-on-surface-variant));
}

.nuvexa-paciente-contexto__divisor {
  align-self: stretch;
  min-height: 34px;
}

.nuvexa-paciente-contexto__vitais {
  display: flex;
  flex: 1;
  gap: 32px;
  flex-wrap: wrap;
  justify-content: space-between;
  max-width: 420px;
}

.nuvexa-paciente-contexto__vital {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nuvexa-paciente-contexto__vital-label {
  font-size: 10.5px;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: rgb(var(--v-theme-on-surface-variant));
}

.nuvexa-paciente-contexto__vital-linha {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.nuvexa-paciente-contexto__vital-valor {
  font-size: 15px;
  font-weight: 600;
}

.nuvexa-paciente-contexto__vital-delta {
  font-size: 11.5px;
  font-weight: 600;
  color: rgb(var(--v-theme-on-surface-variant));
}

.nuvexa-paciente-contexto__extras {
  display: flex;
  gap: 10px;
  margin-left: auto;
  flex-wrap: wrap;
  align-items: center;
}

.nuvexa-paciente-contexto__alerta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: rgb(var(--v-theme-error));
  background: rgba(var(--v-theme-error), 0.09);
  border: 1px solid rgba(var(--v-theme-error), 0.3);
  border-radius: 999px;
  padding: 5px 12px;
  white-space: nowrap;
}

.nuvexa-paciente-contexto__link {
  font-size: 12.5px;
  font-weight: 600;
  white-space: nowrap;
}
</style>
