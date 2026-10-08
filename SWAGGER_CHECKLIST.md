# Checklist de documentação Swagger/OpenAPI

Inventário de todos os endpoints do backend (`GET /api/v1/*`), gerado a partir dos
`@RestController` existentes. Legenda:

- **Documentado**: tem entrada em `backend/src/main/resources/openapi/descriptions.yml` (summary +
  description + parâmetros/respostas relevantes) e, quando o corpo da requisição não é óbvio,
  exemplo de payload via `@Schema(example = ...)` no DTO.
- **Testado manualmente**: validado chamando o endpoint pela Swagger UI (`/swagger-ui.html`) no
  ambiente de dev.

Progresso é atualizado incrementalmente, controller por controller, com revisão antes de avançar
para o próximo (ver processo combinado na conversa).

## Pacientes — `PacienteController`

| Método | Path | Documentado | Testado manualmente |
| --- | --- | --- | --- |
| GET | /api/v1/pacientes | ✅ | ⬜ |
| GET | /api/v1/pacientes/{id} | ✅ | ⬜ |
| GET | /api/v1/pacientes/enums | ✅ | ⬜ |
| POST | /api/v1/pacientes | ✅ | ⬜ |
| PUT | /api/v1/pacientes/{id} | ✅ | ⬜ |

## Relatório de pacientes — `PacienteRelatorioController`

| Método | Path | Documentado | Testado manualmente |
| --- | --- | --- | --- |
| GET | /api/v1/pacientes/relatorio | ✅ | ⬜ |
| GET | /api/v1/pacientes/relatorio/excel | ✅ | ⬜ |

## Perfil nutricional — `PerfilNutricionalController`

| Método | Path | Documentado | Testado manualmente |
| --- | --- | --- | --- |
| GET | /api/v1/perfis-nutricionais | ✅ | ⬜ |
| GET | /api/v1/pacientes/{pacienteId}/perfil-nutricional | ✅ | ⬜ |
| GET | /api/v1/perfis-nutricionais/enums | ✅ | ⬜ |
| POST | /api/v1/pacientes/{pacienteId}/perfil-nutricional | ✅ | ⬜ |
| PUT | /api/v1/pacientes/{pacienteId}/perfil-nutricional | ✅ | ⬜ |
| DELETE | /api/v1/pacientes/{pacienteId}/perfil-nutricional | ✅ | ⬜ |

## Avaliações — `AvaliacaoController`

| Método | Path | Documentado | Testado manualmente |
| --- | --- | --- | --- |
| GET | /api/v1/avaliacoes | ✅ | ⬜ |
| GET | /api/v1/avaliacoes/{id} | ✅ | ⬜ |
| GET | /api/v1/avaliacoes/enums | ✅ | ⬜ |
| POST | /api/v1/avaliacoes | ✅ | ⬜ |
| PUT | /api/v1/avaliacoes/{id} | ✅ | ⬜ |
| DELETE | /api/v1/avaliacoes/{id} | ✅ | ⬜ |

## Relatório de avaliações — `AvaliacaoRelatorioController`

| Método | Path | Documentado | Testado manualmente |
| --- | --- | --- | --- |
| GET | /api/v1/avaliacoes/relatorio | ✅ | ⬜ |
| GET | /api/v1/avaliacoes/relatorio/excel | ✅ | ⬜ |

## Planos alimentares — `PlanoAlimentarController`

| Método | Path | Documentado | Testado manualmente |
| --- | --- | --- | --- |
| GET | /api/v1/planos-alimentares | ✅ | ⬜ |
| GET | /api/v1/planos-alimentares/{id} | ✅ | ⬜ |
| GET | /api/v1/planos-alimentares/enums | ✅ | ⬜ |
| POST | /api/v1/planos-alimentares | ✅ | ⬜ |
| PUT | /api/v1/planos-alimentares/{id} | ✅ | ⬜ |
| DELETE | /api/v1/planos-alimentares/{id} | ✅ | ⬜ |

## Relatório de planos alimentares — `PlanoAlimentarRelatorioController`

| Método | Path | Documentado | Testado manualmente |
| --- | --- | --- | --- |
| GET | /api/v1/planos-alimentares/relatorio | ✅ | ⬜ |
| GET | /api/v1/planos-alimentares/relatorio/excel | ✅ | ⬜ |

