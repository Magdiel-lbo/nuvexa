<template>
  <div class="equipe">
    <div class="equipe__header">
      <div>
        <h1 class="equipe__title">{{ $t('equipe.titulo') }}</h1>
        <p class="equipe__subtitle">{{ $t('equipe.subtitulo') }}</p>
      </div>
      <NuvexaButton variant="primary" icon="mdi-account-plus-outline" @click="abrirConvite">{{ $t('equipe.convidar') }}</NuvexaButton>
    </div>

    <div class="equipe__abas">
      <button type="button" class="equipe__aba" :class="{ 'equipe__aba--ativa': aba === 'membros' }" @click="aba = 'membros'">
        {{ $t('equipe.abas.membros') }} <span class="equipe__aba-contagem">{{ membrosAtivos.length }}</span>
      </button>
      <button type="button" class="equipe__aba" :class="{ 'equipe__aba--ativa': aba === 'convites' }" @click="aba = 'convites'">
        {{ $t('equipe.abas.convites') }} <span class="equipe__aba-contagem">{{ convitesPendentes.length }}</span>
      </button>
    </div>

    <v-card v-if="aba === 'membros'" variant="flat" color="surface-variant" class="equipe__card">
      <NuvexaDataTable
        :headers="headersMembros"
        :items="membros"
        item-key="vinculoId"
        :loading="carregando"
        v-model:page="paginaMembros"
        v-model:items-per-page="porPagina"
        :items-per-page-label="$t('equipe.itensPorPagina') as string"
        :of-label="$t('dashboardEmpresas.lista.de') as string"
        :total-items="membros.length"
      >
        <template #item.nome="{ item }">
          <div class="equipe__nome-cell">
            <v-avatar color="primary" variant="tonal" size="31"><span class="equipe__iniciais">{{ iniciais(item.nome) }}</span></v-avatar>
            <div class="equipe__nome-info">
              <span class="equipe__nome-texto">{{ item.nome }}</span>
              <span class="equipe__nome-sub">{{ item.email }}</span>
            </div>
          </div>
        </template>

        <template #item.papel="{ item }">
          <v-select
            v-if="item.ativo && podeAlterar(item)"
            :model-value="item.papel"
            :items="papeisConcedidos"
            item-title="rotulo"
            item-value="valor"
            density="compact"
            hide-details
            class="equipe__papel-select"
            @update:model-value="alterarPapel(item, $event)"
          />
          <NuvexaStatusChip v-else :label="rotuloPapel(item.papel)" color="on-surface-variant" />
        </template>

        <template #item.ativo="{ item }">
          <NuvexaStatusChip :label="item.ativo ? $t('equipe.status.ativo') : $t('equipe.status.removido')" :color="item.ativo ? 'success' : 'error'" />
        </template>

        <template #item.acoes="{ item }">
          <div class="d-flex justify-end">
            <v-tooltip v-if="item.ativo && podeAlterar(item)" :text="$t('equipe.removerAcesso')" location="top">
              <template #activator="{ props }">
                <v-btn v-bind="props" icon="mdi-account-remove-outline" variant="text" density="comfortable" size="small" @click="pedirRemocao(item)" />
              </template>
            </v-tooltip>
          </div>
        </template>

        <template #no-data>
          <NuvexaEmptyState icon="mdi-account-group-outline" :title="$t('equipe.vazioMembros') as string" :description="''" />
        </template>
      </NuvexaDataTable>
    </v-card>

    <v-card v-else variant="flat" color="surface-variant" class="equipe__card">
      <NuvexaDataTable
        :headers="headersConvites"
        :items="convites"
        item-key="id"
        :loading="carregando"
        v-model:page="paginaConvites"
        v-model:items-per-page="porPagina"
        :items-per-page-label="$t('equipe.itensPorPagina') as string"
        :of-label="$t('dashboardEmpresas.lista.de') as string"
        :total-items="convites.length"
      >
        <template #item.papel="{ item }">{{ rotuloPapel(item.papel) }}</template>
        <template #item.status="{ item }">
          <NuvexaStatusChip :label="$t(`equipe.statusConvite.${item.status}`) as string" :color="corDoConvite(item.status)" />
        </template>
        <template #item.expiraEm="{ item }">{{ formatarData(item.expiraEm) }}</template>
        <template #item.acoes="{ item }">
          <div class="d-flex justify-end">
            <v-tooltip v-if="item.status === 'PENDENTE' && podeConceder(item.papel)" :text="$t('equipe.revogarConvite')" location="top">
              <template #activator="{ props }">
                <v-btn v-bind="props" icon="mdi-email-remove-outline" variant="text" density="comfortable" size="small" @click="revogarConvite(item)" />
              </template>
            </v-tooltip>
          </div>
        </template>
        <template #no-data>
          <NuvexaEmptyState icon="mdi-email-outline" :title="$t('equipe.vazioConvites') as string" :description="''" />
        </template>
      </NuvexaDataTable>
    </v-card>

    <v-dialog v-model="conviteAberto" max-width="480">
      <v-card>
        <v-card-title>{{ $t('equipe.convidar') }}</v-card-title>
        <v-card-text>
          <v-form ref="formConvite" @submit.prevent="enviarConvite">
            <v-text-field v-model="novoConvite.email" type="email" :label="$t('auth.email')" :rules="[obrigatorio]" />
            <v-select
              v-model="novoConvite.papel"
              :items="papeisConcedidos"
              item-title="rotulo"
              item-value="valor"
              :label="$t('equipe.papel')"
              :rules="[obrigatorio]"
            />
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="conviteAberto = false">{{ $t('acao.cancelar') }}</v-btn>
          <v-btn color="primary" :loading="enviando" @click="enviarConvite">{{ $t('equipe.enviarConvite') }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="remocaoAberta" max-width="440">
      <v-card>
        <v-card-title>{{ $t('equipe.removerAcesso') }}</v-card-title>
        <v-card-text>{{ $t('equipe.confirmarRemocao', { nome: alvoDaRemocao?.nome ?? '' }) }}</v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="remocaoAberta = false">{{ $t('acao.cancelar') }}</v-btn>
          <v-btn color="error" :loading="removendo" @click="confirmarRemocao">{{ $t('equipe.removerAcesso') }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import equipeService from '../../service/equipe-service'
import type { Convite, Membro, StatusConvite } from '../../types/equipe'
import type { PapelOrganizacional } from '../../core/contexto/contexto'
import { extrairMensagemErro } from '../../util/api-util'
import { useAppStore } from '../../store/app.store'
import { useContextoStore } from '../../core/contexto/contexto.store'
import NuvexaButton from '../../components/common/NuvexaButton.vue'
import NuvexaDataTable, { type NuvexaTableHeader } from '../../components/common/NuvexaDataTable.vue'
import NuvexaEmptyState from '../../components/common/NuvexaEmptyState.vue'
import NuvexaStatusChip from '../../components/common/NuvexaStatusChip.vue'

const PAPEIS: PapelOrganizacional[] = ['PROPRIETARIO', 'GESTOR', 'MEMBRO', 'RECEPCAO']

/** Espelha PapelOrganizacional.podeConceder do backend — a UI só decide o que mostrar. */
function concede(executor: PapelOrganizacional | null, alvo: PapelOrganizacional): boolean {
  if (executor === 'PROPRIETARIO') return true
  if (executor === 'GESTOR') return alvo === 'MEMBRO' || alvo === 'RECEPCAO'
  return false
}

@Component({ name: 'Equipe', components: { NuvexaButton, NuvexaDataTable, NuvexaEmptyState, NuvexaStatusChip } })
export default class Equipe extends Vue {
  aba: 'membros' | 'convites' = 'membros'
  membros: Membro[] = []
  convites: Convite[] = []
  carregando = false
  paginaMembros = 1
  paginaConvites = 1
  porPagina = 10

  conviteAberto = false
  enviando = false
  novoConvite: { email: string; papel: PapelOrganizacional } = { email: '', papel: 'MEMBRO' }

  remocaoAberta = false
  removendo = false
  alvoDaRemocao: Membro | null = null

  get appStore() {
    return useAppStore()
  }

  get meuPapel(): PapelOrganizacional | null {
    return useContextoStore().papelOrganizacional
  }

  get membrosAtivos(): Membro[] {
    return this.membros.filter((membro) => membro.ativo)
  }

  get convitesPendentes(): Convite[] {
    return this.convites.filter((convite) => convite.status === 'PENDENTE')
  }

  get headersMembros(): NuvexaTableHeader[] {
    return [
      { title: this.$t('equipe.colunas.nome') as string, key: 'nome' },
      { title: this.$t('equipe.colunas.papel') as string, key: 'papel', sortable: false },
      { title: this.$t('equipe.colunas.situacao') as string, key: 'ativo', sortable: false },
      { title: this.$t('acao.titulo') as string, key: 'acoes', sortable: false, align: 'end' },
    ]
  }

  get headersConvites(): NuvexaTableHeader[] {
    return [
      { title: this.$t('equipe.colunas.email') as string, key: 'email' },
      { title: this.$t('equipe.colunas.papel') as string, key: 'papel', sortable: false },
      { title: this.$t('equipe.colunas.situacao') as string, key: 'status', sortable: false },
      { title: this.$t('equipe.colunas.expiraEm') as string, key: 'expiraEm', sortable: false },
      { title: this.$t('equipe.colunas.convidadoPor') as string, key: 'convidadoPorNome', sortable: false },
      { title: this.$t('acao.titulo') as string, key: 'acoes', sortable: false, align: 'end' },
    ]
  }

  get papeisConcedidos() {
    return PAPEIS.filter((papel) => concede(this.meuPapel, papel)).map((papel) => ({ valor: papel, rotulo: this.rotuloPapel(papel) }))
  }

  obrigatorio = (valor: unknown) => (valor !== null && valor !== undefined && valor !== '') || (this.$t('validacao.obrigatorio') as string)

  rotuloPapel(papel: PapelOrganizacional): string {
    return this.$t(`equipe.papeis.${papel}`) as string
  }

  podeConceder(papel: PapelOrganizacional): boolean {
    return concede(this.meuPapel, papel)
  }

  podeAlterar(membro: Membro): boolean {
    return concede(this.meuPapel, membro.papel)
  }

  corDoConvite(status: StatusConvite): string {
    return { PENDENTE: 'info', ACEITO: 'success', EXPIRADO: 'on-surface-variant', REVOGADO: 'error' }[status]
  }

  iniciais(nome: string): string {
    return nome.trim().split(/\s+/).filter(Boolean).slice(0, 2).map((parte) => parte[0]).join('').toUpperCase()
  }

  formatarData(iso: string): string {
    return new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })
  }

  async created() {
    await this.carregar()
  }

  async carregar() {
    this.carregando = true
    try {
      const [membros, convites] = await Promise.all([equipeService.membros(), equipeService.convites()])
      this.membros = membros
      this.convites = convites
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarEquipe') as string), erro: true })
    } finally {
      this.carregando = false
    }
  }

  abrirConvite() {
    this.novoConvite = { email: '', papel: (this.papeisConcedidos.find((p) => p.valor === 'MEMBRO')?.valor ?? this.papeisConcedidos[0]?.valor ?? 'MEMBRO') as PapelOrganizacional }
    this.conviteAberto = true
  }

  async enviarConvite() {
    const resultado = await (this.$refs.formConvite as any).validate()
    if (!resultado.valid) {
      return
    }
    this.enviando = true
    try {
      await equipeService.convidar({ email: this.novoConvite.email.trim(), papel: this.novoConvite.papel })
      this.appStore.setToast({ mensagem: this.$t('equipe.conviteEnviado') as string, erro: false })
      this.conviteAberto = false
      this.aba = 'convites'
      await this.carregar()
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.convidar') as string), erro: true })
    } finally {
      this.enviando = false
    }
  }

  async revogarConvite(convite: Convite) {
    try {
      await equipeService.revogarConvite(convite.id)
      await this.carregar()
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.revogarConvite') as string), erro: true })
    }
  }

  async alterarPapel(membro: Membro, papel: PapelOrganizacional) {
    if (papel === membro.papel) {
      return
    }
    try {
      await equipeService.alterarPapel(membro.vinculoId, papel)
      this.appStore.setToast({ mensagem: this.$t('sucesso.salvo') as string, erro: false })
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.alterarPapel') as string), erro: true })
    }
    await this.carregar()
  }

  pedirRemocao(membro: Membro) {
    this.alvoDaRemocao = membro
    this.remocaoAberta = true
  }

  async confirmarRemocao() {
    if (!this.alvoDaRemocao) {
      return
    }
    this.removendo = true
    try {
      await equipeService.removerMembro(this.alvoDaRemocao.vinculoId)
      this.remocaoAberta = false
      await this.carregar()
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.removerMembro') as string), erro: true })
    } finally {
      this.removendo = false
    }
  }
}
</script>

