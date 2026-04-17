package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.domain.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/uploads")
public class UploadController {

    private final S3Service s3Service;

    /**
     * POST /api/uploads/comprovante?osg=OSG000001
     * Upload de comprovante de pagamento ao credenciado (Contas a Pagar).
     * Pasta: comprovantes/OSG000001.ext
     */
    @PostMapping("/comprovante")
    public ResponseEntity<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("osg") String osg) {

        String url = s3Service.upload(file, osg);
        return ResponseEntity.ok(url);
    }

    /**
     * POST /api/uploads/recebimento?osg=OSG000001
     * Upload de comprovante de recebimento do cliente (Contas a Receber).
     * Pasta: recebimentos/pagamento-cliente-OSG000001.ext
     */
    @PostMapping("/recebimento")
    public ResponseEntity<String> uploadRecebimento(
            @RequestParam("file") MultipartFile file,
            @RequestParam("osg") String osg) {

        String url = s3Service.uploadRecebimento(file, osg);
        return ResponseEntity.ok(url);
    }

    /**
     * DELETE /api/uploads/comprovante?url=https://...
     * Remove qualquer arquivo do bucket (comprovantes/ ou recebimentos/).
     */
    @DeleteMapping("/comprovante")
    public ResponseEntity<Void> deletar(@RequestParam("url") String url) {
        s3Service.deletar(url);
        return ResponseEntity.noContent().build();
    }
}