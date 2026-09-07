package com.nuvexa.platform.web;

import com.nuvexa.platform.exception.ApiErro;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
        content = @Content(schema = @Schema(implementation = ApiErro.class)))
public abstract class BaseController {
}
