package com.nuvexa.core.service;

import com.nuvexa.core.dto.request.ConviteAceiteRequestDTO;
import com.nuvexa.core.dto.request.ConviteCreateRequestDTO;
import com.nuvexa.core.dto.response.AutenticacaoResponseDTO;
import com.nuvexa.core.dto.response.ConviteResponseDTO;
import com.nuvexa.core.model.Convite;
import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.PapelOrganizacional;
import com.nuvexa.core.model.Perfil;
import com.nuvexa.core.model.StatusConvite;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.core.repository.ConviteRepository;
import com.nuvexa.core.repository.UsuarioRepository;
import com.nuvexa.core.repository.VinculoRepository;
import com.nuvexa.platform.auditoria.AuditoriaService;
import com.nuvexa.platform.auditoria.AutenticacaoAuditoriaService;
import com.nuvexa.platform.auditoria.EntidadeAuditavel;
import com.nuvexa.platform.auditoria.TipoEventoAuditoria;
import com.nuvexa.platform.auditoria.TipoEventoAutenticacao;
import com.nuvexa.platform.email.EmailService;
import com.nuvexa.platform.exception.NegocioException;
import com.nuvexa.platform.security.JwtService;
import com.nuvexa.platform.security.LimiteDeTentativas;
import com.nuvexa.platform.security.TokenOpaco;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entrada de novas pessoas em uma organização existente. Só convida e-mail SEM conta: a conta é
 * criada no aceite, já vinculada à organização de quem convidou (sem organização individual
 * própria). Quem já tem conta fica de fora até existir escolha de organização na requisição —
 * hoje {@code ContextoDeAutenticacao.vinculoAtual()} sempre usa o vínculo mais antigo.
 *
 * <p>Gerenciar convites exige {@code GERENCIAR_EQUIPE} (checado no controller) e ninguém concede
 * papel acima do próprio ({@link PapelOrganizacional#podeConceder}).
 */
@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class ConviteService {

    static final long VALIDADE_DIAS = 7;

    private final ConviteRepository conviteRepository;
    private final UsuarioRepository usuarioRepository;
    private final VinculoRepository vinculoRepository;
    private final ContextoDeAutenticacao contextoDeAutenticacao;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AuditoriaService auditoriaService;
    private final AutenticacaoAuditoriaService autenticacaoAuditoria;
    private final LimiteDeTentativas limiteDeTentativas;
    private final SessaoService sessaoService;
    private final MessageSourceAccessor mensagens;

    @Value("${app.cors.allowed-origin}")
    private String frontendUrl;

    public ConviteResponseDTO convidar(ConviteCreateRequestDTO request) {
        Vinculo vinculoDoConvidador = contextoDeAutenticacao.vinculoAtual();
        garantirPodeConceder(vinculoDoConvidador.getPapel(), request.getPapel());

        String email = request.getEmail().trim();
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new NegocioException(HttpStatus.CONFLICT, mensagens.getMessage("convite.email.jaTemConta", new Object[]{email}));
        }

        Organizacao organizacao = vinculoDoConvidador.getOrganizacao();
        LocalDateTime agora = LocalDateTime.now();
        conviteRepository.findByOrganizacaoIdAndEmailAndUsadoEmIsNullAndRevogadoEmIsNull(organizacao.getId(), email)
                .forEach(pendente -> pendente.setRevogadoEm(agora));

        String token = TokenOpaco.gerar();
        Convite convite = conviteRepository.save(Convite.builder()
                .organizacao(organizacao)
                .email(email)
                .papel(request.getPapel())
                .hashToken(TokenOpaco.hash(token))
                .expiraEm(agora.plusDays(VALIDADE_DIAS))
                .convidadoPor(contextoDeAutenticacao.usuarioAtual())
                .build());

        Usuario convidador = contextoDeAutenticacao.usuarioAtual();
        auditoriaService.registrar(EntidadeAuditavel.CONVITE, convite.getId(), TipoEventoAuditoria.CRIACAO, organizacao.getId(),
                convidador.getId(), convidador.getNome(), null, descrever(convite));

        String link = frontendUrl + "/aceitar-convite?token=" + token;
        emailService.enviar(email, "Convite para a " + organizacao.getNome() + " — Nuvexa",
                "<p>Você foi convidado(a) para participar de <b>" + escapar(organizacao.getNome()) + "</b> na Nuvexa.</p>"
                        + "<p><a href=\"" + link + "\">Clique aqui para aceitar o convite e criar sua conta</a></p>"
                        + "<p>Esse link expira em " + VALIDADE_DIAS + " dias e só pode ser usado uma vez.</p>");

        log.info("Convite criado com id={} para a organização id={}", convite.getId(), organizacao.getId());
        return ConviteResponseDTO.from(convite);
    }

    @Transactional(readOnly = true)
    public List<ConviteResponseDTO> listar() {
        return conviteRepository.findByOrganizacaoIdOrderByCriadoEmDesc(contextoDeAutenticacao.organizacaoAtualId()).stream()
                .map(ConviteResponseDTO::from)
                .toList();
    }

    public void revogar(Long id) {
        Convite convite = conviteRepository.findByIdAndOrganizacaoId(id, contextoDeAutenticacao.organizacaoAtualId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, mensagens.getMessage("convite.naoEncontrado", new Object[]{id})));
        garantirPodeConceder(contextoDeAutenticacao.vinculoAtual().getPapel(), convite.getPapel());

        if (convite.status(LocalDateTime.now()) == StatusConvite.ACEITO) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, mensagens.getMessage("convite.jaAceito"));
        }
        if (convite.getRevogadoEm() == null) {
            convite.setRevogadoEm(LocalDateTime.now());
            Usuario revogador = contextoDeAutenticacao.usuarioAtual();
            auditoriaService.registrar(EntidadeAuditavel.CONVITE, convite.getId(), TipoEventoAuditoria.REVOGACAO,
                    convite.getOrganizacao().getId(), revogador.getId(), revogador.getNome(), descrever(convite), null);
        }
        log.info("Convite revogado com id={}", id);
    }

    /** Público (sem login): o token do e-mail é a credencial. Qualquer falha de validade responde igual. */
    public AutenticacaoResponseDTO aceitar(ConviteAceiteRequestDTO request) {
        limiteDeTentativas.garantirTokenPermitido();
        Convite convite = conviteRepository.findByHashToken(TokenOpaco.hash(request.getToken()))
                .filter(c -> c.status(LocalDateTime.now()) == StatusConvite.PENDENTE)
                .orElseThrow(() -> {
                    limiteDeTentativas.registrarTokenInvalido();
                    return new NegocioException(HttpStatus.BAD_REQUEST, mensagens.getMessage("convite.invalido"));
                });

        Usuario usuario;
        try {
            usuario = usuarioRepository.saveAndFlush(Usuario.builder()
                    .nome(request.getNome().trim())
                    .email(convite.getEmail())
                    .senha(passwordEncoder.encode(request.getSenha()))
                    .perfil(Perfil.PROFISSIONAL)
                    .ativo(true)
                    .build());
        } catch (DataIntegrityViolationException ex) {
            throw new NegocioException(HttpStatus.CONFLICT, mensagens.getMessage("autenticacao.emailEmUso", new Object[]{convite.getEmail()}));
        }

        vinculoRepository.save(Vinculo.builder()
                .usuario(usuario)
                .organizacao(convite.getOrganizacao())
                .papel(convite.getPapel())
                .ativo(true)
                .build());
        convite.setUsadoEm(LocalDateTime.now());
        auditoriaService.registrar(EntidadeAuditavel.CONVITE, convite.getId(), TipoEventoAuditoria.CONVITE_ACEITO,
                convite.getOrganizacao().getId(), usuario.getId(), usuario.getNome(), null, descrever(convite));
        autenticacaoAuditoria.registrarNaTransacaoAtual(TipoEventoAutenticacao.CADASTRO_POR_CONVITE, usuario.getId(), usuario.getEmail());

        log.info("Convite id={} aceito; usuário id={} vinculado à organização id={}", convite.getId(), usuario.getId(), convite.getOrganizacao().getId());
        SessaoService.Emissao emissao = sessaoService.iniciar(usuario);
        return AutenticacaoResponseDTO.of(jwtService.generateToken(usuario, emissao.sessao().getId()),
                emissao.refreshToken(), jwtService.getExpiracaoSegundos(), usuario);
    }

    private void garantirPodeConceder(PapelOrganizacional papelDoConvidador, PapelOrganizacional papelConcedido) {
        if (!papelDoConvidador.podeConceder(papelConcedido)) {
            throw new NegocioException(HttpStatus.FORBIDDEN, mensagens.getMessage("convite.papel.naoPodeConceder", new Object[]{papelConcedido}));
        }
    }

    private String descrever(Convite convite) {
        return "email=%s, papel=%s".formatted(convite.getEmail(), convite.getPapel());
    }

    private String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
