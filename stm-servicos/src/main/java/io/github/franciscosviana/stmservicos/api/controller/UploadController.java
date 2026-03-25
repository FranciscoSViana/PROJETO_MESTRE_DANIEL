package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.domain.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/uploads")
public class UploadController {

    private final S3Service s3Service;

    @PostMapping("/comprovante")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        String url = s3Service.upload(file);
        return ResponseEntity.ok(url);
    }
}
