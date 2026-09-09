package com.nuvexa.platform.storage;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;

/**
 * Implementação compatível com S3 — fala com MinIO em desenvolvimento e com S3/compatível em
 * produção só trocando {@code app.storage.*} (nenhuma mudança de código). {@code forcePathStyle}
 * é necessário para MinIO (endpoint próprio, sem DNS por bucket).
 */
@Service
@Log4j2
public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    private final String bucket;

    public S3StorageService(
            @Value("${app.storage.endpoint}") String endpoint,
            @Value("${app.storage.access-key}") String accessKey,
            @Value("${app.storage.secret-key}") String secretKey,
            @Value("${app.storage.bucket}") String bucket,
            @Value("${app.storage.region}") String region) {
        this.bucket = bucket;
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .forcePathStyle(true)
                .build();
    }

    @Override
    public void upload(String chave, byte[] conteudo, String tipoMime) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(chave).contentType(tipoMime).build(),
                    RequestBody.fromBytes(conteudo));
        } catch (SdkException e) {
            log.error("Falha ao enviar arquivo para o storage, chave={}", chave, e);
            throw new StorageException("Erro ao enviar arquivo para o storage", e);
        }
    }

    @Override
    public byte[] download(String chave) {
        try {
            return s3Client.getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(chave).build()).asByteArray();
        } catch (SdkException e) {
            log.error("Falha ao baixar arquivo do storage, chave={}", chave, e);
            throw new StorageException("Erro ao baixar arquivo do storage", e);
        }
    }

    @Override
    public void delete(String chave) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(chave).build());
        } catch (SdkException e) {
            log.error("Falha ao excluir arquivo do storage, chave={}", chave, e);
            throw new StorageException("Erro ao excluir arquivo do storage", e);
        }
    }
}
