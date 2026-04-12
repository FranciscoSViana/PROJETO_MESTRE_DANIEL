package io.github.franciscosviana.stmservicos.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.bucket}")
    private String bucket;

    /**
     * Faz upload do comprovante com nome baseado na OSG.
     * Formato: comprovantes/OSG-000001.pdf (ou .jpg, etc.)
     */
    public String upload(MultipartFile file, String osg) {
        try {
            String extensao = obterExtensao(file.getOriginalFilename());
            String fileName = "comprovantes/" + osg + extensao;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return "https://" + bucket + ".s3.amazonaws.com/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao fazer upload para S3", e);
        }
    }

    /**
     * Remove o arquivo do bucket a partir da URL pública.
     */
    public void deletar(String url) {
        try {
            String prefix = "https://" + bucket + ".s3.amazonaws.com/";
            if (!url.startsWith(prefix)) {
                throw new RuntimeException("URL não pertence ao bucket configurado: " + url);
            }
            String key = url.substring(prefix.length());

            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao excluir arquivo do S3" + e.getMessage(), e);
        }
    }

    private String obterExtensao(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains(".")) return "";
        return nomeArquivo.substring(nomeArquivo.lastIndexOf(".")).toLowerCase();
    }
}