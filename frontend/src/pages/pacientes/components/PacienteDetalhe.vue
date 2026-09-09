<template>
  <div class="paciente-detalhe">
    <div class="paciente-detalhe__header">
      <div class="paciente-detalhe__header-info">
        <v-btn icon="mdi-arrow-left" variant="text" :aria-label="$t('acao.voltar')" @click="$emit('voltar')" />
        <div class="paciente-detalhe__header-textos">
          <h1 class="paciente-detalhe__titulo">{{ paciente.nome }}</h1>
          <span class="paciente-detalhe__subtitulo">{{ subtituloHeader }}</span>
        </div>
      </div>
      <div class="paciente-detalhe__header-acoes">
        <v-btn variant="outlined" prepend-icon="mdi-file-document-outline" @click="$emit('abrir-prontuario')">
          {{ $t('paciente.detalhe.verProntuario') }}
        </v-btn>
        <v-btn color="primary" variant="flat" prepend-icon="mdi-plus" @click="$emit('nova-consulta')">
          {{ $t('paciente.detalhe.novaConsulta') }}
        </v-btn>
      </div>
    </div>

    <v-card variant="flat" color="surface-variant" class="paciente-detalhe__contexto">
      <v-card-text class="paciente-detalhe__contexto-bar">
        <div class="paciente-detalhe__contexto-identidade">
          <v-avatar color="primary" variant="tonal" size="44">
            <span class="paciente-detalhe__iniciais">{{ iniciais }}</span>
          </v-avatar>
          <div class="paciente-detalhe__contexto-objetivo">
            <span class="paciente-detalhe__contexto-label">{{ $t('paciente.objetivo') }}</span>
            <span class="paciente-detalhe__contexto-valor">{{ objetivoLabel }}</span>
          </div>
        </div>

        <v-divider vertical class="paciente-detalhe__contexto-divisor" />

        <div class="paciente-detalhe__contexto-metricas">
          <div v-for="vital in vitaisContexto" :key="vital.label" class="paciente-detalhe__vital">
            <span class="paciente-detalhe__vital-label">{{ vital.label }}</span>
            <div class="paciente-detalhe__vital-linha">
              <span class="paciente-detalhe__vital-valor">{{ vital.valor }}</span>
              <span v-if="vital.delta" class="paciente-detalhe__vital-delta">{{ vital.delta }}</span>
            </div>
          </div>
        </div>

        <!-- Chips de alerta clínico (alergia/comorbidade): depende de Dados Clínicos (Passo 4),
             ainda não existe no backend. Fica pronto para popular via prop assim que existir —
             por isso sem margin-left:auto (mesmo defeito já corrigido na timeline do Prontuário):
             o grupo de métricas acima é quem ocupa o espaço com flex:1, os chips só ficam soltos
             ao lado, sem reservar vazio quando quebram de linha. -->
        <div v-if="alertasClinicos.length > 0" class="paciente-detalhe__contexto-chips">
          <span
            v-for="alerta in alertasClinicos"
            :key="alerta.label"
            class="paciente-detalhe__chip"
            :class="`paciente-detalhe__chip--${alerta.cor}`"
          >
            <v-icon icon="mdi-alert-circle-outline" size="13" />
            {{ alerta.label }}
          </span>
        </div>
      </v-card-text>
    </v-card>

    <div class="paciente-detalhe__abas">
      <button
        v-for="tab in abasDisponiveis"
        :key="tab.chave"
        type="button"
        class="paciente-detalhe__aba"
        :class="{ 'paciente-detalhe__aba--ativa': aba === tab.chave }"
        @click="aba = tab.chave"
      >
        {{ tab.label }}
        <span v-if="tab.contagem !== undefined" class="paciente-detalhe__aba-contagem">{{ tab.contagem }}</span>
      </button>
    </div>

    <!-- ABA: VISÃO GERAL -->
    <div v-if="aba === 'visao'" class="paciente-detalhe__grid">
      <v-card variant="flat" color="surface-variant" class="paciente-detalhe__card">
        <v-card-text class="paciente-detalhe__card-conteudo">
          <div class="paciente-detalhe__card-header">
            <span class="paciente-detalhe__card-titulo">{{ $t('paciente.detalhe.indicadores') }}</span>
            <span class="paciente-detalhe__card-hint">{{ $t('paciente.detalhe.indicadoresHint') }}</span>
            <div class="paciente-detalhe__card-regua" />
          </div>

          <template v-if="perfil">
            <div class="paciente-detalhe__indicadores-grid">
              <div v-for="ind in indicadores" :key="ind.label" class="paciente-detalhe__indicador">
                <span class="paciente-detalhe__indicador-label">{{ ind.label }}</span>
                <div class="paciente-detalhe__indicador-linha">
                  <span class="paciente-detalhe__indicador-valor">{{ ind.valor }}</span>
                  <span v-if="ind.unidade" class="paciente-detalhe__indicador-unidade">{{ ind.unidade }}</span>
                </div>
                <span class="paciente-detalhe__indicador-nota" :class="{ 'paciente-detalhe__indicador-nota--destaque': ind.destaque }">
                  {{ ind.nota }}
                </span>
              </div>
            </div>

            <div v-if="avaliacaoRecente && perfil.peso !== null" class="paciente-detalhe__nota-origem">
              <v-icon icon="mdi-scale-bathroom" size="15" color="primary" />
              <span class="paciente-detalhe__nota-origem-texto">{{ textoOrigemPeso }}</span>
              <a href="#" @click.prevent="$emit('nova-avaliacao')">{{ $t('paciente.detalhe.novaAvaliacao') }}</a>
            </div>
          </template>
          <span v-else class="paciente-detalhe__vazio">{{ $t('paciente.detalhe.semAvaliacao') }}</span>
        </v-card-text>
      </v-card>

      <v-card variant="flat" color="surface-variant" class="paciente-detalhe__card">
        <v-card-text class="paciente-detalhe__card-conteudo">
          <div class="paciente-detalhe__card-header">
            <span class="paciente-detalhe__card-titulo">{{ $t('paciente.detalhe.evolucaoPeso') }}</span>
            <div class="paciente-detalhe__card-regua" />
            <a href="#" @click.prevent="$emit('ver-avaliacoes')">{{ $t('paciente.detalhe.verTudo') }}</a>
          </div>

          <NuvexaBarChart v-if="evolucaoPesoRows.length > 0" :rows="evolucaoPesoRows" />
          <span v-else class="paciente-detalhe__vazio">{{ $t('paciente.detalhe.semEvolucaoPeso') }}</span>
        </v-card-text>
      </v-card>

      <v-card variant="flat" color="surface-variant" class="paciente-detalhe__card">
        <v-card-text class="paciente-detalhe__card-conteudo">
          <div class="paciente-detalhe__card-header">
            <span class="paciente-detalhe__card-titulo">{{ $t('paciente.detalhe.dadosClinicos') }}</span>
            <div class="paciente-detalhe__card-regua" />
          </div>
          <!-- Depende de DadosClinicos (Passo 4 do backend), ainda não implementado — card fica
               presente com estado vazio, sem inventar alergias/comorbidades/medicamentos. -->
          <span class="paciente-detalhe__vazio">{{ $t('paciente.detalhe.semDadosClinicos') }}</span>
        </v-card-text>
      </v-card>

      <v-card variant="flat" color="surface-variant" class="paciente-detalhe__card">
        <v-card-text class="paciente-detalhe__card-conteudo">
          <div class="paciente-detalhe__card-header">
            <span class="paciente-detalhe__card-titulo">{{ $t('paciente.detalhe.planoVigente') }}</span>
            <div class="paciente-detalhe__card-regua" />
            <a v-if="planoAtivo" href="#" @click.prevent="$emit('abrir-plano', planoAtivo.id)">
              {{ $t('paciente.detalhe.abrirPlano') }}
            </a>
          </div>

          <template v-if="planoAtivo">
            <div class="paciente-detalhe__plano-titulo">
              <v-icon icon="mdi-food-apple-outline" size="18" color="primary" />
              <div class="paciente-detalhe__plano-titulo-textos">
                <span class="paciente-detalhe__plano-nome">{{ planoAtivo.nome }}</span>
                <span class="paciente-detalhe__plano-desde">{{ $t('paciente.detalhe.acompanhamentoDesde', { data: formatarDataSemHora(planoAtivo.dataInicio) }) }}</span>
              </div>
            </div>
            <div class="paciente-detalhe__plano-campos">
              <div class="paciente-detalhe__plano-campo">
                <span class="paciente-detalhe__indicador-label">{{ $t('planoAlimentar.calorias') }}</span>
                <span class="paciente-detalhe__plano-valor">{{ planoAtivo.calorias }} kcal</span>
              </div>
              <div class="paciente-detalhe__plano-campo">
                <span class="paciente-detalhe__indicador-label">{{ $t('planoAlimentar.refeicoesPorDia') }}</span>
                <span class="paciente-detalhe__plano-valor">{{ planoAtivo.refeicoesPorDia }}</span>
              </div>
            </div>
            <div v-if="proximaConsulta" class="paciente-detalhe__proxima-consulta">
              <v-icon icon="mdi-calendar-outline" size="15" color="info" />
              <span>{{ $t('paciente.detalhe.proximaConsulta', { data: formatarDataHora(proximaConsulta.dataHora) }) }}</span>
            </div>
          </template>
          <span v-else class="paciente-detalhe__vazio">{{ $t('paciente.detalhe.semPlanoAtivo') }}</span>
        </v-card-text>
      </v-card>
    </div>

    <!-- ABA: FICHA -->
    <div v-else-if="aba === 'ficha'" class="paciente-detalhe__ficha">
      <v-card v-for="grupo in fichaGrupos" :key="grupo.titulo" variant="flat" color="surface-variant" class="paciente-detalhe__card">
        <v-card-text class="paciente-detalhe__card-conteudo">
          <div class="paciente-detalhe__card-header">
            <span class="paciente-detalhe__card-titulo">{{ grupo.titulo }}</span>
            <span class="paciente-detalhe__card-hint">{{ grupo.hint }}</span>
            <div class="paciente-detalhe__card-regua" />
          </div>
          <div class="paciente-detalhe__ficha-grid">
            <div v-for="campo in grupo.campos" :key="campo.label" class="paciente-detalhe__ficha-campo" :class="{ 'paciente-detalhe__ficha-campo--largo': campo.largo }">
              <label>{{ campo.label }}</label>
              <div class="paciente-detalhe__ficha-valor-linha">
                <span :class="{ 'paciente-detalhe__vazio': campo.vazio }">{{ campo.valor }}</span>
              </div>
              <span v-if="campo.hint" class="paciente-detalhe__ficha-campo-hint">{{ campo.hint }}</span>
            </div>
          </div>
        </v-card-text>
      </v-card>
    </div>

    <!-- ABA: HISTÓRICOS -->
    <div v-else class="paciente-detalhe__historicos">
      <v-card v-for="bloco in historicosBlocos" :key="bloco.titulo" variant="flat" color="surface-variant" class="paciente-detalhe__card">
        <v-card-text class="paciente-detalhe__card-conteudo">
          <div class="paciente-detalhe__card-header">
            <v-icon :icon="bloco.icone" size="16" color="primary" />
            <span class="paciente-detalhe__card-titulo">{{ bloco.titulo }}</span>
            <span class="paciente-detalhe__bloco-contagem">{{ bloco.itens.length }}</span>
            <div class="paciente-detalhe__card-regua" />
            <a href="#" @click.prevent="bloco.onCriar">{{ bloco.acaoCriar }}</a>
          </div>
          <div class="paciente-detalhe__bloco-lista">
            <button
              v-for="item in bloco.itens"
              :key="item.id"
              type="button"
              class="paciente-detalhe__bloco-item"
              @click="item.onClick"
            >
              <span class="paciente-detalhe__bloco-item-data">{{ item.data }}</span>
              <span class="paciente-detalhe__bloco-item-desc">{{ item.descricao }}</span>
              <NuvexaStatusChip :label="item.statusLabel" :color="item.statusColor" />
            </button>
            <span v-if="bloco.itens.length === 0" class="paciente-detalhe__vazio">{{ $t('paciente.detalhe.semRegistros') }}</span>
          </div>
        </v-card-text>
      </v-card>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import NuvexaStatusChip from '../../../components/common/NuvexaStatusChip.vue'
