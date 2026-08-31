Quero que você faça o commit das alterações atuais.

Antes de realizar qualquer commit git pull na master para ver se não há atualizações e siga exatamente este fluxo:

1. Verifique a branch atual:
   git branch --show-current

2. Verifique o estado do repositório:
   git status

3. Compare a branch atual com a master para entender tudo que está sendo submetido:
   git diff master...HEAD

4. Analise também as alterações ainda não commitadas:
   git diff
   git diff --cached

5. Leia os arquivos alterados quando necessário para entender o contexto e NÃO faça uma descrição baseada apenas nos nomes dos arquivos.

6. Identifique o que realmente foi implementado nesta alteração:
   - novas funcionalidades;
   - alterações de comportamento;
   - correções;
   - refatorações;
   - melhorias de arquitetura;
   - alterações de frontend;
   - alterações de backend;
   - banco de dados/migrations;
   - testes.
   - ignorar tudo que estiver fora de backend, frontend e docker-compose.yml, a não ser que faça sentido na aplicação.


7. Gere uma mensagem de commit seguindo Conventional Commits.

Formato:

<tipo>(<escopo>): <resumo objetivo>

<descrição detalhada das funcionalidades implementadas>

- funcionalidade ou alteração 1;
- funcionalidade ou alteração 2;
- funcionalidade ou alteração 3;
- etc.


Co-authored-by: Magdiel <SEU_EMAIL_DO_GIT>

IMPORTANTE SOBRE A DESCRIÇÃO:

A descrição deve explicar O QUE FOI ENTREGUE, e não simplesmente listar arquivos alterados.

ERRADO:

"Altera UserService.java"
"Cria Controller.java"
"Atualiza componente Vue"

CERTO:

"Implementa o registro de verticais profissionais, permitindo que novas especialidades sejam descobertas automaticamente pelo Spring sem necessidade de condicionais no código."

A descrição deve ser útil para alguém que futuramente olhar o histórico do Git e quiser entender o que aquela alteração entregou.

Se houver várias funcionalidades relacionadas, agrupe-as em tópicos.

Não invente funcionalidades que não estejam presentes no diff.

============================================================
REGRAS DO COMMIT
============================================================

Use Conventional Commits:

feat     → nova funcionalidade
fix      → correção de bug
refactor → refatoração sem alteração funcional
perf     → melhoria de performance
test     → testes
docs     → documentação
style    → alterações de estilo/formatação
build    → build/dependências
ci       → CI/CD
chore    → manutenção

Escolha o tipo com base no que realmente foi alterado.

O título deve ser curto, direto e em português.

Exemplo:

feat(verticals): implementa registro dinâmico de verticais

Não coloque ponto final no título.

============================================================
ASSINATURA
============================================================

No final da mensagem do commit, adicione:

Co-authored-by: Magdiel <SEU_EMAIL_DO_GIT>

Antes de usar o email do Magdiel, verifique qual email está configurado no Git:

git config user.email

Use esse email na assinatura.

NÃO invente um email.

============================================================
ARQUIVOS NÃO DEVEM SER COMMITADOS
============================================================

Antes do commit, verifique se existem arquivos sensíveis ou gerados indevidamente, como:

.env
.env.*
credentials
secrets
arquivos temporários
logs
build/
dist/
node_modules/
.idea/
arquivos gerados automaticamente

Se algum deles estiver sendo incluído no commit e não fizer parte intencional da alteração, NÃO faça o commit ainda.

Avise quais arquivos precisam ser removidos do staging.

============================================================
STAGING
============================================================

Não faça:

git add .

automaticamente.

Primeiro analise quais arquivos realmente pertencem à alteração.

Adicione somente os arquivos relacionados à funcionalidade que está sendo submetida.

Se existirem alterações não relacionadas ao trabalho atual, NÃO as inclua no commit.

============================================================
VALIDAÇÃO
============================================================

Antes do commit:

- verifique novamente o diff;
- confirme os arquivos staged;
- confirme que não existem secrets;
- confirme que a mensagem descreve corretamente a implementação.

Depois faça o commit.

Após o commit execute:

git status

e confirme se o commit foi criado corretamente.

============================================================
IMPORTANTE
============================================================

Não quero apenas que você sugira a mensagem de commit.

QUERO QUE VOCÊ REALIZE O COMMIT.

O fluxo deve ser:
GIT PULL NA MASTER
↓
ANALISAR DIFF
↓
IDENTIFICAR FUNCIONALIDADES
↓
SELECIONAR ARQUIVOS
↓
CRIAR MENSAGEM
↓
FAZER COMMIT
↓
VERIFICAR STATUS

Se houver alguma dúvida crítica sobre o que deve entrar no commit, pare e pergunte antes de executar.

Caso esteja tudo claro, execute o commit.
