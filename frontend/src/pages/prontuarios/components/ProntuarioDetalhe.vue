<template>
  <div class="prontuario-detalhe">
    <NuvexaPacienteContexto :nome="prontuario.pacienteNome" :subtitulo="contextoSubtitulo" :vitais="contextoVitais">
      <template #prefixo>
        <v-btn icon="mdi-arrow-left" variant="text" :aria-label="$t('acao.voltar')" @click="$emit('voltar')" />
      </template>
    </NuvexaPacienteContexto>

    <div class="prontuario-detalhe__corpo">
      <v-card variant="flat" color="surface-variant" class="prontuario-detalhe__timeline">
        <div class="prontuario-detalhe__timeline-header">
          <span class="prontuario-detalhe__timeline-titulo">{{ $t('prontuario.historico') }}</span>
          <span class="prontuario-detalhe__timeline-total">{{ historico.length }} {{ $t('prontuario.registros') }}</span>
        </div>
        <div class="prontuario-detalhe__timeline-lista">
          <button
            v-for="item in historico"
            :key="item.id"
            type="button"
            class="prontuario-detalhe__timeline-item"
            :class="{ 'prontuario-detalhe__timeline-item--ativo': item.id === prontuario.id }"
            @click="$emit('navegar', item.id)"
          >
            <div class="prontuario-detalhe__timeline-item-topo">
              <span>{{ formatarData(item.atualizadoEm) }}</span>
              <span class="prontuario-detalhe__timeline-status" :class="`prontuario-detalhe__timeline-status--${statusColor(item.status)}`" />
            </div>
            <NuvexaStatusChip
              class="prontuario-detalhe__timeline-secao"
              :label="rotulosProntuario[item.secao] ?? item.secao"
              :color="secaoChipColor(item.secao)"
              variant="tonal"
            />
            <span class="prontuario-detalhe__timeline-autor">{{ item.autorNome }}</span>
          </button>
        </div>
      </v-card>

      <v-card variant="flat" color="surface-variant" class="prontuario-detalhe__nota">
        <v-card-text class="prontuario-detalhe__nota-conteudo">
          <div class="prontuario-detalhe__nota-header">
            <div class="prontuario-detalhe__nota-header-info">
              <div class="prontuario-detalhe__nota-titulo-linha">
                <h1 class="prontuario-detalhe__nota-titulo">
                  {{ rotulosProntuario[prontuario.secao] ?? prontuario.secao }} — {{ formatarData(prontuario.criadoEm) }}
                </h1>
                <NuvexaStatusChip :label="rotulosProntuario[prontuario.status] ?? prontuario.status" :color="statusColor(prontuario.status)" />
                <span v-if="anexos.length > 0" class="prontuario-detalhe__anexo-badge">
                  <v-icon icon="mdi-paperclip" size="14" />{{ anexos.length }}
                </span>
              </div>
              <span class="prontuario-detalhe__nota-sub">{{ prontuario.registro }} · {{ prontuario.autorNome }}</span>
            </div>

            <div class="prontuario-detalhe__nota-header-acoes">
              <v-btn v-if="prontuario.status === 'PENDENTE'" color="primary" variant="flat" @click="$emit('editar')">
                {{ $t('prontuario.editar') }}
              </v-btn>
            </div>
          </div>

          <div class="prontuario-detalhe__meta-grid">
            <div class="prontuario-detalhe__meta-campo">
              <label>{{ $t('prontuario.secao') }}</label>
              <span>{{ rotulosProntuario[prontuario.secao] ?? prontuario.secao }}</span>
            </div>
            <div class="prontuario-detalhe__meta-campo">
              <label>{{ $t('prontuario.autor') }}</label>
              <span>{{ prontuario.autorNome }}</span>
            </div>
            <div class="prontuario-detalhe__meta-campo">
              <label>{{ $t('prontuario.dataRegistro') }}</label>
              <span>{{ formatarData(prontuario.criadoEm) }}</span>
            </div>
          </div>

          <div v-if="avaliacaoRecente" class="prontuario-detalhe__medidas">
            <div class="prontuario-detalhe__medidas-header">
              <div class="prontuario-detalhe__medidas-origem">
                <v-icon icon="mdi-scale-bathroom" size="14" color="primary" />
                <span>{{ textoOrigemMedidas }}</span>
              </div>
              <a href="#" @click.prevent="$emit('abrir-avaliacao', avaliacaoRecente.id)">{{ $t('prontuario.abrirAvaliacao') }}</a>
            </div>
            <div class="prontuario-detalhe__medidas-grid">
              <div v-for="medida in medidasVisiveis" :key="medida.label" class="prontuario-detalhe__medida">
                <span class="prontuario-detalhe__medida-label">{{ medida.label }}</span>
                <span class="prontuario-detalhe__medida-valor">{{ medida.valor }}</span>
              </div>
            </div>
          </div>

          <div v-if="planoAtivo" class="prontuario-detalhe__medidas">
            <div class="prontuario-detalhe__medidas-header">
              <div class="prontuario-detalhe__medidas-origem">
                <v-icon icon="mdi-food-apple-outline" size="14" color="primary" />
                <span>{{ $t('prontuario.planoAlimentarOrigem') }}</span>
              </div>
              <a href="#" @click.prevent="$emit('abrir-plano-alimentar', planoAtivo.id)">{{ $t('prontuario.abrirPlano') }}</a>
            </div>
            <div class="prontuario-detalhe__medidas-grid">
              <div class="prontuario-detalhe__medida">
                <span class="prontuario-detalhe__medida-label">{{ $t('planoAlimentar.nome') }}</span>
                <span class="prontuario-detalhe__medida-valor">{{ planoAtivo.nome }}</span>
              </div>
              <div class="prontuario-detalhe__medida">
                <span class="prontuario-detalhe__medida-label">{{ $t('planoAlimentar.dataInicio') }}</span>
                <span class="prontuario-detalhe__medida-valor">{{ formatarDataSemHora(planoAtivo.dataInicio) }}</span>
              </div>
              <div class="prontuario-detalhe__medida">
                <span class="prontuario-detalhe__medida-label">{{ $t('planoAlimentar.calorias') }}</span>
                <span class="prontuario-detalhe__medida-valor">{{ planoAtivo.calorias }} kcal</span>
              </div>
              <div class="prontuario-detalhe__medida">
                <span class="prontuario-detalhe__medida-label">{{ $t('planoAlimentar.refeicoesPorDia') }}</span>
                <span class="prontuario-detalhe__medida-valor">{{ planoAtivo.refeicoesPorDia }}</span>
              </div>
            </div>
          </div>

          <div class="prontuario-detalhe__nota-texto">
            <label>{{ $t('prontuario.conteudo') }}</label>
            <p>{{ prontuario.conteudo || '—' }}</p>
          </div>

          <div class="prontuario-detalhe__anexos">
            <div class="prontuario-detalhe__anexos-header">
              <span class="prontuario-detalhe__anexos-titulo">{{ $t('prontuario.anexos') }} ({{ anexos.length }})</span>
              <v-btn variant="text" color="primary" size="small" :loading="enviandoAnexo" @click="abrirSeletorArquivo">
                {{ $t('prontuario.adicionarAnexo') }}
              </v-btn>
              <input
                ref="inputArquivo"
                type="file"
                class="prontuario-detalhe__anexo-input"
                accept=".pdf,.jpg,.jpeg,.png,.doc,.docx,.xls,.xlsx"
                @change="onArquivoSelecionado"
              />
            </div>

            <div v-for="anexo in anexos" :key="anexo.id" class="prontuario-detalhe__anexo-item">
              <v-icon :icon="iconeAnexo(anexo.tipoMime)" size="20" color="on-surface-variant" />
              <div class="prontuario-detalhe__anexo-info">
                <span class="prontuario-detalhe__anexo-nome">{{ anexo.nomeOriginal }}</span>
                <span class="prontuario-detalhe__anexo-meta">
                  {{ formatarTamanho(anexo.tamanho) }} · {{ anexo.criadoPorNome }} · {{ formatarDataHora(anexo.criadoEm) }}
                </span>
              </div>
              <div class="prontuario-detalhe__anexo-acoes">
                <v-btn icon="mdi-download" variant="text" density="comfortable" size="small" @click="$emit('baixar-anexo', anexo)" />
                <v-btn
                  v-if="prontuario.status !== 'ASSINADO'"
                  icon="mdi-delete-outline"
                  variant="text"
                  density="comfortable"
                  size="small"
                  @click="$emit('excluir-anexo', anexo.id)"
                />
              </div>
            </div>

            <span v-if="anexos.length === 0" class="prontuario-detalhe__anexos-vazio">{{ $t('prontuario.nenhumAnexo') }}</span>
          </div>

          <div v-if="prontuario.status === 'ASSINADO'" class="prontuario-detalhe__adendos">
            <div class="prontuario-detalhe__adendos-header">
              <span class="prontuario-detalhe__adendos-titulo">{{ $t('prontuario.adendos') }} ({{ adendos.length }})</span>
              <v-btn v-if="!mostrarFormAdendo" variant="text" color="warning" size="small" @click="mostrarFormAdendo = true">
                {{ $t('prontuario.adicionarAdendo') }}
              </v-btn>
            </div>

            <div v-for="adendo in adendos" :key="adendo.id" class="prontuario-detalhe__adendo">
              <div class="prontuario-detalhe__adendo-cabecalho">
                <span class="prontuario-detalhe__adendo-autor">{{ adendo.autorNome }}</span>
                <span class="prontuario-detalhe__adendo-data">{{ formatarDataHora(adendo.criadoEm) }}</span>
              </div>
              <p class="prontuario-detalhe__adendo-texto">{{ adendo.texto }}</p>
            </div>

            <div v-if="mostrarFormAdendo" class="prontuario-detalhe__adendo-form">
              <v-textarea v-model="textoAdendo" :label="$t('prontuario.textoAdendo') as string" rows="3" auto-grow />
              <div class="prontuario-detalhe__adendo-form-acoes">
                <v-btn variant="outlined" @click="cancelarAdendo">{{ $t('acao.cancelar') }}</v-btn>
                <v-btn color="warning" variant="flat" :loading="criandoAdendo" @click="salvarAdendo">{{ $t('acao.salvar') }}</v-btn>
              </div>
            </div>
          </div>

          <div class="prontuario-detalhe__nota-rodape">
            <span class="prontuario-detalhe__auditoria">{{ auditoria }}</span>
            <div v-if="prontuario.status === 'RASCUNHO'" class="prontuario-detalhe__nota-rodape-acoes">
              <v-btn variant="text" @click="$emit('descartar')">{{ $t('prontuario.descartar') }}</v-btn>
              <v-btn variant="outlined" @click="$emit('editar')">{{ $t('prontuario.salvarRascunho') }}</v-btn>
              <v-btn color="primary" variant="flat" :loading="assinando" @click="confirmarAssinatura">
                {{ $t('prontuario.assinarRegistro') }}
              </v-btn>
            </div>
          </div>

          <div class="prontuario-detalhe__historico">
            <button type="button" class="prontuario-detalhe__historico-header" @click="mostrarHistorico = !mostrarHistorico">
              <span>{{ $t('prontuario.historicoAuditoria') }} ({{ eventosAuditoria.length }})</span>
              <v-icon :icon="mostrarHistorico ? 'mdi-chevron-up' : 'mdi-chevron-down'" />
            </button>
            <div v-if="mostrarHistorico" class="prontuario-detalhe__historico-lista">
              <div v-for="evento in eventosAuditoria" :key="evento.id" class="prontuario-detalhe__historico-item">
                <span class="prontuario-detalhe__historico-evento">{{ rotulosProntuario[evento.tipoEvento] ?? evento.tipoEvento }}</span>
                <span class="prontuario-detalhe__historico-usuario">{{ evento.usuarioNome }}</span>
                <span class="prontuario-detalhe__historico-data">{{ formatarDataHora(evento.criadoEm) }}</span>
                <span v-if="evento.dadosDepois" class="prontuario-detalhe__historico-resumo">{{ evento.dadosDepois }}</span>
              </div>
              <span v-if="eventosAuditoria.length === 0" class="prontuario-detalhe__historico-vazio">{{ $t('prontuario.nenhumEvento') }}</span>
            </div>
          </div>
        </v-card-text>
      </v-card>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import NuvexaStatusChip from '../../../components/common/NuvexaStatusChip.vue'