## Consultas — `ConsultaController`

| Método | Path | Documentado | Testado manualmente |
| --- | --- | --- | --- |
| GET | /api/v1/consultas | ✅ | ⬜ |
| GET | /api/v1/consultas/{id} | ✅ | ⬜ |
| GET | /api/v1/consultas/profissionais | ✅ | ⬜ |
| POST | /api/v1/consultas | ✅ | ⬜ |
| PUT | /api/v1/consultas/{id} | ✅ | ⬜ |
| DELETE | /api/v1/consultas/{id} | ✅ | ⬜ |

## Relatório de consultas — `ConsultaRelatorioController`

| Método | Path | Documentado | Testado manualmente |
| --- | --- | --- | --- |
| GET | /api/v1/consultas/relatorio | ✅ | ⬜ |
| GET | /api/v1/consultas/relatorio/excel | ✅ | ⬜ |

## Prontuários — `ProntuarioController`

| Método | Path | Documentado | Testado manualmente |
| --- | --- | --- | --- |
| GET | /api/v1/prontuarios | ✅ | ⬜ |
| GET | /api/v1/prontuarios/{id} | ✅ | ⬜ |
| GET | /api/v1/prontuarios/enums | ✅ | ⬜ |
| POST | /api/v1/prontuarios | ✅ | ⬜ |
| PUT | /api/v1/prontuarios/{id} | ✅ | ⬜ |
| DELETE | /api/v1/prontuarios/{id} | ✅ | ⬜ |
| PATCH | /api/v1/prontuarios/{id}/assinar | ✅ | ⬜ |
| GET | /api/v1/prontuarios/{id}/auditoria | ✅ | ⬜ |

## Adendos de prontuário — `ProntuarioAdendoController`

| Método | Path | Documentado | Testado manualmente |
| --- | --- | --- | --- |
| GET | /api/v1/prontuarios/{prontuarioId}/adendos | ✅ | ⬜ |
| POST | /api/v1/prontuarios/{prontuarioId}/adendos | ✅ | ⬜ |

## Anexos de prontuário — `ProntuarioAnexoController`

| Método | Path | Documentado | Testado manualmente |
| --- | --- | --- | --- |
| GET | /api/v1/prontuarios/{prontuarioId}/anexos | ✅ | ⬜ |
| POST | /api/v1/prontuarios/{prontuarioId}/anexos (multipart) | ✅ | ⬜ |
| GET | /api/v1/prontuarios/{prontuarioId}/anexos/{anexoId}/download | ✅ | ⬜ |
| DELETE | /api/v1/prontuarios/{prontuarioId}/anexos/{anexoId} | ✅ | ⬜ |

## Relatório de prontuários — `ProntuarioRelatorioController`

| Método | Path | Documentado | Testado manualmente |
| --- | --- | --- | --- |
| GET | /api/v1/prontuarios/relatorio | ✅ | ⬜ |
| GET | /api/v1/prontuarios/relatorio/excel | ✅ | ⬜ |

## Contexto — `ContextoController`

| Método | Path | Documentado | Testado manualmente |
| --- | --- | --- | --- |
| GET | /api/v1/contexto | ✅ | ⬜ |

## Menus — `MenuController`

| Método | Path | Documentado | Testado manualmente |
| --- | --- | --- | --- |
| GET | /api/v1/menus | ✅ | ⬜ |

## Autenticação — `AutenticacaoController`

| Método | Path | Documentado | Testado manualmente |
| --- | --- | --- | --- |
| POST | /api/v1/auth/cadastrar | ✅ | ⬜ |
| POST | /api/v1/auth/login | ✅ | ⬜ |
| POST | /api/v1/auth/esqueci-senha | ✅ | ⬜ |
| POST | /api/v1/auth/redefinir-senha | ✅ | ⬜ |

---

**Resumo:** 58/58 endpoints documentados em `descriptions.yml` (todos os 17 controllers). Nenhum
endpoint testado manualmente ainda — próximo passo é subir o backend (perfil `dev` ou local) e
percorrer a Swagger UI (`/swagger-ui.html`) endpoint por endpoint, marcando a coluna "Testado
manualmente" conforme validado.

Ver `BUGS_ENCONTRADOS.md` para achados de comportamento (não corrigidos) encontrados durante a
auditoria.
