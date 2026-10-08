# Bugs e pendências encontrados durante a Etapa C (documentação Swagger)

Achados durante a auditoria de endpoints/serviços para documentação — não corrigidos de propósito
(fora do escopo da Etapa C, que é só documentação/teste manual). Tratar depois que o checklist de
`SWAGGER_CHECKLIST.md` estiver fechado.

## 1. `busca` de `ConsultaController` documentado com descrição que não bate com o comportamento real

- **Controller/método:** `ConsultaController.findAll` (`GET /api/v1/consultas`) → `ConsultaService.buscarConsultas`.
- **O que foi encontrado:** `descriptions.yml` documenta o parâmetro `busca` como "Termo de busca
  livre", mas a implementação (`ConsultaService.buscarConsultas`) filtra estritamente por
  `consulta.paciente.nome.containsIgnoreCase(busca)` — ou seja, busca só por nome do paciente, a
  mesma semântica usada em `PacienteController`, `PerfilNutricionalController` e
  `AvaliacaoController` (documentados como "Termo de busca por nome do paciente").
- **Impacto:** documentação enganosa — sugere que a busca cobre mais campos do que realmente
  cobre (ex.: tipo/status/observações da consulta).
- **Ação sugerida:** alinhar a descrição do parâmetro em `descriptions.yml` para "Termo de busca
  por nome do paciente", igual aos demais controllers.

## 2. `AvaliacaoService.delete()` não bloqueia exclusão de avaliação `CONCLUIDA`

- **Controller/método:** `AvaliacaoController.delete` (`DELETE /api/v1/avaliacoes/{id}`) →
  `AvaliacaoService.delete`.
- **O que foi encontrado:** `AvaliacaoService.update()` chama `garantirEditavel(avaliacao)`, que
  bloqueia alteração quando `status == StatusAvaliacao.CONCLUIDA` (mensagem
  `avaliacao.concluida.imutavel`). `AvaliacaoService.delete()` não tem a mesma checagem — uma
  avaliação concluída (histórico clínico fechado) pode ser excluída mesmo não podendo ser editada.
- **Impacto:** possível inconsistência de regra de negócio — se avaliação concluída é imutável
  para edição por ser "histórico clínico fechado" (ver comentário no próprio código,
  `AvaliacaoService.garantirEditavel`), a mesma justificativa provavelmente deveria valer para
  exclusão.
- **Ação sugerida:** confirmar com o dono do produto se exclusão de avaliação concluída deveria
  ser bloqueada também; se sim, aplicar a mesma guarda de `garantirEditavel` (ou equivalente) em
  `delete()`.

## 3. `PlanoAlimentarService.delete()` não bloqueia exclusão de plano `ENCERRADO`

- **Controller/método:** `PlanoAlimentarController.delete` (`DELETE /api/v1/planos-alimentares/{id}`)
  → `PlanoAlimentarService.delete`.
- **O que foi encontrado:** mesmo padrão do achado #2, mas em Plano Alimentar. `update()` chama
  `garantirEditavel(planoAlimentar)`, que bloqueia alteração quando
  `status == StatusPlanoAlimentar.ENCERRADO` (mensagem `planoAlimentar.encerrado.imutavel`).
  `delete()` não tem a mesma checagem — um plano encerrado pode ser excluído mesmo não podendo
  ser editado.
- **Impacto:** mesma inconsistência de regra de negócio do achado #2 — se ENCERRADO é terminal
  para edição, provavelmente deveria ser terminal para exclusão também.
- **Ação sugerida:** mesma do achado #2 — confirmar com o dono do produto e, se aplicável,
  aplicar a mesma guarda de `garantirEditavel` (ou equivalente) em `delete()`. Vale revisar os
  dois achados (#2 e #3) juntos, já que parecem ser a mesma lacuna repetida em dois services.

## Suspeita de padrão sistêmico (achados #2 e #3)

Os achados #2 (`AvaliacaoService`) e #3 (`PlanoAlimentarService`) têm exatamente a mesma forma:
`update()` chama `garantirEditavel()` antes de alterar um registro em estado terminal, mas
`delete()` do mesmo service não chama a checagem equivalente antes de excluir. Isso **não parece
ser coincidência de dois bugs avulsos** — é candidato a lacuna sistêmica em como o padrão
"registro em estado final é imutável" foi implementado nesses services.

Checagem rápida (sem investigar a fundo, só grep) encontrou um terceiro caso com a mesma forma e
um contraexemplo que serve de referência para a correção:

- **`ConsultaService.delete()`** também não chama `garantirEditavel` (a checagem que bloqueia
  alterar consulta `REALIZADA`/`CANCELADA`/`FALTOU`, adicionada nesta mesma sessão) — provável
  4º caso da mesma lacuna.
- **`ProntuarioService.delete()`** já chama `garantirEditavel(prontuario)` corretamente antes de
  excluir (junto com `garantirSemFilhos`) — ou seja, o padrão não é ausente em todo lugar, é
  **inconsistente entre services**: alguém implementou a guarda certa em Prontuário e não replicou
  nos outros três.

**Recomendação para quando isso for tratado:** ao corrigir, auditar `delete()` de **todos** os
services que têm `garantirEditavel` (ou equivalente) em `update()` — não corrigir só Avaliação e
Plano Alimentar isoladamente, checar Consulta junto e confirmar se há mais algum service com o
mesmo formato antes de fechar a revisão. Não investigado o motivo raiz (ex.: se foi omissão em
commits diferentes ou decisão consciente que não se sustenta mais) — fica para a etapa de correção.

## 4. Mensagem de erro de `ProntuarioAnexoService` fala só em "excluir", mas é usada também pro upload

- **Controller/método:** `ProntuarioAnexoController.upload` (`POST /api/v1/prontuarios/{prontuarioId}/anexos`)
  → `ProntuarioAnexoService.upload` → `garantirEditavel`.
- **O que foi encontrado:** a mensagem `prontuarioAnexo.assinado.imutavel` = "Não é possível
  excluir anexo de um prontuário assinado" foi escrita pensando em exclusão (`delete()`), mas
  `garantirEditavel` — que lança essa mesma mensagem — também é chamado no início de `upload()`.
  Ou seja: tentar fazer upload de anexo em um prontuário já assinado retorna um 400 cujo texto
  fala em "excluir", quando a ação tentada foi enviar um arquivo novo.
- **Impacto:** mensagem de erro confusa para quem estiver testando/usando a API — não é um bug
  de comportamento (o bloqueio em si está correto, é só o texto que não bate com a ação).
- **Ação sugerida:** reescrever a mensagem para algo neutro que sirva pros dois casos, ex.: "Não é
  possível alterar anexos de um prontuário assinado", ou criar uma chave de mensagem separada
  para cada ação (upload vs. exclusão).
