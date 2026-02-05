package dopamine.soundock.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardCreateRequest {
    @NotEmpty(message = "최소 1글자 이상 입력해주세요.")
    private String title;

    @NotEmpty(message = "최소 1글자 이상 입력해주세요.")
    private String content;

    private String fileUrl;
    // S3 공개 URL 또는 유튜브 URL

    private String fileKey;
    // S3 파일 키

    private String fileName;
    // 원본 파일명

    private Long fileSize;
    // 파일 크기


    private List<String> imageUrls;
    // 이미지 URL 리스트

    private List<String> imageKeys;
    // 이미지 키 리스트(삭제 사용)

}
