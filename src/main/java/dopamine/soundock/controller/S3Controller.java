package dopamine.soundock.controller;


import dopamine.soundock.dto.request.MultipleFileRequest;
import dopamine.soundock.dto.response.PresignedUrlResponse;
import dopamine.soundock.enums.CategoryType;
import dopamine.soundock.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3Service s3Service;


//    업로드용 presigned URL 발급
    @GetMapping("/presigned-upload")
    public ResponseEntity<List<PresignedUrlResponse>> getUploadUrls(
            @RequestBody MultipleFileRequest request) {

        List<PresignedUrlResponse> responses = s3Service.generateUploadUrls(
                request.getFileNames(),
                request.getFileTypes()
        );
        return ResponseEntity.ok(responses);
    }


//    다운로드용 presigned URL 발급
    @GetMapping("/presigned-download")
    public ResponseEntity<PresignedUrlResponse> getDownloadUrl(@RequestParam String fileKey) {
    PresignedUrlResponse response = s3Service.generateDownloadUrl(fileKey);
    return ResponseEntity.ok(response);
    }


//    파일 삭제
    @DeleteMapping("/file")
    public ResponseEntity<Void> deleteFile(@RequestParam String fileKey) {
        s3Service.deleteFile(fileKey);
        return ResponseEntity.noContent().build();
    }

}
