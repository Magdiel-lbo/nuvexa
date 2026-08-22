package com.nuvexa.core.organization.repository;

import com.nuvexa.core.organization.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}
