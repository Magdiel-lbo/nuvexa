package com.nuvexa.platform.security;

import tools.jackson.databind.ObjectMapper;
import com.nuvexa.platform.exception.ApiErro;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final MessageSourceAccessor mensagens;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ApiErro error = new ApiErro(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                mensagens.getMessage("erro.naoAutenticado"),
                request.getRequestURI());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
