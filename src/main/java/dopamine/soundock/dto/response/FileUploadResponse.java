package dopamine.soundock.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private String fileKey;       // S3 파일 키 (삭제/다운 시 사용)
    private String fileUrl;       // S3 공개 URL (조회 시 사용)
    private String contentType;   // MIME 타입 (image/jpg 등)
    private Boolean isImage;      // 이미지 파일 확인
    private String originalFilename;
}