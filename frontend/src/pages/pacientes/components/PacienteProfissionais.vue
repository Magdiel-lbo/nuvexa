<template>
  <v-card variant="flat" color="surface-variant" class="paciente-profissionais">
    <v-card-text class="paciente-profissionais__conteudo">
      <div class="paciente-profissionais__cabecalho">
        <span class="paciente-profissionais__titulo">{{ $t('paciente.profissionais.titulo') }}</span>
        <span class="paciente-profissionais__hint">{{ $t('paciente.profissionais.hint') }}</span>
      </div>

      <div v-if="ativos.length > 0" class="paciente-profissionais__lista">
        <div v-for="vinculo in ativos" :key="vinculo.id" class="paciente-profissionais__item">
          <v-avatar color="primary" variant="tonal" size="28"><span class="paciente-profissionais__iniciais">{{ iniciais(vinculo.profissionalNome) }}</span></v-avatar>
          <span class="paciente-profissionais__nome">{{ vinculo.profissionalNome }}</span>
          <v-tooltip :text="$t('paciente.profissionais.desvincular')" location="top">
            <template #activator="{ props }">
              <v-btn v-bind="props" icon="mdi-close" variant="text" density="comfortable" size="x-small" @click="desvincular(vinculo)" />
            </template>
          </v-tooltip>
        </div>
      </div>
      <span v-else class="paciente-profissionais__vazio">{{ $t('paciente.profissionais.vazio') }}</span>

      <div v-if="opcoes.length > 0" class="paciente-profissionais__adicionar">
        <v-select
          v-model="escolhido"
          :items="opcoes"
          item-title="nome"
          item-value="id"
          :label="$t('paciente.profissionais.adicionar')"
          density="compact"
          hide-details
          clearable
        />
        <v-btn color="primary" variant="tonal" :disabled="escolhido === null" :loading="salvando" @click="vincular">{{ $t('paciente.profissionais.vincular') }}</v-btn>
      </div>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import equipeService from '../../../service/equipe-service'
import consultaService from '../../../service/consulta-service'
import type { PacienteProfissional } from '../../../types/equipe'
import type { Profissional } from '../../../types/consulta'
import { extrairMensagemErro } from '../../../util/api-util'
import { useAppStore } from '../../../store/app.store'

@Component({ name: 'PacienteProfissionais' })
export default class PacienteProfissionais extends Vue {
  @Prop({ required: true })
  pacienteId!: number

  vinculos: PacienteProfissional[] = []
  profissionais: Profissional[] = []
  escolhido: number | null = null
  salvando = false

  get ativos(): PacienteProfissional[] {
    return this.vinculos.filter((vinculo) => vinculo.ativo)
  }

  get opcoes(): Profissional[] {
    const jaVinculados = new Set(this.ativos.map((vinculo) => vinculo.profissionalId))
    return this.profissionais.filter((profissional) => !jaVinculados.has(profissional.id))
  }

  iniciais(nome: string): string {
    return nome.trim().split(/\s+/).filter(Boolean).slice(0, 2).map((parte) => parte[0]).join('').toUpperCase()
  }

  async created() {
    try {
      const [vinculos, profissionais] = await Promise.all([equipeService.profissionaisDoPaciente(this.pacienteId), consultaService.listarProfissionais()])
      this.vinculos = vinculos
      this.profissionais = profissionais
    } catch (e) {
      useAppStore().setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarProfissionais') as string), erro: true })
    }
  }

  async vincular() {
    if (this.escolhido === null) {
      return
    }
    this.salvando = true
    try {
      await equipeService.vincularProfissional(this.pacienteId, this.escolhido)
      this.vinculos = await equipeService.profissionaisDoPaciente(this.pacienteId)
      this.escolhido = null
    } catch (e) {
      useAppStore().setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.vincularProfissional') as string), erro: true })
    } finally {
      this.salvando = false
    }
  }

  async desvincular(vinculo: PacienteProfissional) {
    try {
      await equipeService.desvincularProfissional(this.pacienteId, vinculo.profissionalId)
      this.vinculos = await equipeService.profissionaisDoPaciente(this.pacienteId)
    } catch (e) {
      useAppStore().setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.vincularProfissional') as string), erro: true })
    }
  }
}
</script>

<style scoped lang="scss">
.paciente-profissionais {
  border-radius: 12px;
}

.paciente-profissionais__conteudo {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.paciente-profissionais__cabecalho {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.paciente-profissionais__titulo {
  font-size: 0.9rem;
  font-weight: 600;
}

.paciente-profissionais__hint,
.paciente-profissionais__vazio {
  font-size: 0.78rem;
  color: rgb(var(--v-theme-on-surface-variant));
}

.paciente-profissionais__lista {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.paciente-profissionais__item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.paciente-profissionais__iniciais {
  font-size: 0.62rem;
  font-weight: 700;
}

.paciente-profissionais__nome {
  flex: 1;
  font-size: 0.85rem;
}

.paciente-profissionais__adicionar {
  display: flex;
  align-items: flex-end;
  gap: 10px;
}
</style>
