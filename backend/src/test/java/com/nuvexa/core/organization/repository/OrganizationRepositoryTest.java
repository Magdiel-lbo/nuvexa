package com.nuvexa.core.organization.repository;

import com.nuvexa.core.organization.model.Organization;
import com.nuvexa.core.organization.model.OrganizationStatus;
import com.nuvexa.core.organization.model.OrganizationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrganizationRepositoryTest {

    @Autowired
    private OrganizationRepository organizationRepository;

    private Organization newIndividualOrganization(String name) {
        return Organization.builder()
                .name(name)
                .type(OrganizationType.INDIVIDUAL)
                .status(OrganizationStatus.ACTIVE)
                .build();
    }

    @Test
    void shouldPersistAndReadBackAllFields() {
        Organization saved = organizationRepository.saveAndFlush(newIndividualOrganization("Dra. Ana Nutricionista"));

        Organization found = organizationRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getName()).isEqualTo("Dra. Ana Nutricionista");
        assertThat(found.getLegalName()).isNull();
        assertThat(found.getDocument()).isNull();
        assertThat(found.getType()).isEqualTo(OrganizationType.INDIVIDUAL);
        assertThat(found.getStatus()).isEqualTo(OrganizationStatus.ACTIVE);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldPersistClinicWithLegalNameAndDocument() {
        Organization clinic = Organization.builder()
                .name("Clínica Bem Estar")
                .legalName("Bem Estar Serviços de Saúde LTDA")
                .document("12345678000199")
                .type(OrganizationType.CLINIC)
                .status(OrganizationStatus.ACTIVE)
                .build();

        Organization saved = organizationRepository.saveAndFlush(clinic);
        Organization found = organizationRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getLegalName()).isEqualTo("Bem Estar Serviços de Saúde LTDA");
        assertThat(found.getDocument()).isEqualTo("12345678000199");
        assertThat(found.getType()).isEqualTo(OrganizationType.CLINIC);
    }

    @Test
    void shouldUpdateUpdatedAtOnChange() {
        Organization saved = organizationRepository.saveAndFlush(newIndividualOrganization("Dr. João Fisioterapeuta"));
        var firstUpdatedAt = saved.getUpdatedAt();

        saved.setStatus(OrganizationStatus.SUSPENDED);
        Organization updated = organizationRepository.saveAndFlush(saved);

        assertThat(updated.getStatus()).isEqualTo(OrganizationStatus.SUSPENDED);
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(firstUpdatedAt);
        assertThat(updated.getCreatedAt()).isEqualTo(saved.getCreatedAt());
    }
}