import NuvexaPacienteContexto from '../../../components/common/NuvexaPacienteContexto.vue'
import type { NuvexaVital } from '../../../components/common/NuvexaPacienteContexto.vue'
import type { EventoAuditoria, Prontuario, ProntuarioAdendo, ProntuarioAnexo, StatusProntuario, SecaoProntuario } from '../../../types/prontuario'
import type { PerfilNutricionalResponse } from '../../../nutricao/types/perfil-nutricional'
import type { Avaliacao } from '../../../nutricao/types/avaliacao'
import type { PlanoAlimentar } from '../../../nutricao/types/plano-alimentar'

const STATUS_COLORS: Record<StatusProntuario, string> = {
  RASCUNHO: 'warning',
  PENDENTE: 'warning',
  ASSINADO: 'success',
}

@Component({
  name: 'ProntuarioDetalhe',
  components: { NuvexaStatusChip, NuvexaPacienteContexto },
  emits: [
    'voltar',
    'navegar',
    'editar',
    'assinar',
    'descartar',
    'abrir-avaliacao',
    'abrir-plano-alimentar',
    'criar-adendo',
    'upload-anexo',
    'baixar-anexo',
    'excluir-anexo',
  ],
})
export default class ProntuarioDetalhe extends Vue {
  @Prop({ required: true })
  prontuario!: Prontuario

