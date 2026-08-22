package com.nuvexa.core.identity.service;

import com.nuvexa.core.identity.dto.request.ForgotPasswordRequestDTO;
import com.nuvexa.core.identity.dto.request.LoginRequestDTO;
import com.nuvexa.core.identity.dto.request.RegisterRequestDTO;
import com.nuvexa.core.identity.dto.request.ResetPasswordRequestDTO;
import com.nuvexa.core.identity.dto.response.AuthResponseDTO;
import com.nuvexa.core.identity.dto.response.MessageResponseDTO;
import com.nuvexa.platform.exception.CustomException;
import com.nuvexa.core.identity.model.Role;
import com.nuvexa.core.identity.model.User;
import com.nuvexa.core.identity.repository.UserRepository;
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
public class AuthService {

    private static final Locale MESSAGE_LOCALE = Locale.of("pt", "BR");
    private static final int RESET_TOKEN_BYTES = 32;
    private static final long RESET_TOKEN_VALIDITY_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MessageSource messageSource;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.cors.allowed-origin}")
    private String frontendUrl;

    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException(HttpStatus.CONFLICT, resolveMessage("auth.emailInUse", request.getEmail()));
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.PROFESSIONAL)
                .enabled(true)
                .build();

        User saved;
        try {
            saved = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            // Duas requisições de registro concorrentes para o mesmo e-mail podem passar
            // ambas pela checagem acima antes de qualquer uma commitar; quem chegar
            // depois esbarra na constraint UNIQUE do banco. Trata como o mesmo conflito
            // de negócio, em vez de vazar um erro 500 genérico.
            throw new CustomException(HttpStatus.CONFLICT, resolveMessage("auth.emailInUse", request.getEmail()));
        }
        log.info("Usuário registrado com id={}", saved.getId());

        return buildAuthResponse(saved);
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (AuthenticationException ex) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, resolveMessage("auth.invalidCredentials"));
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, resolveMessage("auth.invalidCredentials")));

        log.info("Usuário autenticado com id={}", user.getId());
        return buildAuthResponse(user);
    }

    public MessageResponseDTO forgotPassword(ForgotPasswordRequestDTO request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String rawToken = generateRawToken();

            user.setResetTokenHash(hashToken(rawToken));
            user.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_VALIDITY_MINUTES));
            userRepository.save(user);

            String resetLink = frontendUrl + "/reset-password?token=" + rawToken;
            log.info("Link de recuperação de senha para {} (válido por {} min): {}",
                    user.getEmail(), RESET_TOKEN_VALIDITY_MINUTES, resetLink);
        } else {
            log.info("Recuperação de senha solicitada para e-mail não cadastrado: {}", request.getEmail());
        }

        return new MessageResponseDTO(resolveMessage("auth.forgotPassword.sent"));
    }

    public MessageResponseDTO resetPassword(ResetPasswordRequestDTO request) {
        User user = userRepository.findByResetTokenHash(hashToken(request.getToken()))
                .filter(u -> u.getResetTokenExpiresAt() != null && u.getResetTokenExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, resolveMessage("auth.token.invalid")));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetTokenHash(null);
        user.setResetTokenExpiresAt(null);
        userRepository.save(user);

        log.info("Senha redefinida para usuário com id={}", user.getId());
        return new MessageResponseDTO(resolveMessage("auth.resetPassword.success"));
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

    private AuthResponseDTO buildAuthResponse(User user) {
        return AuthResponseDTO.builder()
                .token(jwtService.generateToken(user))
                .tokenType("Bearer")
                .role(user.getRole().name())
                .build();
    }

    private String resolveMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, MESSAGE_LOCALE);
    }
}
