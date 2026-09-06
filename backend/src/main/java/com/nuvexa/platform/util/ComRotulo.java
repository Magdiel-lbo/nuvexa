package com.nuvexa.platform.util;

/**
 * Contrato para enums de domínio que carregam seu próprio rótulo de exibição — nenhum enum de
 * negócio deve depender de {@code messages.properties}/{@code MessageSource} só para dizer como
 * se chama. Ver {@link EnumOpcaoResolver#resolve} para o único ponto que lê este rótulo.
 */
public interface ComRotulo {

    String getRotulo();
}
