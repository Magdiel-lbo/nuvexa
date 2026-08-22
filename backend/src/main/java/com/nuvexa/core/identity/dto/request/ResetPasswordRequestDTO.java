package com.nuvexa.core.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequestDTO {

    @NotBlank(message = "{auth.token.required}")
    private String token;

    @NotBlank(message = "{auth.password.required}")
    @Size(min = 8, message = "{auth.password.invalid}")
    private String newPassword;
}
