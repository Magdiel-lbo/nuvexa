package com.nuvexa.nutricao.service;

import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.service.OrganizacaoScopedContext;
import com.nuvexa.core.service.ValidadorOrganizacional;
import com.nuvexa.nutricao.dto.request.PlanoAlimentarCreateRequestDTO;
import com.nuvexa.nutricao.dto.request.PlanoAlimentarUpdateRequestDTO;
import com.nuvexa.nutricao.dto.response.PlanoAlimentarEnumsResponseDTO;
import com.nuvexa.nutricao.dto.response.PlanoAlimentarResponseDTO;
import com.nuvexa.nutricao.model.PlanoAlimentar;
import com.nuvexa.nutricao.model.QPlanoAlimentar;
import com.nuvexa.nutricao.model.StatusPlanoAlimentar;
import com.nuvexa.nutricao.repository.PlanoAlimentarRepository;
import com.nuvexa.platform.exception.NegocioException;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class PlanoAlimentarService {

    private final PlanoAlimentarRepository planoAlimentarRepository;
    private final ModelMapper modelMapper;
    private final OrganizacaoScopedContext contexto;
    private final ValidadorOrganizacional validadorOrganizacional;

    public PlanoAlimentarResponseDTO create(PlanoAlimentarCreateRequestDTO request) {
        Paciente paciente = buscarPacienteOuFalhar(request.getPacienteId());
        Usuario autor = buscarAutorOuFalhar(request.getAutorId());

        PlanoAlimentar planoAlimentar = planoAlimentarRepository.save(
                request.toPlanoAlimentar(contexto.getContextoDeAutenticacao().organizacaoAtual(), paciente, autor));
        log.info("Plano alimentar criado com id={}", planoAlimentar.getId());
        return PlanoAlimentarResponseDTO.from(planoAlimentar);
    }

    public PlanoAlimentarResponseDTO update(Long id, PlanoAlimentarUpdateRequestDTO request) {
        PlanoAlimentar planoAlimentar = buscarPlanoAlimentarOuFalhar(id);
        garantirEditavel(planoAlimentar);
        garantirTransicaoValida(planoAlimentar.getStatus(), request.getStatus());
        Usuario autor = buscarAutorOuFalhar(request.getAutorId());
        request.atualizar(planoAlimentar, autor, modelMapper);

        PlanoAlimentar saved = planoAlimentarRepository.save(planoAlimentar);
        log.info("Plano alimentar atualizado com id={}", id);
        return PlanoAlimentarResponseDTO.from(saved);
    }

    public PlanoAlimentarResponseDTO findById(Long id) {
        return PlanoAlimentarResponseDTO.from(buscarPlanoAlimentarOuFalhar(id));
    }

    public List<PlanoAlimentarResponseDTO> findAll(Long pacienteId, String busca) {
        return buscarPlanos(pacienteId, normalizarBusca(busca)).stream()
                .map(PlanoAlimentarResponseDTO::from)
                .toList();
    }

    public PlanoAlimentarEnumsResponseDTO getEnums() {
        return PlanoAlimentarEnumsResponseDTO.of();
    }

    public void delete(Long id) {
        PlanoAlimentar planoAlimentar = buscarPlanoAlimentarOuFalhar(id);
        planoAlimentarRepository.delete(planoAlimentar);
        log.info("Plano alimentar removido com id={}", id);
    }

    private List<PlanoAlimentar> buscarPlanos(Long pacienteId, String busca) {
        QPlanoAlimentar planoAlimentar = QPlanoAlimentar.planoAlimentar;

        BooleanExpression filtroOrganizacao = planoAlimentar.organizacao.id.eq(contexto.getContextoDeAutenticacao().organizacaoAtualId());
        BooleanExpression filtroPaciente = pacienteId == null ? null : planoAlimentar.paciente.id.eq(pacienteId);
        BooleanExpression filtroBusca = busca == null
                ? null
                : planoAlimentar.nome.containsIgnoreCase(busca).or(planoAlimentar.paciente.nome.containsIgnoreCase(busca));

        return contexto.getQueryFactory()
                .selectFrom(planoAlimentar)
                .join(planoAlimentar.paciente).fetchJoin()
                .join(planoAlimentar.autor).fetchJoin()
                .where(filtroOrganizacao, filtroPaciente, filtroBusca)
                .orderBy(planoAlimentar.atualizadoEm.desc())
                .fetch();
    }

    private String normalizarBusca(String busca) {
        return (busca == null || busca.isBlank()) ? null : busca.trim();
    }

    /**
     * ENCERRADO é status terminal — sem transição de saída. Bloqueia o update inteiro nesse
     * caso, mesmo padrão de {@code ProntuarioService.garantirEditavel}.
     */
    private void garantirEditavel(PlanoAlimentar planoAlimentar) {
        if (planoAlimentar.getStatus() == StatusPlanoAlimentar.ENCERRADO) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("planoAlimentar.encerrado.imutavel"));
        }
    }

    /**
     * Única transição proibida fora do caso terminal (ENCERRADO, já barrado por
     * {@link #garantirEditavel}): voltar de ATIVO para RASCUNHO. As demais combinações entre
     * RASCUNHO/ATIVO/ENCERRADO são livres.
     */
    private void garantirTransicaoValida(StatusPlanoAlimentar atual, StatusPlanoAlimentar novo) {
        if (atual == StatusPlanoAlimentar.ATIVO && novo == StatusPlanoAlimentar.RASCUNHO) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("planoAlimentar.transicao.invalida"));
        }
    }

    private Paciente buscarPacienteOuFalhar(Long pacienteId) {
        return validadorOrganizacional.pacienteDaOrganizacao(pacienteId, contexto.getContextoDeAutenticacao().organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("paciente.naoEncontrado", pacienteId)));
    }

    /**
     * Autor válido = Usuario com Vinculo ativo na organização atual — mesma regra de
     * {@code ProntuarioService.buscarAutorOuFalhar}.
     */
    private Usuario buscarAutorOuFalhar(Long autorId) {
        return validadorOrganizacional.usuarioAtivoNaOrganizacao(autorId, contexto.getContextoDeAutenticacao().organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.BAD_REQUEST,
                        resolveMessage("planoAlimentar.autor.invalido", autorId)));
    }

    private PlanoAlimentar buscarPlanoAlimentarOuFalhar(Long id) {
        return planoAlimentarRepository
                .findByIdAndOrganizacaoId(id, contexto.getContextoDeAutenticacao().organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, resolveMessage("planoAlimentar.naoEncontrado", id)));
    }

    private String resolveMessage(String key, Object... args) {
        return contexto.getMensagens().getMessage(key, args);
    }
}
