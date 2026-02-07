package dopamine.soundock.controller;

import dopamine.soundock.dto.response.FileUploadResponse;
import dopamine.soundock.dto.response.PresignedUrlResponse;
import dopamine.soundock.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    // 파일 업로드(적용x)
    @PostMapping("/upload")
    public ResponseEntity<List<FileUploadResponse>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files) {
        try {
            List<FileUploadResponse> responses = s3Service.uploadFiles(files);
            return ResponseEntity.ok(responses);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Presigned URL 생성 (다운로드용)
    @GetMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
            @RequestParam String fileKey) {
        try {
            String presignedUrl = s3Service.generatePresignedUrl(fileKey);
            return ResponseEntity.ok(new PresignedUrlResponse(presignedUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}