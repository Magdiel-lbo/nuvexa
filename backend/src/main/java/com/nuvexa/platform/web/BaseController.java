package com.nuvexa.platform.web;

import com.nuvexa.platform.exception.ApiErro;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * Base para controllers autenticados; só centraliza documentação OpenAPI comum, sem endpoints
 * ou lógica própria.
 *
 * Springdoc resolve @ApiResponse de classe via Spring AnnotatedElementUtils, que percorre a
 * hierarquia de superclasses (confirmado no bytecode de GenericResponseService), então o
 * @ApiResponse abaixo aparece em todo endpoint de quem estende esta classe. @Tag não pode vir
 * pra cá: a mesma resolução faz UNION com a tag do controller concreto em vez de substituí-la,
 * então uma tag genérica aqui vazaria para as tags de todos os recursos. @Operation/@Parameter
 * também ficam de fora: são lidos do método concreto, e como os controllers não sobrescrevem
 * métodos abstratos desta classe, nada aqui seria encontrado.
 */
@ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido",
        content = @Content(schema = @Schema(implementation = ApiErro.class)))
public abstract class BaseController {
}
