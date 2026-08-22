package com.nuvexa.nucleo.organizacao.repository;

import com.nuvexa.nucleo.organizacao.model.Organizacao;
import com.nuvexa.nucleo.organizacao.model.StatusOrganizacao;
import com.nuvexa.nucleo.organizacao.model.TipoOrganizacao;
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
                .name(name)
                .type(TipoOrganizacao.INDIVIDUAL)
                .status(StatusOrganizacao.ACTIVE)
                .build();
    }

    @Test
    void shouldPersistAndReadBackAllFields() {
        Organizacao saved = organizationRepository.saveAndFlush(novaOrganizacaoIndividual("Dra. Ana Nutricionista"));

        Organizacao found = organizationRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getName()).isEqualTo("Dra. Ana Nutricionista");
        assertThat(found.getLegalName()).isNull();
        assertThat(found.getDocument()).isNull();
        assertThat(found.getType()).isEqualTo(TipoOrganizacao.INDIVIDUAL);
        assertThat(found.getStatus()).isEqualTo(StatusOrganizacao.ACTIVE);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldPersistClinicWithLegalNameAndDocument() {
        Organizacao clinic = Organizacao.builder()
                .name("Clínica Bem Estar")
                .legalName("Bem Estar Serviços de Saúde LTDA")
                .document("12345678000199")
                .type(TipoOrganizacao.CLINIC)
                .status(StatusOrganizacao.ACTIVE)
                .build();

        Organizacao saved = organizationRepository.saveAndFlush(clinic);
        Organizacao found = organizationRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getLegalName()).isEqualTo("Bem Estar Serviços de Saúde LTDA");
        assertThat(found.getDocument()).isEqualTo("12345678000199");
        assertThat(found.getType()).isEqualTo(TipoOrganizacao.CLINIC);
    }

    @Test
    void shouldUpdateUpdatedAtOnChange() {
        Organizacao saved = organizationRepository.saveAndFlush(novaOrganizacaoIndividual("Dr. João Fisioterapeuta"));
        var firstUpdatedAt = saved.getUpdatedAt();

        saved.setStatus(StatusOrganizacao.SUSPENDED);
        Organizacao updated = organizationRepository.saveAndFlush(saved);

        assertThat(updated.getStatus()).isEqualTo(StatusOrganizacao.SUSPENDED);
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(firstUpdatedAt);
        assertThat(updated.getCreatedAt()).isEqualTo(saved.getCreatedAt());
    }
}
