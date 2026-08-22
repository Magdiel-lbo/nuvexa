package com.nuvexa.verticals.nutrition.model;

import com.nuvexa.core.patient.model.PatientCore;
import com.nuvexa.platform.persistence.AbstractModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "nutrition_profiles")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class NutritionProfile extends AbstractModel {

    @OneToOne
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private PatientCore patientCore;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal height;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Goal goal;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", nullable = false, length = 30)
    private ActivityLevel activityLevel;

    @Column(name = "manual_daily_calories", precision = 6, scale = 2)
    private BigDecimal manualDailyCalories;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
