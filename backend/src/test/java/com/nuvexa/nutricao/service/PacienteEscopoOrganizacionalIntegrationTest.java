package com.nuvexa.nutricao.service;

import com.nuvexa.core.contexto.ContextoDeAutenticacao;
import com.nuvexa.core.organizacao.model.Organizacao;
import com.nuvexa.core.organizacao.model.StatusOrganizacao;
import com.nuvexa.core.organizacao.model.TipoOrganizacao;
import com.nuvexa.core.organizacao.repository.OrganizacaoRepository;
import com.nuvexa.core.paciente.model.Paciente;
import com.nuvexa.core.paciente.model.Sexo;
import com.nuvexa.core.paciente.repository.PacienteRepository;
import com.nuvexa.platform.config.MessageConfig;
import com.nuvexa.platform.config.ModelMapperConfig;
import com.nuvexa.platform.config.querydsl.QuerydslConfig;
import com.nuvexa.platform.exception.NegocioException;
import com.nuvexa.nutricao.calculator.GastoCaloricoCalculator;
import com.nuvexa.nutricao.calculator.ImcCalculator;
import com.nuvexa.nutricao.calculator.TaxaMetabolicaCalculator;
import com.nuvexa.nutricao.dto.request.PacienteCreateRequestDTO;
import com.nuvexa.nutricao.dto.request.PacienteUpdateRequestDTO;
import com.nuvexa.nutricao.dto.response.PacienteResponseDTO;
import com.nuvexa.nutricao.model.NivelAtividade;
import com.nuvexa.nutricao.model.Objetivo;
import com.nuvexa.nutricao.model.PerfilNutricional;
import com.nuvexa.nutricao.report.dto.request.PacienteRelatorioFiltroDTO;
import com.nuvexa.nutricao.report.dto.response.PacienteRelatorioLinhaDTO;
import com.nuvexa.nutricao.report.service.PacienteRelatorioService;
import com.nuvexa.nutricao.repository.PerfilNutricionalRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Isolamento multi-organização: garante que o escopo aplicado na query realmente impede um
 * usuário de ler, alterar, excluir ou exportar paciente de outra organização.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({QuerydslConfig.class, MessageConfig.class, ModelMapperConfig.class,
        ImcCalculator.class, TaxaMetabolicaCalculator.class, GastoCaloricoCalculator.class,
        PacienteService.class, PacienteRelatorioService.class})
class PacienteEscopoOrganizacionalIntegrationTest {

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private PacienteRelatorioService pacienteRelatorioService;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PerfilNutricionalRepository perfilNutricionalRepository;

    @Autowired
    private OrganizacaoRepository organizacaoRepository;

    @MockitoBean
    private ContextoDeAutenticacao contextoDeAutenticacao;

    private Organizacao minhaOrganizacao;
    private Organizacao outraOrganizacao;

    @BeforeEach
    void setUp() {
        minhaOrganizacao = novaOrganizacao("Clínica A");
        outraOrganizacao = novaOrganizacao("Clínica B");
        estarLogadoEm(minhaOrganizacao);
    }

    private Organizacao novaOrganizacao(String nome) {
        return organizacaoRepository.saveAndFlush(Organizacao.builder()
                .nome(nome)
                .tipo(TipoOrganizacao.CLINICA)
                .status(StatusOrganizacao.ATIVA)
                .build());
    }

    private void estarLogadoEm(Organizacao organizacao) {
        when(contextoDeAutenticacao.organizacaoAtual()).thenReturn(organizacao);
        when(contextoDeAutenticacao.organizacaoAtualId()).thenReturn(organizacao.getId());
    }

    private Paciente novoPaciente(Organizacao organizacao, String nome) {
        Paciente paciente = pacienteRepository.saveAndFlush(Paciente.builder()
                .organizacao(organizacao)
                .nome(nome)
                .dataNascimento(LocalDate.of(1990, 5, 20))
                .sexo(Sexo.FEMININO)
                .build());

        perfilNutricionalRepository.saveAndFlush(PerfilNutricional.builder()
                .paciente(paciente)
                .altura(new BigDecimal("1.65"))
                .peso(new BigDecimal("62.50"))
                .objetivo(Objetivo.EMAGRECIMENTO)
                .nivelAtividade(NivelAtividade.MODERADAMENTE_ATIVO)
                .build());

        return paciente;
    }

    private PacienteCreateRequestDTO createRequest(String nome) {
        PacienteCreateRequestDTO request = new PacienteCreateRequestDTO();
        request.setNome(nome);
        request.setDataNascimento(LocalDate.of(1990, 5, 20));
        request.setSexo(Sexo.FEMININO);
        request.setAltura(new BigDecimal("1.65"));
        request.setPeso(new BigDecimal("62.50"));
        request.setObjetivo(Objetivo.EMAGRECIMENTO);
        request.setNivelAtividade(NivelAtividade.MODERADAMENTE_ATIVO);
        return request;
    }

    private PacienteUpdateRequestDTO updateRequest(String nome) {
        PacienteUpdateRequestDTO request = new PacienteUpdateRequestDTO();
        request.setNome(nome);
        request.setDataNascimento(LocalDate.of(1990, 5, 20));
        request.setSexo(Sexo.FEMININO);
        request.setAltura(new BigDecimal("1.70"));
        request.setPeso(new BigDecimal("65.00"));
        request.setObjetivo(Objetivo.MANUTENCAO_PESO);
        request.setNivelAtividade(NivelAtividade.MUITO_ATIVO);
        return request;
    }

