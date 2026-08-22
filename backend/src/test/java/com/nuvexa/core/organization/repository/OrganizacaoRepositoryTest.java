package com.nuvexa.core.organizacao.repository;

import com.nuvexa.core.organizacao.model.Organizacao;
import com.nuvexa.core.organizacao.model.StatusOrganizacao;
import com.nuvexa.core.organizacao.model.TipoOrganizacao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrganizacaoRepositoryTest {

    @Autowired
    private OrganizacaoRepository organizationRepository;

    private Organizacao novaOrganizacaoIndividual(String name) {
        return Organizacao.builder()
                .nome(name)
                .tipo(TipoOrganizacao.INDIVIDUAL)
                .status(StatusOrganizacao.ATIVA)
                .build();
    }

    @Test
    void shouldPersistAndReadBackAllFields() {
        Organizacao saved = organizationRepository.saveAndFlush(novaOrganizacaoIndividual("Dra. Ana Nutricionista"));

        Organizacao found = organizationRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getNome()).isEqualTo("Dra. Ana Nutricionista");
        assertThat(found.getRazaoSocial()).isNull();
        assertThat(found.getDocumento()).isNull();
        assertThat(found.getTipo()).isEqualTo(TipoOrganizacao.INDIVIDUAL);
        assertThat(found.getStatus()).isEqualTo(StatusOrganizacao.ATIVA);
        assertThat(found.getCriadoEm()).isNotNull();
        assertThat(found.getAtualizadoEm()).isNotNull();
    }

    @Test
    void shouldPersistClinicWithLegalNameAndDocument() {
        Organizacao clinic = Organizacao.builder()
                .nome("Clínica Bem Estar")
                .razaoSocial("Bem Estar Serviços de Saúde LTDA")
                .documento("12345678000199")
                .tipo(TipoOrganizacao.CLINICA)
                .status(StatusOrganizacao.ATIVA)
                .build();

        Organizacao saved = organizationRepository.saveAndFlush(clinic);
        Organizacao found = organizationRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getRazaoSocial()).isEqualTo("Bem Estar Serviços de Saúde LTDA");
        assertThat(found.getDocumento()).isEqualTo("12345678000199");
        assertThat(found.getTipo()).isEqualTo(TipoOrganizacao.CLINICA);
    }

    @Test
    void shouldUpdateUpdatedAtOnChange() {
        Organizacao saved = organizationRepository.saveAndFlush(novaOrganizacaoIndividual("Dr. João Fisioterapeuta"));
        var firstUpdatedAt = saved.getAtualizadoEm();

        saved.setStatus(StatusOrganizacao.SUSPENSA);
        Organizacao updated = organizationRepository.saveAndFlush(saved);

        assertThat(updated.getStatus()).isEqualTo(StatusOrganizacao.SUSPENSA);
        assertThat(updated.getAtualizadoEm()).isAfterOrEqualTo(firstUpdatedAt);
        assertThat(updated.getCriadoEm()).isEqualTo(saved.getCriadoEm());
    }
}
