---
name: nuvexa-backend
description: Padrões de implementação do backend Nuvexa (backend/) — camadas Controller/Service/Repository, entidades, DTOs, exceções, transações, logging, migrations Flyway e quando usar Strategy/Chain of Responsibility/Observer. Use ao criar ou alterar qualquer controller, service, repository, entity, DTO ou migration dentro de backend/src.
---

# Nuvexa Backend

Regras de execução para tarefas de backend, baseadas no guia pessoal de padrões Java do usuário (`docs/roteirojava/PADROES-DE-PROJETO.md`) já filtrado pelo que se aplica a um monólito simples como o Nuvexa — não a um ecossistema de microsserviços. Estrutura de pacotes (`platform/core/verticals`), fluxo de auth JWT e o estado do multi-tenancy já estão no `CLAUDE.md` da raiz — não repita isso aqui, só aplique.

## Fluxo de uma feature nova

`Controller → Service → Repository`, com DTO de request/response sempre separado da entidade JPA — a entidade **nunca** é serializada direto na resposta HTTP. Controllers são finos: recebem o DTO, chamam um método do service, devolvem o resultado. Nenhuma regra de negócio no controller. Não existe camada `*Mapper` — conversão fica no Model/DTO, ver "Conversão (Model/DTO)" abaixo.

```java
@RestController
@RequestMapping("/api/v1/<recurso>")
@RequiredArgsConstructor
public class XController {

    private final XService xService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public XResponseDTO create(@Valid @RequestBody XCreateRequestDTO request) {
        return xService.create(request);
    }
}
```

Não adicione anotações do SpringDoc/OpenAPI (`@Operation`, `@ApiResponse`, `@Tag`) — a dependência não está no `pom.xml` deste projeto, isso quebraria a compilação.

## Entidades

Sempre estendem `platform.persistence.AbstractModel` (dá `id`/`createdAt`/`updatedAt`) com esta combinação exata de Lombok — não use `@Builder` puro nem `@AllArgsConstructor` em entidade, porque quebra o builder herdado:

```java
@Entity
@Table(name = "nome_plural_snake_case")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class X extends AbstractModel {
    @Column(nullable = false, length = 150)
    private String campo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AlgumEnum outroCampo;
}
```

Relacionamentos sempre `FetchType.LAZY`.

## DTOs

Request e response DTOs usam sempre `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder` (aqui, ao contrário da entidade, é `@Builder` normal — DTO não estende nada). Validação Jakarta com mensagem apontando para uma chave i18n, nunca texto literal:

```java
@NotBlank(message = "{x.campo.required}")
private String campo;
```

## Mensagens (i18n)

Toda mensagem de validação ou de erro de negócio é uma chave em `src/main/resources/mensagens/messages.properties`, convenção `dominio.campo.regra` (ex.: `patient.birthDate.required`, `patient.notFound`) ou `error.*` para erros genéricos de infraestrutura (`error.notFound`, `error.unauthenticated`). Nunca hardcode a mensagem de erro em português direto no código — sempre a chave, resolvida via `MessageSource`/`CustomException`.

## Exceções e logging

Erro de negócio esperado → `throw new CustomException(HttpStatus.X, mensagemResolvida)`; o `GlobalExceptionHandlerController` já converte isso (e erros de validação) num `ApiError` uniforme — não crie handlers de exceção novos a menos que seja um tipo de exceção genuinamente novo.

Logging via Lombok `@Log4j2` na classe de service, nunca `System.out`/`e.printStackTrace()`:

| Nível | Quando |
|---|---|
| `log.info` | fluxo de negócio normal (recurso criado/atualizado/removido, autenticação) — sempre com o id do recurso: `log.info("Paciente criado com id={}", id)` |
| `log.warn` | `CustomException` capturada / situação esperada mas anômala |
| `log.error` | exceção não prevista, sempre passando a exception como segundo argumento pro stack trace ir pro log |

## Transações

