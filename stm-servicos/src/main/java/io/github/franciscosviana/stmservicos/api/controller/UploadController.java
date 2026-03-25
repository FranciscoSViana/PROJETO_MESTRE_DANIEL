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
     */
    @PostMapping("/comprovante")
    public ResponseEntity<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("osg") String osg) {

        String url = s3Service.upload(file, osg);
        return ResponseEntity.ok(url);
    }

    /**
     * DELETE /api/uploads/comprovante?url=https://...
     */
    @DeleteMapping("/comprovante")
    public ResponseEntity<Void> deletar(@RequestParam("url") String url) {
        s3Service.deletar(url);
        return ResponseEntity.noContent().build();
    }
}
