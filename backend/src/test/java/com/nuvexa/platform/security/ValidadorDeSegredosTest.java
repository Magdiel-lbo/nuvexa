package com.nuvexa.platform.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Os valores abaixo são fixtures de teste, não segredos reais. O "padrão público" é o mesmo
 * literal de {@code application.yml}, de propósito: é o que o validador precisa reconhecer.
 */
class ValidadorDeSegredosTest {

    private static final String JWT_FORTE = "x".repeat(32);
    private static final String ACCESS_FORTE = "chave-de-acesso-de-teste";
    private static final String SECRET_FORTE = "secret-de-teste-forte";
    private static final String JWT_PADRAO = "change-this-secret-change-this-secret-change-this-secret";

    @Configuration
    @Import(ValidadorDeSegredos.class)
    static class Config {
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner().withUserConfiguration(Config.class);

    @Test
    void deveAceitarSegredosForesNoConstrutor() {
        assertThatCode(() -> new ValidadorDeSegredos(JWT_FORTE, ACCESS_FORTE, SECRET_FORTE)).doesNotThrowAnyException();
    }

    @Test
    void deveAceitarJwtComExatamente32Bytes() {
        assertThatCode(() -> new ValidadorDeSegredos("a".repeat(32), ACCESS_FORTE, SECRET_FORTE)).doesNotThrowAnyException();
    }

    @Test
    void deveRecusarJwtAusenteOuEmBranco() {
        assertThatThrownBy(() -> new ValidadorDeSegredos(null, ACCESS_FORTE, SECRET_FORTE)).hasMessageContaining("JWT_SECRET");
        assertThatThrownBy(() -> new ValidadorDeSegredos("   ", ACCESS_FORTE, SECRET_FORTE)).hasMessageContaining("JWT_SECRET");
    }

    @Test
    void deveRecusarJwtPadraoPublico() {
        assertThatThrownBy(() -> new ValidadorDeSegredos(JWT_PADRAO, ACCESS_FORTE, SECRET_FORTE)).hasMessageContaining("JWT_SECRET");
    }

    @Test
    void deveRecusarJwtMenorQue32Bytes() {
        assertThatThrownBy(() -> new ValidadorDeSegredos("a".repeat(31), ACCESS_FORTE, SECRET_FORTE)).hasMessageContaining("JWT_SECRET");
    }

    @Test
    void deveContarBytesEmUtf8NoTamanhoDoJwt() {
        // 16 caracteres de 2 bytes = 32 bytes; 15 = 30 bytes.
        assertThatCode(() -> new ValidadorDeSegredos("é".repeat(16), ACCESS_FORTE, SECRET_FORTE)).doesNotThrowAnyException();
        assertThatThrownBy(() -> new ValidadorDeSegredos("é".repeat(15), ACCESS_FORTE, SECRET_FORTE)).hasMessageContaining("JWT_SECRET");
    }

    @Test
    void deveRecusarStorageAusenteOuEmBranco() {
        assertThatThrownBy(() -> new ValidadorDeSegredos(JWT_FORTE, "", SECRET_FORTE)).hasMessageContaining("STORAGE_ACCESS_KEY");
        assertThatThrownBy(() -> new ValidadorDeSegredos(JWT_FORTE, ACCESS_FORTE, null)).hasMessageContaining("STORAGE_SECRET_KEY");
        assertThatThrownBy(() -> new ValidadorDeSegredos(JWT_FORTE, ACCESS_FORTE, " ")).hasMessageContaining("STORAGE_SECRET_KEY");
    }

    @Test
    void deveRecusarStoragePadraoPublico() {
        assertThatThrownBy(() -> new ValidadorDeSegredos(JWT_FORTE, "nuvexa", SECRET_FORTE)).hasMessageContaining("STORAGE_ACCESS_KEY");
        assertThatThrownBy(() -> new ValidadorDeSegredos(JWT_FORTE, ACCESS_FORTE, "nuvexa123")).hasMessageContaining("STORAGE_SECRET_KEY");
    }

    @Test
    void deveRecusarStorageSecretCurto() {
        assertThatThrownBy(() -> new ValidadorDeSegredos(JWT_FORTE, ACCESS_FORTE, "1234567")).hasMessageContaining("STORAGE_SECRET_KEY");
    }

    @Test
    void mensagemDeErroNaoDeveConterOSegredo() {
        String segredoCurto = "segredo-secreto-curto";
        assertThatThrownBy(() -> new ValidadorDeSegredos(segredoCurto, ACCESS_FORTE, SECRET_FORTE))
                .hasMessageNotContaining(segredoCurto);
        assertThatThrownBy(() -> new ValidadorDeSegredos(JWT_PADRAO, ACCESS_FORTE, SECRET_FORTE))
                .hasMessageNotContaining(JWT_PADRAO);
        assertThatThrownBy(() -> new ValidadorDeSegredos(JWT_FORTE, ACCESS_FORTE, "nuvexa123"))
                .hasMessageNotContaining("nuvexa123");
    }

    @Test
    void profileProdDeveFalharNaInicializacaoComOsDefaultsDoApplicationYml() {
        runner.withInitializer(context -> context.getEnvironment().addActiveProfile("prod"))
                .withPropertyValues("app.jwt.secret=" + JWT_PADRAO, "app.storage.access-key=nuvexa", "app.storage.secret-key=nuvexa123")
                .run(contexto -> assertThat(contexto).hasFailed());
    }

    @Test
    void profileProdDeveFalharQuandoOsSegredosNaoSaoInformados() {
        runner.withInitializer(context -> context.getEnvironment().addActiveProfile("prod"))
                .run(contexto -> assertThat(contexto).hasFailed());
    }

    @Test
    void profileProdDeveSubirComSegredosValidos() {
        runner.withInitializer(context -> context.getEnvironment().addActiveProfile("prod"))
                .withPropertyValues("app.jwt.secret=" + JWT_FORTE, "app.storage.access-key=" + ACCESS_FORTE, "app.storage.secret-key=" + SECRET_FORTE)
                .run(contexto -> assertThat(contexto).hasNotFailed().hasSingleBean(ValidadorDeSegredos.class));
    }

    @Test
    void foraDoProfileProdOValidadorNaoEstaAtivoEOsDefaultsLocaisContinuamValendo() {
        runner.withPropertyValues("app.jwt.secret=" + JWT_PADRAO, "app.storage.access-key=nuvexa", "app.storage.secret-key=nuvexa123")
                .run((AssertableApplicationContext contexto) -> assertThat(contexto).hasNotFailed().doesNotHaveBean(ValidadorDeSegredos.class));
    }
}
