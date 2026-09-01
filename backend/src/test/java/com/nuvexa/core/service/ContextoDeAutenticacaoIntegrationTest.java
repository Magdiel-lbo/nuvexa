package com.nuvexa.core.service;

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
import com.nuvexa.platform.config.MessageConfig;
import com.nuvexa.platform.exception.NegocioException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({MessageConfig.class, ContextoDeAutenticacao.class})
class ContextoDeAutenticacaoIntegrationTest {

    @Autowired
    private ContextoDeAutenticacao contextoDeAutenticacao;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OrganizacaoRepository organizacaoRepository;

    @Autowired
    private VinculoRepository vinculoRepository;

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    private Usuario autenticar(String email) {
        Usuario usuario = usuarioRepository.saveAndFlush(Usuario.builder()
                .nome("Usuário de Teste")
                .email(email)
                .senha("hash-irrelevante")
                .perfil(Perfil.PROFISSIONAL)
                .ativo(true)
                .build());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));

        return usuario;
    }

    private Organizacao vincular(Usuario usuario, String nomeOrganizacao, PapelOrganizacional papel, boolean ativo) {
        Organizacao organizacao = organizacaoRepository.saveAndFlush(Organizacao.builder()
                .nome(nomeOrganizacao)
                .tipo(TipoOrganizacao.CLINICA)
                .status(StatusOrganizacao.ATIVA)
                .build());

        vinculoRepository.saveAndFlush(Vinculo.builder()
                .usuario(usuario)
                .organizacao(organizacao)
                .papel(papel)
                .ativo(ativo)
                .build());

        return organizacao;
    }

    // 1. Usuário autenticado alcança a própria organização, com o papel correto.
    @Test
    void deveResolverOrganizacaoEPapelDoUsuarioAutenticado() {
        Usuario usuario = autenticar("com.vinculo@nuvexa.test");
        Organizacao organizacao = vincular(usuario, "Clínica do Usuário", PapelOrganizacional.PROPRIETARIO, true);

        assertThat(contextoDeAutenticacao.usuarioAtual().getId()).isEqualTo(usuario.getId());
        assertThat(contextoDeAutenticacao.organizacaoAtualId()).isEqualTo(organizacao.getId());
        assertThat(contextoDeAutenticacao.vinculoAtual().getPapel()).isEqualTo(PapelOrganizacional.PROPRIETARIO);
    }

    // 9. Usuário sem vínculo é barrado com 403.
    @Test
    void deveRecusarUsuarioSemVinculo() {
        autenticar("sem.vinculo@nuvexa.test");

        assertThatThrownBy(() -> contextoDeAutenticacao.organizacaoAtual())
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void deveIgnorarVinculoInativo() {
        Usuario usuario = autenticar("vinculo.inativo@nuvexa.test");
        vincular(usuario, "Clínica Desativada", PapelOrganizacional.MEMBRO, false);

        assertThatThrownBy(() -> contextoDeAutenticacao.vinculoAtual())
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void deveRecusarRequisicaoSemAutenticacao() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> contextoDeAutenticacao.usuarioAtual())
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }
}