  @Prop({ default: () => [] })
  historico!: Prontuario[]

  @Prop({ default: () => [] })
  adendos!: ProntuarioAdendo[]

  @Prop({ default: () => [] })
  anexos!: ProntuarioAnexo[]

  @Prop({ default: false })
  enviandoAnexo!: boolean

  @Prop({ default: () => [] })
  eventosAuditoria!: EventoAuditoria[]

  @Prop({ default: false })
  criandoAdendo!: boolean

  @Prop({ default: null })
  perfil!: PerfilNutricionalResponse | null

  @Prop({ default: null })
  avaliacaoRecente!: Avaliacao | null

  @Prop({ default: null })
  planoAtivo!: PlanoAlimentar | null

  @Prop({ default: () => ({}) })
  rotulosProntuario!: Record<string, string>

  @Prop({ default: () => ({}) })
  rotulosEnum!: Record<string, string>

  @Prop({ default: () => ({}) })
  rotulosAvaliacao!: Record<string, string>

  @Prop({ default: false })
  assinando!: boolean

  mostrarFormAdendo = false
  textoAdendo = ''
  mostrarHistorico = false

  get deltaPeso(): string | null {
    const variacao = this.avaliacaoRecente?.variacaoPeso
    if (variacao === null || variacao === undefined) {
      return null
    }
    const sinal = variacao > 0 ? '+' : ''
    return `${sinal}${variacao.toFixed(1).replace('.', ',')} kg`
  }

