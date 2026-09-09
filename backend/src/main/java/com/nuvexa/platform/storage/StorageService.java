package com.nuvexa.platform.storage;

/**
 * Abstração de armazenamento de arquivo — não conhece Prontuário, Anexo ou qualquer entidade de
 * negócio, só chave/conteúdo/tipo. Quem gera a chave e decide regra de negócio é o service que a
 * consome (ex: {@code ProntuarioAnexoService}).
 */
public interface StorageService {

    void upload(String chave, byte[] conteudo, String tipoMime);

    byte[] download(String chave);

    void delete(String chave);
}