import NuvexaBarChart from '../../../components/common/NuvexaBarChart.vue'
import type { NuvexaBarChartRow } from '../../../components/common/NuvexaBarChart.vue'
import type { PacienteResponse } from '../../../types/paciente'
import type { PerfilNutricionalResponse } from '../../../nutricao/types/perfil-nutricional'
import type { Avaliacao } from '../../../nutricao/types/avaliacao'
import type { Consulta, ConsultaStatus } from '../../../types/consulta'
import type { PlanoAlimentar, StatusPlanoAlimentar } from '../../../nutricao/types/plano-alimentar'
import type { Prontuario, StatusProntuario } from '../../../types/prontuario'
import { consultaStatusColor } from '../../../util/consulta-status'

export interface AlertaClinico {
  label: string
  cor: string
}

const STATUS_AVALIACAO_COLORS: Record<string, string> = {
  CONCLUIDA: 'success',
  AGENDADA: 'on-surface-variant',
}

const STATUS_PLANO_COLORS: Record<StatusPlanoAlimentar, string> = {
  ATIVO: 'success',
  RASCUNHO: 'warning',
  ENCERRADO: 'on-surface-variant',
}

const STATUS_PRONTUARIO_COLORS: Record<StatusProntuario, string> = {
  RASCUNHO: 'warning',
  PENDENTE: 'warning',
  ASSINADO: 'success',
}