    // 2. Usuário lista somente os pacientes da sua organização.
    @Test
    void deveListarApenasPacientesDaOrganizacaoAtual() {
        novoPaciente(minhaOrganizacao, "Ana da Minha Clinica");
        novoPaciente(outraOrganizacao, "Bruno da Outra Clinica");

        List<PacienteResponseDTO> resultado = pacienteService.findAll(null);

        assertThat(resultado)
                .extracting(PacienteResponseDTO::getNome)
                .containsExactly("Ana da Minha Clinica")
                .doesNotContain("Bruno da Outra Clinica");
    }

    // 3. Usuário cria paciente já dentro da sua organização.
    @Test
    void deveCriarPacienteNaOrganizacaoAtual() {
        PacienteResponseDTO criado = pacienteService.create(createRequest("Carla Nova"));

        Paciente persistido = pacienteRepository.findById(criado.getId()).orElseThrow();
        assertThat(persistido.getOrganizacao().getId()).isEqualTo(minhaOrganizacao.getId());
    }

    // 4. Usuário edita paciente da própria organização, e o escopo não é alterado no caminho.
    @Test
    void deveEditarPacienteDaPropriaOrganizacaoPreservandoEscopo() {
        Paciente paciente = novoPaciente(minhaOrganizacao, "Diana Original");

        PacienteResponseDTO atualizado = pacienteService.update(paciente.getId(), updateRequest("Diana Editada"));

        assertThat(atualizado.getNome()).isEqualTo("Diana Editada");
        Paciente persistido = pacienteRepository.findById(paciente.getId()).orElseThrow();
        assertThat(persistido.getOrganizacao().getId()).isEqualTo(minhaOrganizacao.getId());
    }

    // 5. Usuário não acessa paciente de outra organização.
    @Test
    void naoDeveAcessarPacienteDeOutraOrganizacao() {
        Paciente alheio = novoPaciente(outraOrganizacao, "Eduardo Alheio");

        assertThatThrownBy(() -> pacienteService.findById(alheio.getId()))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // 6. Usuário não altera paciente de outra organização.
    @Test
    void naoDeveAlterarPacienteDeOutraOrganizacao() {
        Paciente alheio = novoPaciente(outraOrganizacao, "Fabio Alheio");

        assertThatThrownBy(() -> pacienteService.update(alheio.getId(), updateRequest("Invadido")))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        assertThat(pacienteRepository.findById(alheio.getId()).orElseThrow().getNome())
                .isEqualTo("Fabio Alheio");
    }

    // 7. Usuário não exclui paciente de outra organização.
    @Test
    void naoDeveExcluirPacienteDeOutraOrganizacao() {
        Paciente alheio = novoPaciente(outraOrganizacao, "Gisele Alheia");

        assertThatThrownBy(() -> pacienteService.delete(alheio.getId()))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        assertThat(perfilNutricionalRepository.findByPacienteId(alheio.getId())).isPresent();
    }

    // 8. Relatório não retorna pacientes de outra organização.
    @Test
    void relatorioNaoDeveRetornarPacientesDeOutraOrganizacao() {
        novoPaciente(minhaOrganizacao, "Helena Minha");
        novoPaciente(outraOrganizacao, "Igor Alheio");

        RelatorioResponseDTO<PacienteRelatorioLinhaDTO> relatorio =
                pacienteRelatorioService.generate(PacienteRelatorioFiltroDTO.of(null, null, null, null));

        assertThat(relatorio.getLinhas())
                .extracting(PacienteRelatorioLinhaDTO::getNome)
                .containsExactly("Helena Minha")
                .doesNotContain("Igor Alheio");
    }

    // 8b. A exportação em Excel usa a mesma query escopada de generate() — sem cobertura
    // nenhuma antes disso, nem de caminho feliz nem de isolamento. Lê o .xlsx de volta com
    // Apache POI (já é dependência do módulo de relatórios) para confirmar em cima do dado
    // real, não só que o método não lança exceção.
    @Test
    void relatorioExcelNaoDeveRetornarPacientesDeOutraOrganizacao() {
        novoPaciente(minhaOrganizacao, "Helena Minha");
        novoPaciente(outraOrganizacao, "Igor Alheio");

        byte[] excel = pacienteRelatorioService.generateExcel(PacienteRelatorioFiltroDTO.of(null, null, null, null));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
            Sheet sheet = workbook.getSheet("Pacientes");
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(2); // 1 cabeçalho + 1 linha de dado

            Row linhaDeDado = sheet.getRow(1);
            assertThat(linhaDeDado.getCell(0).getStringCellValue()).isEqualTo("Helena Minha");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // 9. Usuário sem vínculo não acessa dado nenhum: o contexto barra antes da query.
    @Test
    void usuarioSemVinculoNaoDeveAcessarDados() {
        novoPaciente(minhaOrganizacao, "Joana Bloqueada");
        when(contextoDeAutenticacao.organizacaoAtualId())
                .thenThrow(new NegocioException(HttpStatus.FORBIDDEN, "contexto.semVinculo"));

        assertThatThrownBy(() -> pacienteService.findAll(null))
                .isInstanceOf(NegocioException.class)
                .satisfies(ex -> assertThat(((NegocioException) ex).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    // Troca de organização não vaza dados entre uma chamada e outra.
    @Test
    void mesmaBaseDeveResponderDiferentePorOrganizacao() {
        novoPaciente(minhaOrganizacao, "Katia A");
        novoPaciente(outraOrganizacao, "Lucas B");

        assertThat(pacienteService.findAll(null)).extracting(PacienteResponseDTO::getNome).containsExactly("Katia A");

        estarLogadoEm(outraOrganizacao);
        assertThat(pacienteService.findAll(null)).extracting(PacienteResponseDTO::getNome).containsExactly("Lucas B");
    }
}