  get contextoSubtitulo(): string {
    if (!this.perfil) {
      return ''
    }
    const sexo = this.rotulosEnum[this.perfil.sexo] ?? this.perfil.sexo
    return `${sexo} · ${this.perfil.idade} ${this.$t('paciente.detalhe.anos')}`
  }

  get contextoVitais(): NuvexaVital[] {
    if (!this.perfil) {
      return []
    }
    const objetivo = this.rotulosEnum[this.perfil.objetivo] ?? this.perfil.objetivo
    return [
      { label: this.$t('prontuario.pesoAtual') as string, valor: this.formatarPeso(this.perfil.peso), delta: this.deltaPeso ?? undefined },
      { label: this.$t('paciente.imc') as string, valor: this.perfil.imc !== null ? this.perfil.imc.toFixed(1) : '—' },
      { label: this.$t('paciente.objetivo') as string, valor: objetivo },
    ]
  }

  get textoOrigemMedidas(): string {
    const avaliacao = this.avaliacaoRecente as Avaliacao
    const tipo = this.rotulosAvaliacao[avaliacao.tipo] ?? avaliacao.tipo
    return this.$t('prontuario.medidasOrigem', { data: this.formatarDataSemHora(avaliacao.data), tipo }) as string
  }

  get medidasVisiveis(): { label: string; valor: string }[] {
    const medidas: { label: string; valor: string | null }[] = [
      { label: this.$t('prontuario.medidaPeso') as string, valor: this.perfil?.peso != null ? this.formatarPeso(this.perfil.peso) : null },
      { label: this.$t('prontuario.medidaAltura') as string, valor: this.perfil ? this.formatarAltura(this.perfil.altura) : null },
      { label: this.$t('paciente.imc') as string, valor: this.perfil?.imc != null ? this.perfil.imc.toFixed(1) : null },
      {
        label: this.$t('prontuario.medidaPercentualGordura') as string,
        valor: this.avaliacaoRecente?.percentualGordura != null ? this.formatarPercentual(this.avaliacaoRecente.percentualGordura) : null,
      },
    ]
    return medidas.filter((medida): medida is { label: string; valor: string } => medida.valor !== null)
  }