interface Vital {
  label: string
  valor: string
  delta?: string
}

interface Indicador {
  label: string
  valor: string
  unidade: string
  nota: string
  destaque: boolean
}

interface FichaCampo {
  label: string
  valor: string
  vazio: boolean
  hint?: string
  largo?: boolean
}

interface FichaGrupo {
  titulo: string
  hint: string
  campos: FichaCampo[]
}

interface BlocoItem {
  id: number
  data: string
  descricao: string
  statusLabel: string
  statusColor: string
  onClick: () => void
}

interface Bloco {
  titulo: string
  icone: string
  acaoCriar: string
  onCriar: () => void
  itens: BlocoItem[]
}

@Component({
  name: 'PacienteDetalhe',
  components: { NuvexaStatusChip, NuvexaBarChart },
  emits: [
    'voltar',
    'abrir-prontuario',
    'nova-consulta',
    'abrir-consulta',
    'nova-avaliacao',
    'abrir-avaliacao',
    'ver-avaliacoes',
    'novo-plano',
    'abrir-plano',
    'novo-registro',
    'abrir-registro',
  ],
})
export default class PacienteDetalhe extends Vue {
  @Prop({ required: true })
  paciente!: PacienteResponse

  @Prop({ default: null })
  perfil!: PerfilNutricionalResponse | null

