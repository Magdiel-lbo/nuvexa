CREATE TABLE nutrition_profiles (
    id                     BIGSERIAL     PRIMARY KEY,
    patient_id             BIGINT        NOT NULL,
    goal                   VARCHAR(30)   NOT NULL,
    activity_level         VARCHAR(30)   NOT NULL,
    manual_daily_calories  NUMERIC(6,2),
    height                 NUMERIC(4,2)  NOT NULL,
    weight                 NUMERIC(5,2)  NOT NULL,
    notes                  TEXT,
    created_at             TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at             TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT uq_nutrition_profiles_patient_id UNIQUE (patient_id),
    CONSTRAINT fk_nutrition_profiles_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT chk_nutrition_profiles_height_positive CHECK (height > 0),
    CONSTRAINT chk_nutrition_profiles_weight_positive CHECK (weight > 0)
);