  get auditoria(): string {
    const p = this.prontuario
    const criado = this.formatarData(p.criadoEm)
    const atualizado = this.formatarData(p.atualizadoEm)
    if (p.status === 'ASSINADO') {
      const assinadoEm = p.assinadoEm ? this.formatarData(p.assinadoEm) : atualizado
      return this.$t('prontuario.auditoriaAssinado', { autor: p.autorNome, criado, assinante: p.assinadoPorNome ?? p.autorNome, assinadoEm }) as string
    }
    if (p.status === 'PENDENTE') {
      return this.$t('prontuario.auditoriaPendente', { autor: p.autorNome, criado }) as string
    }
    return this.$t('prontuario.auditoriaRascunho', { autor: p.autorNome, criado, atualizado }) as string
  }

  statusColor(status: StatusProntuario): string {
    return STATUS_COLORS[status]
  }

  secaoChipColor(secao: SecaoProntuario): string {
    return secao === 'EXAMES' ? 'info' : 'on-surface-variant'
  }

  formatarData(iso: string): string {
    return new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })
  }

  formatarDataSemHora(data: string): string {
    const [ano, mes, dia] = data.split('-')
    return `${dia}/${mes}/${ano}`
  }

  formatarDataHora(iso: string): string {
    const data = new Date(iso)
    const dataLabel = data.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })
    const horaLabel = data.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
    return `${dataLabel} · ${horaLabel}`
  }

  formatarPeso(peso: number | null): string {
    return peso !== null ? `${peso.toFixed(1).replace('.', ',')} kg` : '—'
  }

  formatarAltura(altura: number | null): string {
    return altura !== null ? `${altura.toFixed(2).replace('.', ',')} m` : '—'
  }

  formatarPercentual(valor: number | null): string {
    return valor !== null ? `${valor.toFixed(1).replace('.', ',')}%` : '—'
  }

  confirmarAssinatura() {
    if (confirm(this.$t('prontuario.confirmarAssinatura') as string)) {
      this.$emit('assinar')
    }
  }

  salvarAdendo() {
    if (!this.textoAdendo.trim()) {
      return
    }
    this.$emit('criar-adendo', this.textoAdendo)
    this.mostrarFormAdendo = false
    this.textoAdendo = ''
  }

  cancelarAdendo() {
    this.mostrarFormAdendo = false
    this.textoAdendo = ''
  }

  formatarTamanho(bytes: number): string {
    if (bytes < 1024 * 1024) {
      return `${Math.round(bytes / 1024)} KB`
    }
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
  }

  iconeAnexo(tipoMime: string): string {
    if (tipoMime === 'application/pdf') return 'mdi-file-pdf-box'
    if (tipoMime.startsWith('image/')) return 'mdi-file-image'
    if (tipoMime.includes('word')) return 'mdi-file-word-box'
    if (tipoMime.includes('excel') || tipoMime.includes('spreadsheet')) return 'mdi-file-excel-box'
    return 'mdi-file-outline'
  }

  abrirSeletorArquivo() {
    ;(this.$refs.inputArquivo as HTMLInputElement).click()
  }

  onArquivoSelecionado(evento: Event) {
    const input = evento.target as HTMLInputElement
    const arquivo = input.files?.[0]
    input.value = ''
    if (arquivo) {
      this.$emit('upload-anexo', arquivo)
    }
  }
}
</script>