  @Prop({ default: null })
  avaliacaoRecente!: Avaliacao | null

  @Prop({ default: () => [] })
  avaliacoes!: Avaliacao[]

  @Prop({ default: () => [] })
  consultas!: Consulta[]

  @Prop({ default: () => [] })
  planos!: PlanoAlimentar[]

  @Prop({ default: () => [] })
  prontuarios!: Prontuario[]

  @Prop({ default: () => [] })
  alertasClinicos!: AlertaClinico[]

  @Prop({ default: () => ({}) })
  rotulosEnum!: Record<string, string>

  @Prop({ default: () => ({}) })
  rotulosAvaliacao!: Record<string, string>

  @Prop({ default: () => ({}) })
  rotulosProntuario!: Record<string, string>

  @Prop({ default: () => ({}) })
  rotulosPlanoAlimentar!: Record<string, string>

  aba: 'visao' | 'ficha' | 'historicos' = 'visao'

  get iniciais(): string {
    return this.paciente.nome
      .trim()
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((parte) => parte[0])
      .join('')
      .toUpperCase()
  }

  get subtituloHeader(): string {
    const sexo = this.rotulosEnum[this.paciente.sexo] ?? this.paciente.sexo
    const desde = this.$t('paciente.detalhe.acompanhamentoDesde', { data: this.formatarMesAno(this.paciente.criadoEm) }) as string
    return `${this.paciente.idade} ${this.$t('paciente.detalhe.anos')} · ${sexo} · ${desde}`
  }

  get objetivoLabel(): string {
    if (!this.perfil) {
      return '—'
    }
    return this.rotulosEnum[this.perfil.objetivo] ?? this.perfil.objetivo
  }

  get deltaPeso(): string | undefined {
    const variacao = this.avaliacaoRecente?.variacaoPeso
    if (variacao === null || variacao === undefined) {
      return undefined
    }
    const sinal = variacao > 0 ? '+' : ''
    return `${sinal}${variacao.toFixed(1).replace('.', ',')} kg`
  }

  get consultasPassadas(): Consulta[] {
    const agora = Date.now()
    return this.consultas.filter((c) => new Date(c.dataHora).getTime() <= agora).sort((a, b) => new Date(b.dataHora).getTime() - new Date(a.dataHora).getTime())
  }

  get proximaConsulta(): Consulta | null {
    const agora = Date.now()
    const futuras = this.consultas
      .filter((c) => new Date(c.dataHora).getTime() > agora && (c.status === 'AGENDADA' || c.status === 'CONFIRMADA'))
      .sort((a, b) => new Date(a.dataHora).getTime() - new Date(b.dataHora).getTime())
    return futuras[0] ?? null
  }

  get vitaisContexto(): Vital[] {
    const ultima = this.consultasPassadas[0]
    return [
      {
        label: this.$t('paciente.detalhe.pesoAtual') as string,
        valor: this.perfil?.peso != null ? this.formatarPeso(this.perfil.peso) : '—',
        delta: this.deltaPeso,
      },
      { label: this.$t('paciente.altura') as string, valor: this.perfil ? `${this.perfil.altura.toFixed(2).replace('.', ',')} m` : '—' },
      { label: this.$t('paciente.imc') as string, valor: this.perfil?.imc != null ? this.perfil.imc.toFixed(1) : '—' },
      { label: this.$t('paciente.detalhe.metricaConsultas') as string, valor: String(this.consultas.length) },
      { label: this.$t('paciente.detalhe.metricaUltima') as string, valor: ultima ? this.formatarDataCurta(ultima.dataHora) : '—' },
    ]
  }

