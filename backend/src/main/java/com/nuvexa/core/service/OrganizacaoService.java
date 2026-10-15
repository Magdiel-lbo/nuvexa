package com.nuvexa.core.service;

import com.nuvexa.core.dto.request.ConviteProprietarioRequestDTO;
import com.nuvexa.core.dto.request.OrganizacaoCreateRequestDTO;
import com.nuvexa.core.dto.request.OrganizacaoUpdateRequestDTO;
import com.nuvexa.core.dto.response.ConviteResponseDTO;
import com.nuvexa.core.dto.response.OrganizacaoEnumsResponseDTO;
import com.nuvexa.core.dto.response.OrganizacaoResponseDTO;
import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.repository.OrganizacaoRepository;
import com.nuvexa.core.repository.VinculoRepository;
import com.nuvexa.platform.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Gestão de empresas (organizações) pela plataforma. Diferente dos demais services, NÃO é
 * escopado pela organização atual: o ADMIN opera sobre todas. O portão de papel
 * ({@code hasRole('ADMIN')}) fica no controller.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class OrganizacaoService {

    private final OrganizacaoRepository organizacaoRepository;
    private final VinculoRepository vinculoRepository;
    private final ConviteService conviteService;
    private final ContextoDeAutenticacao contextoDeAutenticacao;
    private final MessageSourceAccessor mensagens;

    public OrganizacaoResponseDTO create(OrganizacaoCreateRequestDTO request) {
        String documento = normalizar(request.getDocumento());
        if (documento != null && organizacaoRepository.existsByDocumento(documento)) {
            throw documentoDuplicado();
        }
        Organizacao salva = organizacaoRepository.save(request.toOrganizacao(normalizar(request.getRazaoSocial()), documento));
        log.info("Organização criada com id={}", salva.getId());
        String emailProprietario = normalizar(request.getEmailProprietario());
        if (emailProprietario != null) {
            conviteService.convidarProprietarioComoAdmin(salva, emailProprietario);
        }
        return OrganizacaoResponseDTO.from(salva, false);
    }

    /** Convida (ou reenvia o convite a) um PROPRIETARIO da empresa — como o ADMIN não faz parte dela, é o caminho para o dono entrar. */
    public ConviteResponseDTO convidarProprietario(Long organizacaoId, ConviteProprietarioRequestDTO request) {
        return conviteService.convidarProprietarioComoAdmin(buscarOuFalhar(organizacaoId), request.getEmail());
    }

    public OrganizacaoResponseDTO update(Long id, OrganizacaoUpdateRequestDTO request) {
        Organizacao organizacao = buscarOuFalhar(id);
        String documento = normalizar(request.getDocumento());
        if (documento != null && organizacaoRepository.existsByDocumentoAndIdNot(documento, id)) {
            throw documentoDuplicado();
        }
        organizacao.setNome(request.getNome().trim());
        organizacao.setRazaoSocial(normalizar(request.getRazaoSocial()));
        organizacao.setDocumento(documento);
        Organizacao salva = organizacaoRepository.save(organizacao);
        log.info("Organização atualizada com id={}", id);
        return OrganizacaoResponseDTO.from(salva);
    }

    @Transactional(readOnly = true)
    public OrganizacaoResponseDTO findById(Long id) {
        Organizacao organizacao = buscarOuFalhar(id);
        return OrganizacaoResponseDTO.from(organizacao, vinculoRepository.idsDeOrganizacoesComProprietarioAtivo().contains(id));
    }

    @Transactional(readOnly = true)
    public List<OrganizacaoResponseDTO> findAll(String busca) {
        Set<Long> comProprietario = vinculoRepository.idsDeOrganizacoesComProprietarioAtivo();
        return organizacaoRepository.buscar(normalizar(busca)).stream()
                .map(organizacao -> OrganizacaoResponseDTO.from(organizacao, comProprietario.contains(organizacao.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrganizacaoResponseDTO atual() {
        return findById(contextoDeAutenticacao.organizacaoAtualId());
    }

    public OrganizacaoResponseDTO atualizarAtual(OrganizacaoUpdateRequestDTO request) {
        return update(contextoDeAutenticacao.organizacaoAtualId(), request);
    }

    public OrganizacaoEnumsResponseDTO getEnums() {
        return OrganizacaoEnumsResponseDTO.of();
    }

    private Organizacao buscarOuFalhar(Long id) {
        return organizacaoRepository.findById(id)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, mensagens.getMessage("organizacao.naoEncontrada", new Object[]{id})));
    }

    private NegocioException documentoDuplicado() {
        return new NegocioException(HttpStatus.CONFLICT, mensagens.getMessage("organizacao.documento.duplicado"));
    }

    private String normalizar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