<style scoped lang="scss">
.prontuario-detalhe {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.prontuario-detalhe__corpo {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.prontuario-detalhe__timeline {
  flex: 1 1 260px;
  max-width: 300px;
  min-width: 0;
  align-self: flex-start;
  border-radius: 12px;
  overflow: hidden;
}

.prontuario-detalhe__timeline-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 16px 18px;
  border-bottom: 1px solid rgba(var(--v-theme-on-surface), 0.12);
}

.prontuario-detalhe__timeline-lista {
  display: flex;
  flex-direction: column;
  max-height: 480px;
  overflow-y: auto;
}

.prontuario-detalhe__timeline-titulo {
  font-size: 13.5px;
  font-weight: 600;
}

.prontuario-detalhe__timeline-total {
  font-size: 11.5px;
  color: rgb(var(--v-theme-on-surface-variant));
}

.prontuario-detalhe__timeline-item {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 13px 18px;
  border: none;
  border-left: 2px solid transparent;
  border-bottom: 1px solid rgba(var(--v-theme-on-surface), 0.08);
  background: transparent;
  cursor: pointer;
  text-align: left;
  font: inherit;
  color: inherit;
}

.prontuario-detalhe__timeline-item:hover {
  background: rgba(var(--v-theme-on-surface), 0.04);
}

.prontuario-detalhe__timeline-item--ativo {
  border-left-color: rgb(var(--v-theme-primary));
  background: rgba(var(--v-theme-primary), 0.08);
}

.prontuario-detalhe__timeline-item-topo {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
}

.prontuario-detalhe__timeline-status {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.prontuario-detalhe__timeline-status--success {
  background: rgb(var(--v-theme-success));
}

.prontuario-detalhe__timeline-status--warning {
  background: rgb(var(--v-theme-warning));
}

.prontuario-detalhe__timeline-secao {
  align-self: flex-start;
  font-size: 11.5px;
}

.prontuario-detalhe__timeline-autor {
  font-size: 11.5px;
  color: rgb(var(--v-theme-on-surface-variant));
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.prontuario-detalhe__nota {
  flex: 4 1 420px;
  min-width: 0;
  border-radius: 12px;
}

.prontuario-detalhe__nota-conteudo {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-top: 22px;
}

.prontuario-detalhe__nota-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  flex-wrap: wrap;
  padding-bottom: 18px;
  border-bottom: 1px solid rgba(var(--v-theme-on-surface), 0.12);
}

.prontuario-detalhe__nota-header-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.prontuario-detalhe__nota-header-acoes {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.prontuario-detalhe__nota-titulo-linha {
  display: flex;
  align-items: center;
  gap: 11px;
  flex-wrap: wrap;
}

.prontuario-detalhe__nota-titulo {
  margin: 0;
  font-size: 21px;
  font-weight: 600;
}

.prontuario-detalhe__nota-sub {
  font-size: 12.5px;
  color: rgb(var(--v-theme-on-surface-variant));
}

.prontuario-detalhe__anexo-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 600;
  color: rgb(var(--v-theme-on-surface-variant));
}

.prontuario-detalhe__meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px 24px;
}

.prontuario-detalhe__meta-campo {
  display: flex;
  flex-direction: column;
  gap: 4px;

  label {
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.04em;
    color: rgb(var(--v-theme-on-surface-variant));
  }

  span {
    font-size: 14px;
  }
}

.prontuario-detalhe__medidas {
  background: rgb(var(--v-theme-surface));
  border: 1px solid rgba(var(--v-theme-on-surface), 0.12);
  border-radius: 8px;
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.prontuario-detalhe__medidas-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.prontuario-detalhe__medidas-origem {
  display: flex;
  align-items: center;
  gap: 9px;
  font-size: 12.5px;
  color: rgb(var(--v-theme-on-surface-variant));
}

.prontuario-detalhe__medidas-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 14px;
}

.prontuario-detalhe__medida {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.prontuario-detalhe__medida-label {
  font-size: 10.5px;
  font-weight: 600;
  letter-spacing: 0.07em;
  text-transform: uppercase;
  color: rgb(var(--v-theme-on-surface-variant));
}

.prontuario-detalhe__medida-valor {
  font-size: 15px;
  font-weight: 600;
}

.prontuario-detalhe__nota-texto {
  display: flex;
  flex-direction: column;
  gap: 8px;

  label {
    font-size: 11.5px;
    font-weight: 700;
    letter-spacing: 0.09em;
    text-transform: uppercase;
    color: rgb(var(--v-theme-primary));
  }

  p {
    margin: 0;
    font-size: 14px;
    line-height: 1.65;
    color: rgba(var(--v-theme-on-surface), 0.87);
    white-space: pre-wrap;
  }
}

.prontuario-detalhe__nota-rodape {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
  padding-top: 18px;
  border-top: 1px solid rgba(var(--v-theme-on-surface), 0.12);
}

.prontuario-detalhe__nota-rodape-acoes {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.prontuario-detalhe__auditoria {
  font-size: 11.5px;
  color: rgb(var(--v-theme-on-surface-variant));
  line-height: 1.6;
}

.prontuario-detalhe__anexos {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-top: 4px;
  border-top: 1px solid rgba(var(--v-theme-on-surface), 0.12);
}

.prontuario-detalhe__anexos-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.prontuario-detalhe__anexos-titulo {
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: 0.09em;
  text-transform: uppercase;
  color: rgb(var(--v-theme-primary));
}

.prontuario-detalhe__anexo-input {
  display: none;
}

.prontuario-detalhe__anexo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid rgba(var(--v-theme-on-surface), 0.08);
}

.prontuario-detalhe__anexo-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  flex: 1;
}

.prontuario-detalhe__anexo-nome {
  font-size: 13.5px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.prontuario-detalhe__anexo-meta {
  font-size: 11.5px;
  color: rgb(var(--v-theme-on-surface-variant));
}

.prontuario-detalhe__anexo-acoes {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
}

.prontuario-detalhe__anexos-vazio {
  font-size: 12.5px;
  color: rgb(var(--v-theme-on-surface-variant));
  padding: 4px 0;
}

.prontuario-detalhe__adendos {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding-top: 4px;
  border-top: 1px solid rgba(var(--v-theme-on-surface), 0.12);
}

.prontuario-detalhe__adendos-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.prontuario-detalhe__adendos-titulo {
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: 0.09em;
  text-transform: uppercase;
  color: rgb(var(--v-theme-warning));
}

.prontuario-detalhe__adendo {
  background: rgb(var(--v-theme-surface));
  border-left: 2px solid rgb(var(--v-theme-warning));
  border-radius: 8px;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.prontuario-detalhe__adendo-cabecalho {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.prontuario-detalhe__adendo-autor {
  font-size: 12.5px;
  font-weight: 600;
}

.prontuario-detalhe__adendo-data {
  font-size: 11.5px;
  color: rgb(var(--v-theme-on-surface-variant));
}

.prontuario-detalhe__adendo-texto {
  margin: 0;
  font-size: 13.5px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.prontuario-detalhe__adendo-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.prontuario-detalhe__adendo-form-acoes {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.prontuario-detalhe__historico {
  padding-top: 4px;
  border-top: 1px solid rgba(var(--v-theme-on-surface), 0.12);
}

.prontuario-detalhe__historico-header {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 4px 0;
  border: none;
  background: transparent;
  cursor: pointer;
  font: inherit;
  font-weight: 600;
  font-size: 13.5px;
  color: inherit;
  text-align: left;
}

.prontuario-detalhe__historico-lista {
  display: flex;
  flex-direction: column;
  padding-top: 8px;
}

.prontuario-detalhe__historico-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 0;
  border-bottom: 1px solid rgba(var(--v-theme-on-surface), 0.08);
  font-size: 12.5px;
}

.prontuario-detalhe__historico-evento {
  font-weight: 600;
  flex-shrink: 0;
}

.prontuario-detalhe__historico-usuario {
  color: rgb(var(--v-theme-on-surface-variant));
  flex-shrink: 0;
}

.prontuario-detalhe__historico-data {
  color: rgb(var(--v-theme-on-surface-variant));
  flex-shrink: 0;
  white-space: nowrap;
}

.prontuario-detalhe__historico-resumo {
  color: rgb(var(--v-theme-on-surface-variant));
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

.prontuario-detalhe__historico-vazio {
  font-size: 12.5px;
  color: rgb(var(--v-theme-on-surface-variant));
  padding: 8px 0;
}
</style>
