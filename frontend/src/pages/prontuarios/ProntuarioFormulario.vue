<template>
  <div class="prontuario-formulario">
    <template v-if="isView">
      <p v-if="carregando">...</p>
      <ProntuarioDetalhe
        v-else-if="prontuario"
        :prontuario="prontuario"
        :historico="historico"
        :perfil="perfil"
        :avaliacao-recente="avaliacaoRecente"
        :plano-ativo="planoAtivo"
        :rotulos-prontuario="rotulosProntuario"
        :rotulos-enum="rotulosEnum"
        :rotulos-avaliacao="rotulosAvaliacao"
        :assinando="assinando"
        :adendos="adendos"
        :anexos="anexos"
        :enviando-anexo="enviandoAnexo"
        :eventos-auditoria="eventosAuditoria"
        :criando-adendo="criandoAdendo"
        @voltar="voltar"
        @navegar="irParaProntuario"
        @editar="irParaEdicao"
        @assinar="assinar"
        @descartar="descartar"
        @abrir-avaliacao="irParaAvaliacao"
        @abrir-plano-alimentar="irParaPlanoAlimentar"
        @criar-adendo="criarAdendo"
        @upload-anexo="uploadAnexo"
        @baixar-anexo="baixarAnexo"
        @excluir-anexo="excluirAnexo"
      />
    </template>

    <template v-else>
      <div class="prontuario-formulario__header">
        <v-btn icon="mdi-arrow-left" variant="text" :aria-label="$t('acao.voltar')" @click="voltar" />
        <div>
          <h1 class="prontuario-formulario__title">{{ titulo }}</h1>
          <p v-if="prontuario" class="prontuario-formulario__subtitle">{{ prontuario.pacienteNome }}</p>
        </div>
      </div>

      <p v-if="carregando">...</p>

      <v-card v-else-if="form" variant="flat" color="surface-variant" class="prontuario-formulario__card">
        <v-card-text class="pt-4">
          <ProntuarioForm
            v-model="form"
            :submit-label="(isCriacao ? $t('acao.criar') : $t('acao.salvar')) as string"
            :loading="salvando"
            :mostrar-selecao-paciente="isCriacao"
            @submit="onSubmit"
            @cancel="voltar"
          />
        </v-card-text>
      </v-card>
    </template>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-facing-decorator'
import ProntuarioForm from './components/ProntuarioForm.vue'
import type { ProntuarioFormModel } from './components/ProntuarioForm.vue'
import ProntuarioDetalhe from './components/ProntuarioDetalhe.vue'
import prontuarioService from '../../service/prontuario-service'
import perfilNutricionalService from '../../nutricao/services/perfil-nutricional-service'
import avaliacaoService from '../../nutricao/services/avaliacao-service'
import planoAlimentarService from '../../nutricao/services/plano-alimentar-service'
import { carregarRotulosProntuario } from '../../util/prontuario-rotulos'
import { carregarRotulosEnum } from '../../nutricao/utils/enum-rotulos'
import { carregarRotulosAvaliacao } from '../../nutricao/utils/avaliacao-rotulos'
import type { EventoAuditoria, Prontuario, ProntuarioAdendo, ProntuarioAnexo } from '../../types/prontuario'
import type { PerfilNutricionalResponse } from '../../nutricao/types/perfil-nutricional'
import type { Avaliacao } from '../../nutricao/types/avaliacao'
import type { PlanoAlimentar } from '../../nutricao/types/plano-alimentar'
import { useAppStore } from '../../store/app.store'
import { useContextoStore } from '../../core/contexto/contexto.store'
import { extrairMensagemErro } from '../../util/api-util'

function formModelPadrao(): ProntuarioFormModel {
  return {
    pacienteId: null,
    autorId: null,
    secao: 'ANAMNESE',
    status: 'RASCUNHO',
    conteudo: null,
    comAnexo: false,
  }
}

/**
 * Uma única tela para criar, editar e visualizar prontuário — mesmo padrão de
 * ConsultaFormulario.vue/PacienteFormulario.vue (modo decidido pela rota:
 * /prontuarios/novo, /prontuarios/:id/editar, /prontuarios/:id).
 */
