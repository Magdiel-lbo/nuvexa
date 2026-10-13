package com.nuvexa.core.service;

import com.nuvexa.core.model.Especialidade;
import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.PapelOrganizacional;
import com.nuvexa.core.model.Perfil;
import com.nuvexa.core.model.StatusOrganizacao;
import com.nuvexa.core.model.TipoOrganizacao;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.core.repository.OrganizacaoRepository;
import com.nuvexa.core.repository.UsuarioRepository;
import com.nuvexa.core.repository.VinculoRepository;
import com.nuvexa.platform.config.MessageConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({MessageConfig.class, ContextoDeAutenticacao.class, VerificadorDeEspecialidade.class})
class VerificadorDeEspecialidadeIntegrationTest {

    @Autowired
    private VerificadorDeEspecialidade verificadorDeEspecialidade;

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

    private void vincular(Usuario usuario, boolean ativo) {
        Organizacao organizacao = organizacaoRepository.saveAndFlush(Organizacao.builder()
                .nome("Clínica de Teste")
                .tipo(TipoOrganizacao.CLINICA)
                .status(StatusOrganizacao.ATIVA)
                .build());

        vinculoRepository.saveAndFlush(Vinculo.builder()
                .usuario(usuario)
                .organizacao(organizacao)
                .papel(PapelOrganizacional.MEMBRO)
                .ativo(ativo)
                .build());
    }

    @Test
    void devePermitirQuandoAOrganizacaoAtualTemAEspecialidade() {
        vincular(autenticar("com.especialidade@nuvexa.test"), true);

        assertThat(verificadorDeEspecialidade.permite(Especialidade.NUTRICAO.name())).isTrue();
    }

    @Test
    void deveNegarQuandoUsuarioNaoTemVinculoAtivo() {
        vincular(autenticar("vinculo.inativo.especialidade@nuvexa.test"), false);

        assertThat(verificadorDeEspecialidade.permite(Especialidade.NUTRICAO.name())).isFalse();
    }

    @Test
    void deveNegarQuandoNaoHaUsuarioAutenticado() {
        SecurityContextHolder.clearContext();

        assertThat(verificadorDeEspecialidade.permite(Especialidade.NUTRICAO.name())).isFalse();
    }

    @Test
    void deveFalharAltoQuandoOValorNaoEUmaEspecialidade() {
        assertThatThrownBy(() -> verificadorDeEspecialidade.permite("ESPECIALIDADE_INEXISTENTE"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
