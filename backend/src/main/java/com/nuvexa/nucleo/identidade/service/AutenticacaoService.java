package com.nuvexa.nucleo.identidade.service;

import com.nuvexa.nucleo.identidade.dto.request.EsqueciSenhaRequestDTO;
import com.nuvexa.nucleo.identidade.dto.request.LoginRequestDTO;
import com.nuvexa.nucleo.identidade.dto.request.CadastroRequestDTO;
import com.nuvexa.nucleo.identidade.dto.request.RedefinirSenhaRequestDTO;
import com.nuvexa.nucleo.identidade.dto.response.AutenticacaoResponseDTO;
import com.nuvexa.nucleo.identidade.dto.response.MensagemResponseDTO;
import com.nuvexa.plataforma.excecao.NegocioException;
import com.nuvexa.nucleo.identidade.model.Perfil;
import com.nuvexa.nucleo.identidade.model.Usuario;
import com.nuvexa.nucleo.identidade.repository.UsuarioRepository;
import com.nuvexa.plataforma.seguranca.JwtService;
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
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MessageSource messageSource;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.cors.allowed-origin}")
    private String frontendUrl;

    public AutenticacaoResponseDTO register(CadastroRequestDTO request) {
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new NegocioException(HttpStatus.CONFLICT, resolveMessage("auth.emailInUse", request.getEmail()));
        }

        Usuario user = Usuario.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Perfil.PROFESSIONAL)
                .enabled(true)
                .build();

        Usuario saved;
        try {
            saved = usuarioRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            // Duas requisições de registro concorrentes para o mesmo e-mail podem passar
            // ambas pela checagem acima antes de qualquer uma commitar; quem chegar
            // depois esbarra na constraint UNIQUE do banco. Trata como o mesmo conflito
            // de negócio, em vez de vazar um erro 500 genérico.
            throw new NegocioException(HttpStatus.CONFLICT, resolveMessage("auth.emailInUse", request.getEmail()));
        }
        log.info("Usuário registrado com id={}", saved.getId());

        return buildAuthResponse(saved);
    }

    public AutenticacaoResponseDTO login(LoginRequestDTO request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (AuthenticationException ex) {
            throw new NegocioException(HttpStatus.UNAUTHORIZED, resolveMessage("auth.invalidCredentials"));
        }

        Usuario user = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NegocioException(HttpStatus.UNAUTHORIZED, resolveMessage("auth.invalidCredentials")));

        log.info("Usuário autenticado com id={}", user.getId());
        return buildAuthResponse(user);
    }

    public MensagemResponseDTO forgotPassword(EsqueciSenhaRequestDTO request) {
        Optional<Usuario> userOptional = usuarioRepository.findByEmail(request.getEmail());

        if (userOptional.isPresent()) {
            Usuario user = userOptional.get();
            String rawToken = generateRawToken();

            user.setResetTokenHash(hashToken(rawToken));
            user.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_VALIDITY_MINUTES));
            usuarioRepository.save(user);

            String resetLink = frontendUrl + "/reset-password?token=" + rawToken;
            log.info("Link de recuperação de senha para {} (válido por {} min): {}",
                    user.getEmail(), RESET_TOKEN_VALIDITY_MINUTES, resetLink);
        } else {
            log.info("Recuperação de senha solicitada para e-mail não cadastrado: {}", request.getEmail());
        }

        return new MensagemResponseDTO(resolveMessage("auth.forgotPassword.sent"));
    }

    public MensagemResponseDTO resetPassword(RedefinirSenhaRequestDTO request) {
        Usuario user = usuarioRepository.findByResetTokenHash(hashToken(request.getToken()))
                .filter(u -> u.getResetTokenExpiresAt() != null && u.getResetTokenExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new NegocioException(HttpStatus.BAD_REQUEST, resolveMessage("auth.token.invalid")));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetTokenHash(null);
        user.setResetTokenExpiresAt(null);
        usuarioRepository.save(user);

        log.info("Senha redefinida para usuário com id={}", user.getId());
        return new MensagemResponseDTO(resolveMessage("auth.resetPassword.success"));
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

    private AutenticacaoResponseDTO buildAuthResponse(Usuario user) {
        return AutenticacaoResponseDTO.builder()
                .token(jwtService.generateToken(user))
                .tokenType("Bearer")
                .role(user.getRole().name())
                .build();
    }

    private String resolveMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, MESSAGE_LOCALE);
    }
}