  get indicadores(): Indicador[] {
    const perfil = this.perfil as PerfilNutricionalResponse
    return [
      {
        label: this.$t('paciente.imc') as string,
        valor: perfil.imc != null ? perfil.imc.toFixed(1) : '—',
        unidade: '',
        nota: perfil.classificacaoImc ?? '',
        destaque: true,
      },
      {
        label: this.$t('paciente.detalhe.taxaMetabolicaBasal') as string,
        valor: perfil.taxaMetabolicaBasal != null ? String(Math.round(perfil.taxaMetabolicaBasal)) : '—',
        unidade: 'kcal',
        nota: this.$t('paciente.detalhe.notaFormulaTmb') as string,
        destaque: false,
      },
      {
        label: this.$t('paciente.detalhe.gastoCaloricoTotal') as string,
        valor: perfil.gastoCaloricoDiario != null ? String(Math.round(perfil.gastoCaloricoDiario)) : '—',
        unidade: 'kcal',
        nota: this.rotulosEnum[perfil.nivelAtividade] ?? perfil.nivelAtividade,
        destaque: false,
      },
    ]
  }

  get textoOrigemPeso(): string {
    const peso = this.perfil?.peso
    const avaliacao = this.avaliacaoRecente
    if (peso == null || !avaliacao) {
      return ''
    }
    return this.$t('paciente.detalhe.notaOrigemPeso', { peso: this.formatarPeso(peso), data: this.formatarDataSemHora(avaliacao.data) }) as string
  }

  get evolucaoPesoRows(): NuvexaBarChartRow[] {
    return this.avaliacoes
      .filter((a) => a.status === 'CONCLUIDA' && a.peso != null)
      .sort((a, b) => new Date(b.data).getTime() - new Date(a.data).getTime())
      .slice(0, 8)
      .map((a) => ({ label: this.formatarDataSemHora(a.data), value: a.peso as number, color: 'primary' }))
  }

  get planoAtivo(): PlanoAlimentar | null {
    return this.planos.find((p) => p.status === 'ATIVO') ?? null
  }

  get fichaGrupos(): FichaGrupo[] {
    const perfil = this.perfil
    const naoInformado = this.$t('paciente.detalhe.campoNaoInformado') as string
    return [
      {
        titulo: this.$t('paciente.detalhe.grupoIdentificacao') as string,
        hint: this.$t('paciente.detalhe.grupoIdentificacaoHint') as string,
        campos: [
          { label: this.$t('paciente.nome') as string, valor: this.paciente.nome, vazio: false },
          { label: this.$t('paciente.dataNascimento') as string, valor: this.formatarDataSemHora(this.paciente.dataNascimento), vazio: false },
          { label: this.$t('paciente.sexo') as string, valor: this.rotulosEnum[this.paciente.sexo] ?? this.paciente.sexo, vazio: false },
        ],
      },
      {
        titulo: this.$t('paciente.detalhe.grupoPerfilNutricional') as string,
        hint: this.$t('paciente.detalhe.grupoPerfilNutricionalHint') as string,
        campos: perfil
          ? [
              { label: this.$t('paciente.altura') as string, valor: `${perfil.altura.toFixed(2).replace('.', ',')} m`, vazio: false },
              { label: this.$t('paciente.objetivo') as string, valor: this.rotulosEnum[perfil.objetivo] ?? perfil.objetivo, vazio: false },
              { label: this.$t('paciente.nivelAtividade') as string, valor: this.rotulosEnum[perfil.nivelAtividade] ?? perfil.nivelAtividade, vazio: false },
              {
                label: this.$t('paciente.gastoCaloricoManual') as string,
                valor: perfil.caloriasDiariasManuais != null ? `${perfil.caloriasDiariasManuais} kcal` : naoInformado,
                vazio: perfil.caloriasDiariasManuais == null,
                hint: this.$t('paciente.detalhe.sobrepoeCalculoHint') as string,
              },
              {
                label: this.$t('paciente.peso') as string,
                valor:
                  perfil.peso != null && this.avaliacaoRecente
                    ? (this.$t('paciente.detalhe.pesoMedidoEm', {
                        peso: this.formatarPeso(perfil.peso),
                        data: this.formatarDataSemHora(this.avaliacaoRecente.data),
                      }) as string)
                    : naoInformado,
                vazio: perfil.peso == null,
                hint: this.$t('paciente.detalhe.pesoSomenteLeituraHint') as string,
                largo: true,
              },
              {
                label: this.$t('paciente.observacoes') as string,
                valor: perfil.observacoes ?? naoInformado,
                vazio: !perfil.observacoes,
                largo: true,
              },
            ]
          : [],
      },
    ]
  }

