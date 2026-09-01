package com.nuvexa.core.service;

import com.nuvexa.core.dto.request.EsqueciSenhaRequestDTO;
import com.nuvexa.core.dto.request.LoginRequestDTO;
import com.nuvexa.core.dto.request.CadastroRequestDTO;
import com.nuvexa.core.dto.request.RedefinirSenhaRequestDTO;
import com.nuvexa.core.dto.response.AutenticacaoResponseDTO;
import com.nuvexa.core.dto.response.MensagemResponseDTO;
import com.nuvexa.platform.email.EmailService;
import com.nuvexa.platform.exception.NegocioException;
import com.nuvexa.core.model.Perfil;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.repository.UsuarioRepository;
import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.StatusOrganizacao;
import com.nuvexa.core.model.TipoOrganizacao;
import com.nuvexa.core.repository.OrganizacaoRepository;
import com.nuvexa.core.model.PapelOrganizacional;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.core.repository.VinculoRepository;
import com.nuvexa.platform.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class AutenticacaoService {

    private static final Locale MESSAGE_LOCALE = Locale.of("pt", "BR");
    private static final int RESET_TOKEN_BYTES = 32;
    private static final long RESET_TOKEN_VALIDITY_MINUTES = 30;

    private final UsuarioRepository usuarioRepository;
    private final OrganizacaoRepository organizacaoRepository;
    private final VinculoRepository vinculoRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MessageSource messageSource;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.cors.allowed-origin}")
    private String frontendUrl;

    public AutenticacaoResponseDTO cadastrar(CadastroRequestDTO request) {
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new NegocioException(HttpStatus.CONFLICT, resolveMessage("autenticacao.emailEmUso", request.getEmail()));
        }

        Usuario usuario = request.toUsuario(passwordEncoder.encode(request.getSenha()), Perfil.PROFISSIONAL, true);

        Usuario saved;
        try {
            saved = usuarioRepository.save(usuario);
        } catch (DataIntegrityViolationException ex) {
            // Duas requisições de registro concorrentes para o mesmo e-mail podem passar
            // ambas pela checagem acima antes de qualquer uma commitar; quem chegar
            // depois esbarra na constraint UNIQUE do banco. Trata como o mesmo conflito
            // de negócio, em vez de vazar um erro 500 genérico.
            throw new NegocioException(HttpStatus.CONFLICT, resolveMessage("autenticacao.emailEmUso", request.getEmail()));
        }
        log.info("Usuário registrado com id={}", saved.getId());

        provisionarOrganizacaoInicial(saved);

        return buildAuthResponse(saved);
    }

    public AutenticacaoResponseDTO login(LoginRequestDTO request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha()));
        } catch (AuthenticationException ex) {
            throw new NegocioException(HttpStatus.UNAUTHORIZED, resolveMessage("autenticacao.credenciaisInvalidas"));
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NegocioException(HttpStatus.UNAUTHORIZED, resolveMessage("autenticacao.credenciaisInvalidas")));

        log.info("Usuário autenticado com id={}", usuario.getId());
        return buildAuthResponse(usuario);
    }

    public MensagemResponseDTO esqueciSenha(EsqueciSenhaRequestDTO request) {
        Optional<Usuario> userOptional = usuarioRepository.findByEmail(request.getEmail());

        if (userOptional.isPresent()) {
            Usuario usuario = userOptional.get();
            String rawToken = generateRawToken();

            usuario.setHashTokenRedefinicao(hashToken(rawToken));
            usuario.setTokenRedefinicaoExpiraEm(LocalDateTime.now().plusMinutes(RESET_TOKEN_VALIDITY_MINUTES));
            usuarioRepository.save(usuario);

            String resetLink = frontendUrl + "/redefinir-senha?token=" + rawToken;
            emailService.enviar(
                    usuario.getEmail(),
                    "Redefinição de senha — Nuvexa",
                    "<p>Recebemos uma solicitação para redefinir sua senha.</p>"
                            + "<p><a href=\"" + resetLink + "\">Clique aqui para redefinir sua senha</a></p>"
                            + "<p>Esse link expira em " + RESET_TOKEN_VALIDITY_MINUTES + " minutos. "
                            + "Se você não solicitou isso, ignore este e-mail.</p>"
            );
            log.info("E-mail de recuperação de senha disparado para usuário id={}", usuario.getId());
        } else {
            log.info("Recuperação de senha solicitada para e-mail não cadastrado: {}", request.getEmail());
        }

        return new MensagemResponseDTO(resolveMessage("autenticacao.esqueciSenha.enviado"));
    }

    public MensagemResponseDTO redefinirSenha(RedefinirSenhaRequestDTO request) {
        Usuario usuario = usuarioRepository.findByHashTokenRedefinicao(hashToken(request.getToken()))
                .filter(u -> u.getTokenRedefinicaoExpiraEm() != null && u.getTokenRedefinicaoExpiraEm().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("autenticacao.token.invalido")));

        usuario.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        usuario.setHashTokenRedefinicao(null);
        usuario.setTokenRedefinicaoExpiraEm(null);
        usuarioRepository.save(usuario);

        log.info("Senha redefinida para usuário com id={}", usuario.getId());
        return new MensagemResponseDTO(resolveMessage("autenticacao.redefinirSenha.sucesso"));
    }

    /**
     * Todo usuário novo nasce como PROPRIETARIO de uma organização individual própria — sem isso
     * ele autenticaria mas não teria escopo nenhum, e nenhum endpoint de paciente funcionaria.
     * A entrada em organizações existentes (convite) é assunto de uma fase posterior.
     */
    private void provisionarOrganizacaoInicial(Usuario usuario) {
        Organizacao organizacao = organizacaoRepository.save(Organizacao.builder()
                .nome(usuario.getNome())
                .tipo(TipoOrganizacao.INDIVIDUAL)
                .status(StatusOrganizacao.ATIVA)
                .build());

        vinculoRepository.save(Vinculo.builder()
                .usuario(usuario)
                .organizacao(organizacao)
                .papel(PapelOrganizacional.PROPRIETARIO)
                .ativo(true)
                .build());

        log.info("Organização inicial criada com id={} para usuário id={}", organizacao.getId(), usuario.getId());
    }

    private String generateRawToken() {
        byte[] bytes = new byte[RESET_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Algoritmo de hash indisponível", ex);
        }
    }

    private AutenticacaoResponseDTO buildAuthResponse(Usuario usuario) {
        return AutenticacaoResponseDTO.of(jwtService.generateToken(usuario), usuario);
    }

    private String resolveMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, MESSAGE_LOCALE);
    }
}
