package com.nuvexa.nutricao.relatorio.service;

import com.nuvexa.core.model.Organizacao;
import com.nuvexa.core.model.PapelOrganizacional;
import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Perfil;
import com.nuvexa.core.model.Sexo;
import com.nuvexa.core.model.StatusOrganizacao;
import com.nuvexa.core.model.TipoOrganizacao;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.core.repository.OrganizacaoRepository;
import com.nuvexa.core.repository.PacienteRepository;
import com.nuvexa.core.repository.UsuarioRepository;
import com.nuvexa.core.repository.VinculoRepository;
import com.nuvexa.core.service.ContextoDeAutenticacao;
import com.nuvexa.core.service.OrganizacaoScopedContext;
import com.nuvexa.nutricao.calculator.GastoCaloricoCalculator;
import com.nuvexa.nutricao.calculator.ImcCalculator;
import com.nuvexa.nutricao.calculator.TaxaMetabolicaCalculator;
import com.nuvexa.nutricao.model.Avaliacao;
import com.nuvexa.nutricao.model.NivelAtividade;
import com.nuvexa.nutricao.model.Objetivo;
import com.nuvexa.nutricao.model.PerfilNutricional;
import com.nuvexa.nutricao.model.StatusAvaliacao;
import com.nuvexa.nutricao.model.TipoAvaliacao;
import com.nuvexa.nutricao.relatorio.dto.filter.PacienteFiltro;
import com.nuvexa.nutricao.relatorio.dto.response.PacienteRelatorioLinhaDTO;
import com.nuvexa.nutricao.repository.AvaliacaoRepository;
import com.nuvexa.nutricao.repository.PerfilNutricionalRepository;
import com.nuvexa.nutricao.service.AvaliacaoService;
import com.nuvexa.platform.config.MessageConfig;
import com.nuvexa.platform.config.ModelMapperConfig;
import com.nuvexa.platform.config.QuerydslConfig;
import com.nuvexa.relatorios.RelatorioResponseDTO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Isolamento multi-organização do relatório de Pacientes — separado de
 * PacienteEscopoOrganizacionalIntegrationTest (core) porque depende de PerfilNutricional
 * (nutricao) e, desde o Passo 1 do plano de prontuário, de Avaliacao (peso não é mais estado do
 * perfil — vem da avaliação mais recente do paciente).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({QuerydslConfig.class, MessageConfig.class, ModelMapperConfig.class,
        ImcCalculator.class, TaxaMetabolicaCalculator.class, GastoCaloricoCalculator.class,
        OrganizacaoScopedContext.class, AvaliacaoService.class, PacienteRelatorioService.class})
class PacienteRelatorioServiceEscopoOrganizacionalIntegrationTest {

    @Autowired
    private PacienteRelatorioService pacienteRelatorioService;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PerfilNutricionalRepository perfilNutricionalRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private OrganizacaoRepository organizacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VinculoRepository vinculoRepository;

    @MockitoBean
    private ContextoDeAutenticacao contextoDeAutenticacao;

    private Organizacao minhaOrganizacao;
    private Organizacao outraOrganizacao;
    private Usuario profissional;

    @BeforeEach
    void setUp() {
        minhaOrganizacao = novaOrganizacao("Clínica A");
        outraOrganizacao = novaOrganizacao("Clínica B");
        profissional = novoProfissionalVinculado(minhaOrganizacao, "Joana Nutri");
        when(contextoDeAutenticacao.organizacaoAtual()).thenReturn(minhaOrganizacao);
        when(contextoDeAutenticacao.organizacaoAtualId()).thenReturn(minhaOrganizacao.getId());
    }

    private Organizacao novaOrganizacao(String nome) {
        return organizacaoRepository.saveAndFlush(Organizacao.builder()
                .nome(nome)
                .tipo(TipoOrganizacao.CLINICA)
                .status(StatusOrganizacao.ATIVA)
                .build());
    }

    private Usuario novoProfissionalVinculado(Organizacao organizacao, String nome) {
        Usuario usuario = usuarioRepository.saveAndFlush(Usuario.builder()
                .nome(nome)
                .email(nome.toLowerCase().replace(" ", ".") + "@nuvexa.com")
                .senha("hash")
                .perfil(Perfil.PROFISSIONAL)
                .ativo(true)
                .build());
        vinculoRepository.saveAndFlush(Vinculo.builder()
                .usuario(usuario)
                .organizacao(organizacao)
                .papel(PapelOrganizacional.MEMBRO)
                .ativo(true)
                .build());
        return usuario;
    }

    private void novoPacienteComAvaliacao(Organizacao organizacao, Usuario avaliador, String nome) {
        Paciente paciente = pacienteRepository.saveAndFlush(Paciente.builder()
                .organizacao(organizacao)
                .nome(nome)
                .dataNascimento(LocalDate.of(1990, 5, 20))
                .sexo(Sexo.FEMININO)
                .build());

        perfilNutricionalRepository.saveAndFlush(PerfilNutricional.builder()
                .paciente(paciente)
                .altura(new BigDecimal("1.65"))
                .objetivo(Objetivo.EMAGRECIMENTO)
                .nivelAtividade(NivelAtividade.MODERADAMENTE_ATIVO)
                .build());

        avaliacaoRepository.saveAndFlush(Avaliacao.builder()
                .organizacao(organizacao)
                .paciente(paciente)
                .avaliador(avaliador)
                .data(LocalDate.now())
                .tipo(TipoAvaliacao.ANTROPOMETRIA)
                .status(StatusAvaliacao.CONCLUIDA)
                .peso(new BigDecimal("62.50"))
                .build());
    }

    @Test
    void relatorioNaoDeveRetornarPacientesDeOutraOrganizacao() {
        novoPacienteComAvaliacao(minhaOrganizacao, profissional, "Helena Minha");
        Usuario profissionalAlheio = novoProfissionalVinculado(outraOrganizacao, "Carlos Nutri");
        novoPacienteComAvaliacao(outraOrganizacao, profissionalAlheio, "Igor Alheio");

        RelatorioResponseDTO<PacienteRelatorioLinhaDTO> relatorio =
                pacienteRelatorioService.generate(PacienteFiltro.of(null, null, null, null));

        assertThat(relatorio.getLinhas())
                .extracting(PacienteRelatorioLinhaDTO::getNome)
                .containsExactly("Helena Minha")
                .doesNotContain("Igor Alheio");
    }

    @Test
    void relatorioExcelNaoDeveRetornarPacientesDeOutraOrganizacao() {
        novoPacienteComAvaliacao(minhaOrganizacao, profissional, "Helena Minha");
        Usuario profissionalAlheio = novoProfissionalVinculado(outraOrganizacao, "Carlos Nutri");
        novoPacienteComAvaliacao(outraOrganizacao, profissionalAlheio, "Igor Alheio");

        byte[] excel = pacienteRelatorioService.generateExcel(PacienteFiltro.of(null, null, null, null));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
            Sheet sheet = workbook.getSheet("Pacientes");
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(2);

            Row linhaDeDado = sheet.getRow(1);
            assertThat(linhaDeDado.getCell(0).getStringCellValue()).isEqualTo("Helena Minha");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