@Component({ name: 'ProntuarioFormulario', components: { ProntuarioForm, ProntuarioDetalhe } })
export default class ProntuarioFormulario extends Vue {
  prontuario: Prontuario | null = null
  form: ProntuarioFormModel | null = null
  historico: Prontuario[] = []
  perfil: PerfilNutricionalResponse | null = null
  avaliacaoRecente: Avaliacao | null = null
  planoAtivo: PlanoAlimentar | null = null
  rotulosProntuario: Record<string, string> = {}
  rotulosEnum: Record<string, string> = {}
  rotulosAvaliacao: Record<string, string> = {}
  adendos: ProntuarioAdendo[] = []
  anexos: ProntuarioAnexo[] = []
  eventosAuditoria: EventoAuditoria[] = []
  carregando = false
  salvando = false
  assinando = false
  criandoAdendo = false
  enviandoAnexo = false

  get appStore() {
    return useAppStore()
  }

  get contextoStore() {
    return useContextoStore()
  }

  get prontuarioId(): number | null {
    const id = this.$route.params.id as string | undefined
    return id ? Number(id) : null
  }

  get isCriacao(): boolean {
    return this.prontuarioId === null
  }

  get isView(): boolean {
    return this.$route.name === 'prontuario-visualizar'
  }

  get titulo(): string {
    return this.isCriacao ? (this.$t('prontuario.novo') as string) : (this.$t('prontuario.editar') as string)
  }

  async created() {
    if (this.isCriacao) {
      this.form = formModelPadrao()
      const pacienteId = Number(this.$route.query.pacienteId)
      if (pacienteId) {
        this.form.pacienteId = pacienteId
      }
      return
    }
    await this.carregar()
  }

  @Watch('prontuarioId')
  async onProntuarioIdChange(novo: number | null) {
    if (novo !== null) {
      await this.carregar()
    }
  }

  async carregar() {
    this.carregando = true
    try {
      const prontuario = await prontuarioService.buscarPorId(this.prontuarioId as number)
      if (!prontuario) {
        this.$router.replace('/prontuarios')
        return
      }
      this.prontuario = prontuario
      this.form = {
        pacienteId: prontuario.pacienteId,
        autorId: prontuario.autorId,
        secao: prontuario.secao,
        status: prontuario.status,
        conteudo: prontuario.conteudo,
        comAnexo: prontuario.comAnexo,
      }
      if (this.isView) {
        await this.carregarDetalhe(prontuario)
      }
    } finally {
      this.carregando = false
    }
  }

  async carregarDetalhe(prontuario: Prontuario) {
    const [historico, perfil, rotulosProntuario, rotulosEnum, rotulosAvaliacao, adendos, anexos, eventosAuditoria, planosAlimentares] = await Promise.all([
      prontuarioService.buscarPorPaciente(prontuario.pacienteId).catch(() => []),
      perfilNutricionalService.buscarPorPaciente(prontuario.pacienteId),
      carregarRotulosProntuario(),
      carregarRotulosEnum(),
      carregarRotulosAvaliacao(),
      prontuarioService.listarAdendos(prontuario.id).catch(() => []),
      prontuarioService.listarAnexos(prontuario.id).catch(() => []),
      prontuarioService.listarAuditoria(prontuario.id).catch(() => []),
      planoAlimentarService.buscarPorPaciente(prontuario.pacienteId).catch(() => []),
    ])
    this.historico = historico
    this.perfil = perfil ?? null
    this.rotulosProntuario = rotulosProntuario
    this.rotulosEnum = rotulosEnum
    this.rotulosAvaliacao = rotulosAvaliacao
    this.adendos = adendos
    this.anexos = anexos
    this.eventosAuditoria = eventosAuditoria
    this.avaliacaoRecente = perfil?.avaliacaoAtualId ? ((await avaliacaoService.buscarPorId(perfil.avaliacaoAtualId)) ?? null) : null
    this.planoAtivo = planosAlimentares.find((plano) => plano.status === 'ATIVO') ?? null
  }

  async onSubmit() {
    if (!this.form) {
      return
    }
    this.salvando = true
    try {
      if (this.isCriacao) {
        await this.criar()
      } else {
        await this.atualizar()
      }
      this.appStore.setToast({ mensagem: this.$t('sucesso.prontuarioSalvo') as string, erro: false })
      this.voltar()
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.salvarProntuario') as string), erro: true })
    } finally {
      this.salvando = false
    }
  }

  private async criar() {
    const form = this.form as ProntuarioFormModel
    if (!form.pacienteId || !form.autorId) {
      return
    }
    await prontuarioService.criar({
      pacienteId: form.pacienteId,
      autorId: form.autorId,
      secao: form.secao,
      status: form.status,
      conteudo: form.conteudo,
      comAnexo: form.comAnexo,
    })
  }