<style scoped lang="scss">
.equipe {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.equipe__header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.equipe__title {
  margin: 0;
  font-size: 1.7rem;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.equipe__subtitle {
  margin: 4px 0 0;
  font-size: 0.85rem;
  color: rgb(var(--v-theme-on-surface-variant));
}

.equipe__abas {
  display: flex;
  gap: 4px;
  border-bottom: 1px solid rgba(var(--v-theme-on-surface), 0.12);
}

.equipe__aba {
  background: none;
  border: none;
  padding: 10px 16px;
  cursor: pointer;
  font: inherit;
  font-size: 0.9rem;
  color: rgb(var(--v-theme-on-surface-variant));
  border-bottom: 2px solid transparent;

  &--ativa {
    color: rgb(var(--v-theme-primary));
    border-bottom-color: rgb(var(--v-theme-primary));
    font-weight: 600;
  }
}

.equipe__aba-contagem {
  margin-left: 6px;
  font-size: 0.75rem;
  opacity: 0.8;
}

.equipe__card {
  border-radius: 12px;
  padding: 4px 12px;
}

.equipe__nome-cell {
  display: flex;
  align-items: center;
  gap: 11px;
}

.equipe__iniciais {
  font-size: 0.68rem;
  font-weight: 700;
}

.equipe__nome-info {
  display: flex;
  flex-direction: column;
}

.equipe__nome-texto {
  font-size: 0.85rem;
  font-weight: 600;
}

.equipe__nome-sub {
  font-size: 0.72rem;
  color: rgb(var(--v-theme-on-surface-variant));
}

.equipe__papel-select {
  max-width: 190px;
}
</style>
