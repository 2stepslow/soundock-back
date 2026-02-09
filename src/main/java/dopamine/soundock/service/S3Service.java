package dopamine.soundock.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import dopamine.soundock.dto.response.FileUploadResponse;
import dopamine.soundock.enums.CategoryType;
import dopamine.soundock.exceptions.InvalidFileException;
import lombok.RequiredArgsConstructor;
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

    // 이미지 파일 확장자
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
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
    private FileUploadResponse uploadFile(MultipartFile file) throws IOException {

        // 파일 키 생성
        String fileKey = generateFileKey(file.getOriginalFilename());

        // 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
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
    public String generatePresignedUrl(String fileUrl) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000L * 60 * 10; // 5분
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, fileUrl)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }



}