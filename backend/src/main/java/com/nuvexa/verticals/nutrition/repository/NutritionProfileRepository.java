package com.nuvexa.verticals.nutrition.repository;

import com.nuvexa.verticals.nutrition.model.NutritionProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NutritionProfileRepository extends JpaRepository<NutritionProfile, Long> {

    Optional<NutritionProfile> findByPatientCoreId(Long patientId);
}