Toda classe de service tem `@Transactional` — os services atuais (`PatientService`, `AuthService`) aplicam no nível da classe. Para service **novo**, prefira o padrão mais preciso: `@Transactional` só nos métodos de escrita e `@Transactional(readOnly = true)` nos de leitura, em vez de repetir `@Transactional` na classe inteira — não precisa refatorar os services existentes só por causa disso.

## Busca dinâmica (QueryDSL)

O padrão aqui é diferente do `QuerydslPredicateExecutor` no repository: o `JPAQueryFactory` é injetado direto no **service** e a query é montada ali com a Q-class gerada (`QNutritionProfile`, etc. — regeneradas por `./mvnw compile`, ver `PatientService.searchProfiles`). Repository fica só com `JpaRepository` puro + métodos derivados simples (`findByX`, `existsByX`) quando servem; não crie repository com `QuerydslPredicateExecutor` a menos que já exista um precedente assim no módulo que você está tocando.

## Conversão (Model/DTO)

Não existe camada `*Mapper`. Toda lógica de conversão — montar uma entidade a partir de um DTO, montar um DTO a partir de uma entidade, ou montar um DTO a partir de parâmetros soltos (ex.: query params de um filtro) — é um método no próprio Model ou DTO envolvido, nunca no Controller, nunca no Service. Não use o bean `ModelMapper` já configurado (`ModelMapperConfig`) para isso — está no projeto mas não é o padrão usado.

Convenção de nomes:
- `request.toX()` — método de instância no DTO de request que devolve uma entidade nova (ex.: `PacienteCreateRequestDTO.toPaciente()`).
- `request.atualizar(entidade)` — método de instância no DTO de request que muta uma entidade existente com os próprios campos (ex.: `PacienteUpdateRequestDTO.atualizar(Paciente paciente)`).
- `XResponseDTO.from(entidade)` — método estático no DTO de response que monta o DTO a partir da entidade.
- `XDTO.of(parâmetros...)` — método estático que monta o DTO a partir de parâmetros soltos, não de uma entidade (ex.: `PacienteRelatorioFiltroDTO.of(busca, sexo, objetivo, nivelAtividade)`).

**Direção de dependência importa mais que a convenção de nome.** Um Model/DTO do Núcleo (`core.*`) nunca pode importar um DTO de uma vertical (`verticals.*`) — isso inverteria a regra de dependência do projeto (`verticals.* → core`, nunca o contrário; ver seção "Verticals" do `CLAUDE.md`). Quando o DTO que "converteria" pertence à vertical (ex.: `PacienteCreateRequestDTO` mistura campos do `Paciente` do Núcleo com campos de `PerfilNutricional` da vertical Nutrição), o método de conversão fica no DTO (que já pode depender do Model do Núcleo), nunca no Model do Núcleo.

**Lógica que depende de um bean do Spring não migra para o Model/DTO.** Model/DTO são POJOs simples, sem injeção de dependência. Cálculo que precisa de um calculator injetado, senha que precisa de `PasswordEncoder`, token que precisa de `JwtService` — isso continua no Service, que computa o valor e passa como parâmetro pronto pro método de conversão (ex.: `AutenticacaoService` calcula `jwtService.generateToken(usuario)` e só depois chama `AutenticacaoResponseDTO.of(token, usuario)`; `CadastroRequestDTO.toUsuario(senhaCriptografada, perfil, ativo)` recebe a senha já criptografada, não criptografa ela mesma).

**Exceção aceita: `ModelMapper` em métodos de atualização campo-a-campo puros.** Quando um método `atualizar(entidade)` só copia campos com nome e tipo idênticos entre DTO e entidade (sem regra de negócio, sem transformação), pode usar `modelMapper.map(this, entidade)` em vez de setters manuais (ver `PacienteUpdateRequestDTO.atualizar(Paciente, ModelMapper)` / `.atualizar(PerfilNutricional, ModelMapper)`). Como `ModelMapper` é um bean do Spring, o método recebe ele como parâmetro (quem chama, no Service, já tem o bean injetado) — o DTO continua sem `@Autowired`/injeção própria. **Risco assumido conscientemente:** mapeamento por reflection não dá erro de compilação se um campo for renomeado só de um lado — passa a ignorar o campo silenciosamente. Use só quando os nomes dos campos já são idênticos hoje; se a lógica de update tiver qualquer regra além de copiar campo, volte para setters explícitos.

