package com.nuvexa.core.controller;

import com.nuvexa.core.dto.request.ConviteAceiteRequestDTO;
import com.nuvexa.core.dto.request.EsqueciSenhaRequestDTO;
import com.nuvexa.core.dto.request.RefreshRequestDTO;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.service.ContextoDeAutenticacao;
import com.nuvexa.platform.exception.NegocioException;
import com.nuvexa.platform.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.support.MessageSourceAccessor;
import com.nuvexa.core.dto.request.LoginRequestDTO;
import com.nuvexa.core.dto.request.CadastroRequestDTO;
import com.nuvexa.core.dto.request.RedefinirSenhaRequestDTO;
import com.nuvexa.core.dto.response.AutenticacaoResponseDTO;
import com.nuvexa.core.dto.response.MensagemResponseDTO;
import com.nuvexa.core.service.AutenticacaoService;
import com.nuvexa.core.service.ConviteService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Não estende BaseController: seus endpoints são públicos (permitAll em SecurityConfig), então
 * o 401 documentado ali não se aplica aqui. @SecurityRequirements vazio na classe sobrescreve o
 * "bearerAuth" padrão de OpenApiConfig, tirando o cadeado desses endpoints no Swagger UI — é uma
 * exceção deliberada à meta de "zero anotações Swagger": é uma marcação de segurança, não texto
 * de documentação, e faz mais sentido ficar auditável aqui do que escondida no descriptions.yml.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@SecurityRequirements
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;
    private final ConviteService conviteService;
    private final ContextoDeAutenticacao contextoDeAutenticacao;
    private final MessageSourceAccessor mensagens;

    @PostMapping("/cadastrar")
    @ResponseStatus(HttpStatus.CREATED)
    public AutenticacaoResponseDTO cadastrar(@Valid @RequestBody CadastroRequestDTO request) {
        return autenticacaoService.cadastrar(request);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AutenticacaoResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return autenticacaoService.login(request);
    }

    @PostMapping("/convites/aceitar")
    @ResponseStatus(HttpStatus.CREATED)
    public AutenticacaoResponseDTO aceitarConvite(@Valid @RequestBody ConviteAceiteRequestDTO request) {
        return conviteService.aceitar(request);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public AutenticacaoResponseDTO renovar(@Valid @RequestBody RefreshRequestDTO request) {
        return autenticacaoService.renovar(request);
    }

    /** Encerra o login do access token enviado. As rotas /auth/** são públicas, então a sessão é exigida aqui. */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sair(HttpServletRequest request) {
        autenticacaoService.sair(sessaoDaRequisicao(request), usuarioDaRequisicao());
    }

    @PostMapping("/logout-todos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sairDeTodos(HttpServletRequest request) {
        sessaoDaRequisicao(request);
        autenticacaoService.sairDeTodosOsDispositivos(usuarioDaRequisicao());
    }

    private Long sessaoDaRequisicao(HttpServletRequest request) {
        Object sessaoId = request.getAttribute(JwtAuthenticationFilter.ATRIBUTO_SESSAO_ID);
        if (sessaoId == null) {
            throw new NegocioException(HttpStatus.UNAUTHORIZED, mensagens.getMessage("contexto.naoAutenticado"));
        }
        return (Long) sessaoId;
    }

    private Usuario usuarioDaRequisicao() {
        return contextoDeAutenticacao.usuarioAtual();
    }

    @PostMapping("/esqueci-senha")
    @ResponseStatus(HttpStatus.OK)
    public MensagemResponseDTO esqueciSenha(@Valid @RequestBody EsqueciSenhaRequestDTO request) {
        return autenticacaoService.esqueciSenha(request);
    }

    @PostMapping("/redefinir-senha")
    @ResponseStatus(HttpStatus.OK)
    public MensagemResponseDTO redefinirSenha(@Valid @RequestBody RedefinirSenhaRequestDTO request) {
        return autenticacaoService.redefinirSenha(request);
    }
}
