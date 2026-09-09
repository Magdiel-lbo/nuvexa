package com.nuvexa.platform.auditoria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditoriaServiceTest {

    private EventoAuditoriaRepository eventoAuditoriaRepository;
    private AuditoriaService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventoAuditoriaRepository = mock(EventoAuditoriaRepository.class);
        service = new AuditoriaService(eventoAuditoriaRepository);
    }

    @Test
    void registrarDeveSalvarEventoComTodosOsCamposInformados() {
        service.registrar(EntidadeAuditavel.PRONTUARIO, 10L, TipoEventoAuditoria.CRIACAO,
                7L, 99L, "Usuário Logado", null, "secao=EVOLUCAO, status=RASCUNHO");

        ArgumentCaptor<EventoAuditoria> captor = ArgumentCaptor.forClass(EventoAuditoria.class);
        verify(eventoAuditoriaRepository).save(captor.capture());

        EventoAuditoria salvo = captor.getValue();
        assertThat(salvo.getEntidadeTipo()).isEqualTo(EntidadeAuditavel.PRONTUARIO);
        assertThat(salvo.getEntidadeId()).isEqualTo(10L);
        assertThat(salvo.getTipoEvento()).isEqualTo(TipoEventoAuditoria.CRIACAO);
        assertThat(salvo.getOrganizacaoId()).isEqualTo(7L);
        assertThat(salvo.getUsuarioId()).isEqualTo(99L);
        assertThat(salvo.getUsuarioNome()).isEqualTo("Usuário Logado");
        assertThat(salvo.getDadosAntes()).isNull();
        assertThat(salvo.getDadosDepois()).isEqualTo("secao=EVOLUCAO, status=RASCUNHO");
    }

    @Test
    void listarDeveDelegarParaRepositorioComEscopoDeOrganizacao() {
        EventoAuditoria evento = EventoAuditoria.builder()
                .organizacaoId(7L)
                .entidadeTipo(EntidadeAuditavel.PRONTUARIO)
                .entidadeId(10L)
                .tipoEvento(TipoEventoAuditoria.ASSINATURA)
                .usuarioId(99L)
                .usuarioNome("Usuário Logado")
                .build();
        when(eventoAuditoriaRepository.findByEntidadeTipoAndEntidadeIdAndOrganizacaoIdOrderByCriadoEmDesc(
                EntidadeAuditavel.PRONTUARIO, 10L, 7L)).thenReturn(List.of(evento));

        List<EventoAuditoria> resultado = service.listar(EntidadeAuditavel.PRONTUARIO, 10L, 7L);

        assertThat(resultado).containsExactly(evento);
    }

    @Test
    void auditoriaServiceNaoExpoeAtualizarOuExcluir() {
        Method[] metodos = AuditoriaService.class.getDeclaredMethods();
        boolean temOperacaoDeMutacao = Arrays.stream(metodos)
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .map(Method::getName)
                .map(String::toLowerCase)
                .anyMatch(nome -> nome.contains("atualiz") || nome.contains("exclu") || nome.contains("delet") || nome.contains("updat"));

        assertThat(temOperacaoDeMutacao).isFalse();
    }
}
