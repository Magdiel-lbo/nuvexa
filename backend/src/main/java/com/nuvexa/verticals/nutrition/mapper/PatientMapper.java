package com.nuvexa.verticals.nutrition.mapper;

import com.nuvexa.core.patient.model.PatientCore;
import com.nuvexa.verticals.nutrition.dto.request.PatientCreateRequestDTO;
import com.nuvexa.verticals.nutrition.dto.request.PatientUpdateRequestDTO;
import com.nuvexa.verticals.nutrition.dto.response.PatientResponseDTO;
import com.nuvexa.verticals.nutrition.model.NutritionProfile;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public PatientCore toPatientCore(PatientCreateRequestDTO request) {
        return PatientCore.builder()
                .name(request.getName())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .build();
    }

    public NutritionProfile toNutritionProfile(PatientCreateRequestDTO request, PatientCore patientCore) {
        return NutritionProfile.builder()
                .patientCore(patientCore)
                .height(request.getHeight())
                .weight(request.getWeight())
                .goal(request.getGoal())
                .activityLevel(request.getActivityLevel())
                .manualDailyCalories(request.getManualDailyCalories())
                .notes(request.getNotes())
                .build();
    }

    public void updatePatientCore(PatientUpdateRequestDTO request, PatientCore patientCore) {
        patientCore.setName(request.getName());
        patientCore.setBirthDate(request.getBirthDate());
        patientCore.setGender(request.getGender());
    }

    public void updateNutritionProfile(PatientUpdateRequestDTO request, NutritionProfile nutritionProfile) {
        nutritionProfile.setHeight(request.getHeight());
        nutritionProfile.setWeight(request.getWeight());
        nutritionProfile.setGoal(request.getGoal());
        nutritionProfile.setActivityLevel(request.getActivityLevel());
        nutritionProfile.setManualDailyCalories(request.getManualDailyCalories());
        nutritionProfile.setNotes(request.getNotes());
    }

    public PatientResponseDTO toResponse(NutritionProfile nutritionProfile) {
        PatientCore patientCore = nutritionProfile.getPatientCore();
        return PatientResponseDTO.builder()
                .id(patientCore.getId())
                .name(patientCore.getName())
                .birthDate(patientCore.getBirthDate())
                .gender(patientCore.getGender())
                .height(nutritionProfile.getHeight())
                .weight(nutritionProfile.getWeight())
                .goal(nutritionProfile.getGoal())
                .activityLevel(nutritionProfile.getActivityLevel())
                .manualDailyCalories(nutritionProfile.getManualDailyCalories())
                .notes(nutritionProfile.getNotes())
                .createdAt(nutritionProfile.getCreatedAt())
                .updatedAt(nutritionProfile.getUpdatedAt())
                .build();
    }
}