  get historicosBlocos(): Bloco[] {
    return [
      {
        titulo: this.$t('paciente.detalhe.blocoConsultas') as string,
        icone: 'mdi-calendar-outline',
        acaoCriar: this.$t('paciente.detalhe.novaConsulta') as string,
        onCriar: () => this.$emit('nova-consulta'),
        itens: [...this.consultas]
          .sort((a, b) => new Date(b.dataHora).getTime() - new Date(a.dataHora).getTime())
          .map((c) => ({
            id: c.id,
            data: this.formatarDataCurta(c.dataHora),
            descricao: `${this.$t('consulta.tipo.' + c.tipo)} · ${c.duracaoMinutos} min · ${c.profissionalNome}`,
            statusLabel: this.$t('consulta.status.' + c.status) as string,
            statusColor: consultaStatusColor(c.status as ConsultaStatus),
            onClick: () => this.$emit('abrir-consulta', c.id),
          })),
      },
      {
        titulo: this.$t('paciente.detalhe.blocoAvaliacoes') as string,
        icone: 'mdi-scale-bathroom',
        acaoCriar: this.$t('paciente.detalhe.novaAvaliacao') as string,
        onCriar: () => this.$emit('nova-avaliacao'),
        itens: [...this.avaliacoes]
          .sort((a, b) => new Date(b.data).getTime() - new Date(a.data).getTime())
          .map((av) => ({
            id: av.id,
            data: this.formatarDataSemHora(av.data),
            descricao: this.descricaoAvaliacao(av),
            statusLabel: this.rotulosAvaliacao[av.status] ?? av.status,
            statusColor: STATUS_AVALIACAO_COLORS[av.status] ?? 'on-surface-variant',
            onClick: () => this.$emit('abrir-avaliacao', av.id),
          })),
      },
      {
        titulo: this.$t('paciente.detalhe.blocoPlanos') as string,
        icone: 'mdi-food-apple-outline',
        acaoCriar: this.$t('paciente.detalhe.novoPlano') as string,
        onCriar: () => this.$emit('novo-plano'),
        itens: [...this.planos]
          .sort((a, b) => new Date(b.dataInicio).getTime() - new Date(a.dataInicio).getTime())
          .map((p) => ({
            id: p.id,
            data: this.formatarDataSemHora(p.dataInicio),
            descricao: `${p.nome} · ${p.calorias} kcal`,
            statusLabel: this.rotulosPlanoAlimentar[p.status] ?? p.status,
            statusColor: STATUS_PLANO_COLORS[p.status],
            onClick: () => this.$emit('abrir-plano', p.id),
          })),
      },
      {
        titulo: this.$t('paciente.detalhe.blocoProntuarios') as string,
        icone: 'mdi-file-document-outline',
        acaoCriar: this.$t('paciente.detalhe.novoRegistro') as string,
        onCriar: () => this.$emit('novo-registro'),
        itens: [...this.prontuarios]
          .sort((a, b) => new Date(b.criadoEm).getTime() - new Date(a.criadoEm).getTime())
          .map((p) => ({
            id: p.id,
            data: this.formatarDataSemHora(p.criadoEm.slice(0, 10)),
            descricao: `${this.rotulosProntuario[p.secao] ?? p.secao} · ${p.autorNome}`,
            statusLabel: this.rotulosProntuario[p.status] ?? p.status,
            statusColor: STATUS_PRONTUARIO_COLORS[p.status],
            onClick: () => this.$emit('abrir-registro', p.id),
          })),
      },
    ]
  }

  get abasDisponiveis(): { chave: 'visao' | 'ficha' | 'historicos'; label: string; contagem?: number }[] {
    const totalHistoricos = this.consultas.length + this.avaliacoes.length + this.planos.length + this.prontuarios.length
    return [
      { chave: 'visao', label: this.$t('paciente.detalhe.abaVisaoGeral') as string },
      { chave: 'ficha', label: this.$t('paciente.detalhe.abaFicha') as string },
      { chave: 'historicos', label: this.$t('paciente.detalhe.abaHistoricos') as string, contagem: totalHistoricos },
    ]
  }

  descricaoAvaliacao(av: Avaliacao): string {
    const tipo = this.rotulosAvaliacao[av.tipo] ?? av.tipo
    const partes = [tipo]
    if (av.peso != null) {
      partes.push(this.formatarPeso(av.peso))
    }
    if (av.percentualGordura != null) {
      partes.push(`${av.percentualGordura.toFixed(1).replace('.', ',')}% gordura`)
    }
    return partes.join(' · ')
  }

  formatarPeso(peso: number): string {
    return `${peso.toFixed(1).replace('.', ',')} kg`
  }

  formatarDataCurta(iso: string): string {
    const data = new Date(iso)
    return data.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' })
  }

  formatarDataSemHora(data: string): string {
    const [ano, mes, dia] = data.slice(0, 10).split('-')
    return `${dia}/${mes}/${ano}`
  }

