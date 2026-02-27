package dopamine.soundock.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import dopamine.soundock.dto.response.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    // Tika: 실제 콘텐츠 기반 MIME 판별
    private static final Tika TIKA = new Tika();

    // 허용 MIME 타입
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            // 이미지
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",

            // 읽기용 문서
            "application/pdf",
            "text/plain",

            // 오디오
            "audio/mpeg",
            "audio/wav",
            "audio/ogg"
    );

    // 검증 메서드
    private void validateUploadFile(MultipartFile file) {
        if(file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드 파일이 비어있습니다.");
        }
        String originalFilename = file.getOriginalFilename();

        if(originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("파일명이 비어있습니다.");
        }

        long maxBytes = 10 * 1024 * 1024;
        if (file.getSize() > maxBytes) throw new IllegalArgumentException("파일 용량이 너무 큽니다.");
    }

    private String detectMimeType(MultipartFile file) throws IOException {

        // Tika는 스트림을 읽어서 판단하므로 try-with-resources로 처리
        try (InputStream is = file.getInputStream()) {

            // fileName을 같이 주면 판별 정확도가 올라가는 경우가 있음
            String name = file.getOriginalFilename();
            return TIKA.detect(is, name);
        }
    }

    private void validateMimeTypeAllowed(String detectedMimeType) {
        if (detectedMimeType == null || detectedMimeType.isBlank()) {
            throw new IllegalArgumentException("MIME 타입을 판별할 수 없습니다.");
        }
        if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
            throw new IllegalArgumentException("허용되지 않은 파일 타입입니다.");
        }
    }



    // 이미지 파일 확장자
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );


    // 고유 파일 키 생성
    private String generateFileKey(String originalFileName) {
        String extension = "";
        int lastDot = originalFileName.lastIndexOf('.');
        if (lastDot > 0) {
            extension = originalFileName.substring(lastDot).toLowerCase();
        }
        return UUID.randomUUID() + extension;
    }

    // 공개 URL 생성
    private String getPublicUrl(String fileKey) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucket, fileKey);
    }

    // 파일 확장자로 이미지 파일 여부 확인
    public boolean isImageFile(String fileName) {
        if (fileName == null) return false;
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0) return false;
        String extension = fileName.substring(lastDot).toLowerCase();
        return IMAGE_EXTENSIONS.contains(extension);
    }


    // 단일 파일 업로드
    public FileUploadResponse uploadFile(MultipartFile file) throws IOException {

        // MIME 판별
        String detectedMimeType = detectMimeType(file);

        // 허용 타입 체크
        validateMimeTypeAllowed(detectedMimeType);


        // 파일 키 생성
        String fileKey = generateFileKey(file.getOriginalFilename());

        // 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(detectedMimeType);
        metadata.setContentLength(file.getSize());

        // S3에 파일 업로드
        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(new PutObjectRequest(bucket, fileKey, inputStream, metadata));
        }

        // 응답 생성
        return FileUploadResponse.builder()
                .fileKey(fileKey)
                .fileUrl(getPublicUrl(fileKey))
                .contentType(file.getContentType())
                .isImage(isImageFile(file.getOriginalFilename()))
                .originalFilename(file.getOriginalFilename())
                .build();
    }

    // 여러 파일 업로드
    public List<FileUploadResponse> uploadFiles(List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }

        List<FileUploadResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                FileUploadResponse response = uploadFile(file);
                responses.add(response);
            }
        }
        return responses;
    }

    // 파일 삭제
    public void deleteFile(String fileKey) {
        if (fileKey != null && s3Client.doesObjectExist(bucket, fileKey)) {
            s3Client.deleteObject(bucket, fileKey);
        }
    }

    // 여러 파일 삭제
    public void deleteFiles(List<String> fileKeys) {
        if (fileKeys != null && !fileKeys.isEmpty()) {
            for (String fileKey : fileKeys) {
                deleteFile(fileKey);
            }
        }
    }

    // fileUrl에서 key 추출
    public String getFileKeyFromUrl(String fileUrl) {
        if (fileUrl != null && fileUrl.contains("s3.amazonaws.com/")) {
            return fileUrl.substring(fileUrl.indexOf("s3.amazonaws.com/") + 17);
        }
        return fileUrl;
    }

    // Presigned URL 생성 (다운로드용)
    public String generatePresignedUrl(String fileKey) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000L * 60 * 10; // 10분
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, fileKey)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }



}