  private async atualizar() {
    const form = this.form as ProntuarioFormModel
    if (!form.autorId) {
      return
    }
    await prontuarioService.atualizar((this.prontuario as Prontuario).id, {
      autorId: form.autorId,
      secao: form.secao,
      status: form.status,
      conteudo: form.conteudo,
      comAnexo: form.comAnexo,
    })
  }

  async descartar() {
    const prontuario = this.prontuario
    if (!prontuario || !confirm(this.$t('paciente.confirmarExclusao') as string)) {
      return
    }
    try {
      await prontuarioService.excluir(prontuario.id)
      this.appStore.setToast({ mensagem: this.$t('sucesso.excluido') as string, erro: false })
      this.voltar()
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.excluirProntuario') as string), erro: true })
    }
  }

  async assinar() {
    const prontuario = this.prontuario
    if (!prontuario) {
      return
    }
    this.assinando = true
    try {
      const atualizado = await prontuarioService.assinar(prontuario.id)
      this.prontuario = atualizado
      this.historico = this.historico.map((item) => (item.id === atualizado.id ? atualizado : item))
      this.eventosAuditoria = await prontuarioService.listarAuditoria(prontuario.id)
      this.appStore.setToast({ mensagem: this.$t('sucesso.prontuarioAssinado') as string, erro: false })
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.assinarProntuario') as string), erro: true })
    } finally {
      this.assinando = false
    }
  }

  async criarAdendo(texto: string) {
    const prontuario = this.prontuario
    const usuarioId = this.contextoStore.contexto?.usuarioId
    if (!prontuario || !usuarioId) {
      return
    }
    this.criandoAdendo = true
    try {
      const adendo = await prontuarioService.criarAdendo(prontuario.id, { autorId: usuarioId, texto })
      this.adendos = [...this.adendos, adendo]
      this.eventosAuditoria = await prontuarioService.listarAuditoria(prontuario.id)
      this.appStore.setToast({ mensagem: this.$t('sucesso.adendoCriado') as string, erro: false })
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.criarAdendo') as string), erro: true })
    } finally {
      this.criandoAdendo = false
    }
  }

  async uploadAnexo(arquivo: File) {
    const prontuario = this.prontuario
    if (!prontuario) {
      return
    }
    this.enviandoAnexo = true
    try {
      const anexo = await prontuarioService.uploadAnexo(prontuario.id, arquivo)
      this.anexos = [...this.anexos, anexo]
      this.eventosAuditoria = await prontuarioService.listarAuditoria(prontuario.id)
      this.appStore.setToast({ mensagem: this.$t('sucesso.anexoAdicionado') as string, erro: false })
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.uploadAnexo') as string), erro: true })
    } finally {
      this.enviandoAnexo = false
    }
  }

  async baixarAnexo(anexo: ProntuarioAnexo) {
    const prontuario = this.prontuario
    if (!prontuario) {
      return
    }
    try {
      const blob = await prontuarioService.downloadAnexo(prontuario.id, anexo.id)
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = anexo.nomeOriginal
      link.click()
      URL.revokeObjectURL(url)
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.baixarAnexo') as string), erro: true })
    }
  }

  async excluirAnexo(anexoId: number) {
    const prontuario = this.prontuario
    if (!prontuario || !confirm(this.$t('prontuario.confirmarExclusaoAnexo') as string)) {
      return
    }
    try {
      await prontuarioService.excluirAnexo(prontuario.id, anexoId)
      this.anexos = this.anexos.filter((item) => item.id !== anexoId)
      this.eventosAuditoria = await prontuarioService.listarAuditoria(prontuario.id)
      this.appStore.setToast({ mensagem: this.$t('sucesso.anexoExcluido') as string, erro: false })
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.excluirAnexo') as string), erro: true })
    }
  }

  irParaProntuario(id: number) {
    if (id !== this.prontuario?.id) {
      this.$router.push(`/prontuarios/${id}`)
    }
  }

  irParaEdicao() {
    this.$router.push(`/prontuarios/${this.prontuarioId}/editar`)
  }

  irParaAvaliacao(id: number) {
    this.$router.push(`/avaliacoes/${id}`)
  }

  irParaPlanoAlimentar(id: number) {
    this.$router.push(`/nutricao/${id}`)
  }

  voltar() {
    this.$router.push('/prontuarios')
  }
}
</script>

<style scoped lang="scss">
.prontuario-formulario__header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.prontuario-formulario__title {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.prontuario-formulario__subtitle {
  color: rgb(var(--v-theme-on-surface-variant));
  margin: 0;
}

.prontuario-formulario__card {
  border-radius: 12px;
}
</style>