  formatarDataHora(iso: string): string {
    const data = new Date(iso)
    const dataLabel = data.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })
    const horaLabel = data.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
    return `${dataLabel} às ${horaLabel}`
  }

  formatarMesAno(iso: string): string {
    const data = new Date(iso)
    const mes = data.toLocaleDateString('pt-BR', { month: 'short' }).replace('.', '')
    return `${mes}/${data.getFullYear()}`
  }
}
</script>

<style scoped lang="scss">
.paciente-detalhe {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.paciente-detalhe__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  flex-wrap: wrap;
}

.paciente-detalhe__header-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.paciente-detalhe__header-textos {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.paciente-detalhe__titulo {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.paciente-detalhe__subtitulo {
  font-size: 13.5px;
  color: rgb(var(--v-theme-on-surface-variant));
}

.paciente-detalhe__header-acoes {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.paciente-detalhe__contexto {
  border-radius: 12px;
  border-left: 2px solid rgb(var(--v-theme-primary));
}

.paciente-detalhe__contexto-bar {
  display: flex;
  align-items: center;
  gap: 26px;
  flex-wrap: wrap;
}

.paciente-detalhe__contexto-identidade {
  display: flex;
  align-items: center;
  gap: 13px;
}

.paciente-detalhe__iniciais {
  font-size: 14px;
  font-weight: 700;
}

.paciente-detalhe__contexto-objetivo {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.paciente-detalhe__contexto-label {
  font-size: 12.5px;
  color: rgb(var(--v-theme-on-surface-variant));
}

.paciente-detalhe__contexto-valor {
  font-size: 15px;
  font-weight: 600;
}

.paciente-detalhe__contexto-divisor {
  align-self: stretch;
  min-height: 40px;
}

.paciente-detalhe__contexto-metricas {
  display: flex;
  flex: 1;
  gap: 26px;
  flex-wrap: wrap;
}

.paciente-detalhe__vital {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.paciente-detalhe__vital-label {
  font-size: 10.5px;
  font-weight: 600;
  letter-spacing: 0.07em;
  text-transform: uppercase;
  color: rgb(var(--v-theme-on-surface-variant));
  white-space: nowrap;
}

.paciente-detalhe__vital-linha {
  display: flex;
  align-items: baseline;
  gap: 6px;
  white-space: nowrap;
}

.paciente-detalhe__vital-valor {
  font-size: 16px;
  font-weight: 600;
}

.paciente-detalhe__vital-delta {
  font-size: 11.5px;
  font-weight: 600;
  color: rgb(var(--v-theme-on-surface-variant));
}

.paciente-detalhe__contexto-chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}

.paciente-detalhe__chip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 999px;
  padding: 6px 12px;
  white-space: nowrap;
}

.paciente-detalhe__chip--error {
  color: rgb(var(--v-theme-error));
  background: rgba(var(--v-theme-error), 0.09);
  border: 1px solid rgba(var(--v-theme-error), 0.3);
}

.paciente-detalhe__chip--warning {
  color: rgb(var(--v-theme-warning));
  background: rgba(var(--v-theme-warning), 0.09);
  border: 1px solid rgba(var(--v-theme-warning), 0.28);
}

.paciente-detalhe__abas {
  display: flex;
  gap: 26px;
  border-bottom: 1px solid rgba(var(--v-theme-on-surface), 0.12);
  flex-wrap: wrap;
}

.paciente-detalhe__aba {
  font: inherit;
  font-size: 13.5px;
  font-weight: 500;
  color: rgb(var(--v-theme-on-surface-variant));
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  padding: 0 2px 12px;
  margin-bottom: -1px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.paciente-detalhe__aba--ativa {
  font-weight: 600;
  color: rgb(var(--v-theme-primary));
  border-bottom-color: rgb(var(--v-theme-primary));
}

.paciente-detalhe__aba-contagem {
  font-size: 11.5px;
  font-weight: 600;
  border-radius: 999px;
  padding: 2px 8px;
  background: rgba(var(--v-theme-on-surface), 0.08);
}

.paciente-detalhe__aba--ativa .paciente-detalhe__aba-contagem {
  color: rgb(var(--v-theme-primary));
  background: rgba(var(--v-theme-primary), 0.12);
}

.paciente-detalhe__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
  align-items: start;
}

.paciente-detalhe__ficha,
.paciente-detalhe__historicos {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.paciente-detalhe__card {
  border-radius: 12px;
}

.paciente-detalhe__card-conteudo {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding-top: 20px;
}

.paciente-detalhe__card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.paciente-detalhe__card-titulo {
  font-size: 11.5px;
  font-weight: 700;
  color: rgb(var(--v-theme-primary));
  letter-spacing: 0.09em;
  text-transform: uppercase;
  white-space: nowrap;
}

.paciente-detalhe__card-hint {
  font-size: 11.5px;
  color: rgb(var(--v-theme-on-surface-variant));
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.paciente-detalhe__card-regua {
  flex: 1;
  height: 1px;
  background: rgba(var(--v-theme-on-surface), 0.12);
  min-width: 20px;
}

.paciente-detalhe__vazio {
  font-size: 12.5px;
  color: rgb(var(--v-theme-on-surface-variant));
}

.paciente-detalhe__indicadores-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(96px, 1fr));
  gap: 16px;
}

.paciente-detalhe__indicador {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.paciente-detalhe__indicador-label {
  font-size: 10.5px;
  font-weight: 600;
  letter-spacing: 0.07em;
  text-transform: uppercase;
  color: rgb(var(--v-theme-on-surface-variant));
  /* altura mínima reservada: rótulo de 1 ou 2 linhas, o número embaixo sempre começa na mesma linha de base */
  min-height: 26px;
  line-height: 1.25;
}

.paciente-detalhe__indicador-linha {
  display: flex;
  align-items: baseline;
  gap: 5px;
}

.paciente-detalhe__indicador-valor {
  font-size: 22px;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.paciente-detalhe__indicador-unidade {
  font-size: 11.5px;
  color: rgb(var(--v-theme-on-surface-variant));
}

.paciente-detalhe__indicador-nota {
  font-size: 11.5px;
  color: rgb(var(--v-theme-on-surface-variant));
}

.paciente-detalhe__indicador-nota--destaque {
  color: rgb(var(--v-theme-success));
}

.paciente-detalhe__nota-origem {
  display: flex;
  align-items: center;
  gap: 11px;
  background: rgb(var(--v-theme-surface));
  border: 1px solid rgba(var(--v-theme-on-surface), 0.12);
  border-radius: 8px;
  padding: 13px 15px;
}

.paciente-detalhe__nota-origem-texto {
  font-size: 12.5px;
  color: rgb(var(--v-theme-on-surface-variant));
  line-height: 1.5;
  flex: 1;
  min-width: 0;
}

.paciente-detalhe__plano-titulo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.paciente-detalhe__plano-titulo-textos {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.paciente-detalhe__plano-nome {
  font-size: 15px;
  font-weight: 600;
}

.paciente-detalhe__plano-desde {
  font-size: 11.5px;
  color: rgb(var(--v-theme-on-surface-variant));
}

.paciente-detalhe__plano-campos {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.paciente-detalhe__plano-campo {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.paciente-detalhe__plano-valor {
  font-size: 14px;
  font-weight: 600;
}

.paciente-detalhe__proxima-consulta {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgb(var(--v-theme-surface));
  border: 1px solid rgba(var(--v-theme-on-surface), 0.12);
  border-radius: 8px;
  padding: 12px 14px;
  font-size: 12.5px;
  color: rgb(var(--v-theme-on-surface-variant));
}

.paciente-detalhe__ficha-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 22px 26px;
}

.paciente-detalhe__ficha-campo {
  display: flex;
  flex-direction: column;
  gap: 4px;

  label {
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.04em;
    color: rgb(var(--v-theme-on-surface-variant));
  }
}

.paciente-detalhe__ficha-campo--largo {
  grid-column: 1 / -1;
}

.paciente-detalhe__ficha-valor-linha {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  min-height: 34px;
  border-bottom: 1px solid rgba(var(--v-theme-on-surface), 0.12);
  font-size: 14px;
}

.paciente-detalhe__ficha-campo-hint {
  font-size: 11.5px;
  color: rgb(var(--v-theme-on-surface-variant));
}

.paciente-detalhe__bloco-contagem {
  font-size: 11.5px;
  font-weight: 600;
  color: rgb(var(--v-theme-on-surface-variant));
  background: rgba(var(--v-theme-on-surface), 0.08);
  border-radius: 999px;
  padding: 3px 9px;
}

.paciente-detalhe__bloco-lista {
  display: flex;
  flex-direction: column;
}

.paciente-detalhe__bloco-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  border: none;
  border-bottom: 1px solid rgba(var(--v-theme-on-surface), 0.08);
  background: transparent;
  cursor: pointer;
  font: inherit;
  color: inherit;
  text-align: left;
  flex-wrap: wrap;
}

.paciente-detalhe__bloco-item:hover {
  background: rgba(var(--v-theme-on-surface), 0.04);
}

.paciente-detalhe__bloco-item-data {
  font-size: 13px;
  font-weight: 600;
  width: 92px;
  flex-shrink: 0;
}

.paciente-detalhe__bloco-item-desc {
  font-size: 13px;
  color: rgb(var(--v-theme-on-surface-variant));
  flex: 1;
  min-width: 120px;
}
</style>
