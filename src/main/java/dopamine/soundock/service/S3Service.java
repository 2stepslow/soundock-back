package dopamine.soundock.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import dopamine.soundock.dto.response.PresignedUrlResponse;
import dopamine.soundock.enums.CategoryType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 s3Client;
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;


//    고유 파일 키 생성
    private String generateFileKey(String originalFileName) {
        String extension = "";
        int lastDot = originalFileName.lastIndexOf('.');    // 마지막 점 위치 찾아서
        if (lastDot > 0) {
            extension = originalFileName.substring(lastDot);
        }
        // 확장자만 잘라냄
        return UUID.randomUUID() + extension;
    }


//    공개 URL 생성(화면 이미지, DB저장용)
    private String getPublicUrl(String fileKey) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucket, fileKey);
    }


//    fileUrl에서 key 추출(다운로드 시)
    private String getFileKeyFromUrl(String fileUrl) {
        if (fileUrl.contains("s3.amazonaws.com/")) {
            return fileUrl.substring(fileUrl.indexOf("s3.amazonaws.com/") + 18);
        }
        return fileUrl;
    }


//    파일 삭제
    public void deleteFile(String fileKey) {
        if (s3Client.doesObjectExist(bucket, fileKey)) {
            s3Client.deleteObject(bucket, fileKey);
        }
    }




    //    업로드 url 생성
    public List<PresignedUrlResponse> generateUploadUrls(
            List<String> fileNames, List<String> fileTypes) {

    List<PresignedUrlResponse> responses = new ArrayList<>();

    for (int i = 0; i < fileNames.size(); i++) {
        String fileName = fileNames.get(i);
        String fileType = fileTypes.get(i);

//        파일 키 생성
        String fileKey = generateFileKey(fileName);

//        만료시간
        Date expiration = new Date();
        long expirationTime = expiration.getTime();
        expirationTime += 1000 * 60 * 5;
        expiration.setTime(expirationTime);

//        PresignedUrl 생성(PUT 업로드용)
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, fileKey)
                        .withMethod(HttpMethod.PUT)
                        .withContentType(fileType)
                        .withExpiration(expiration);

        URL uploadUrl = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

        PresignedUrlResponse response = PresignedUrlResponse.builder()
                .uploadUrl(uploadUrl.toString())
                .fileKey(fileKey)
                .fileUrl(getPublicUrl(fileKey))
                .build();

        responses.add(response);
    }

    return responses;

}


//    PresignedUrl 생성(다운로드용: community,reviews)
    public PresignedUrlResponse generateDownloadUrl(String fileKey) {

//        만료 시간 설정 (5분)
        Date expiration = new Date();
        long expirationTime = expiration.getTime();
        expirationTime += 1000 * 60 * 5;
        expiration.setTime(expirationTime);


//        Presigned URL 생성 (GET 다운로드용)
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, fileKey)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        URL downloadUrl = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

        return PresignedUrlResponse.builder()
                .downloadUrl(downloadUrl.toString())
                .fileKey(fileKey)
                .build();
    }

}