## Migrations (Flyway)

Use `./gerar_migration <nome>` (documentado no CLAUDE.md) para o arquivo — dentro dele:
- Tabela em `snake_case` plural.
- Toda tabela própria: `id BIGSERIAL PRIMARY KEY`, mais o padrão de auditoria do `AbstractModel` (`created_at`, `updated_at` `TIMESTAMP NOT NULL`).
- FK explícita com `REFERENCES` quando o relacionamento existir na entidade.
- Índice em toda coluna usada em `WHERE`/join fora da PK (ex.: FK, coluna de busca).
- Nunca edite uma migration que já rodou — para corrigir algo já aplicado, crie uma nova migration.

## Padrões de comportamento — só quando a complexidade pedir

Não implemente nada disto especulativamente; são para quando o requisito realmente exigir:

- **Strategy** — quando existir mais de um "tipo" de algo que precisa de tratamento diferente sem `if/else`/`switch` crescendo (ex.: múltiplos tipos de notificação, múltiplos cálculos de plano). Uma interface comum + uma implementação por tipo + um coordenador que escolhe pelo tipo.
- **Chain of Responsibility** — quando um fluxo tem várias etapas independentes e opcionais em sequência, cada uma decidindo se se aplica (ex.: pipeline de validações/enriquecimento de um cadastro complexo). Cada elo expõe `deveProcessar(...)` e `processar(...)`.
- **Observer (Spring `ApplicationEventPublisher`/`@EventListener`)** — quando uma ação precisa disparar efeitos colaterais que não fazem parte do fluxo principal e não devem bloquear a resposta (ex.: enviar e-mail depois de criar paciente). Publica um evento no fim do método de service, quem escuta fica em outra classe.

### Nomenclatura de classes de design pattern

Toda classe/interface que participa de um desses padrões carrega o **nome do padrão em inglês como sufixo**, depois do nome do papel/domínio (que continua em português, como o resto do projeto — ver "Tradução" no `CLAUDE.md`). O sufixo identifica o padrão; nunca traduza nem abrevie essa parte.

- Strategy → `<Papel>Strategy` — ex.: `VerticalStrategy` (interface), `NutricaoStrategy` (implementação)
- Registry → `<Papel>Registry` — ex.: `VerticalRegistry` (coleta/indexa toda implementação de `VerticalStrategy`, keyed por `Especialidade`)
- Chain of Responsibility → `<Papel>Chain` — ex.: `CadastroPacienteChain`, elos individuais `<Regra>ChainLink` ou `<Regra>Chain` quando cada elo já é a etapa nomeada
- Observer → eventos como `<Fato>Event` (ex.: `PacienteCriadoEvent`); o listener como `<Fato>Listener`
- Qualquer outro padrão do catálogo (Factory, Builder, Decorator, Adapter, etc.), se um dia for introduzido: mesma regra — sufixo em inglês igual ao nome do padrão em inglês.

## Fora de escopo neste projeto

`docs/roteirojava` descreve um ecossistema de microsserviços (RabbitMQ, gRPC, Redis, distributed lock, multi-tenant via `ContextWrapper`/JWT com `idEmpresa`, múltiplos profiles Spring por modo de execução). **Nada disso existe no Nuvexa hoje** — é um único módulo Spring Boot com Postgres. Não adicione essas dependências/infraestrutura a menos que o usuário peça explicitamente; se o padrão pedido esbarrar em algo daquela lista, pare e confirme antes de introduzir a peça de infraestrutura.

## Validação

Depois de qualquer mudança de backend, rode `./mvnw test` (sobe contra o Postgres do `docker compose`, não é embutido) — ver comandos no CLAUDE.md. `./mvnw compile` sozinho já revalida as Q-classes do QueryDSL se você alterou uma entidade.
