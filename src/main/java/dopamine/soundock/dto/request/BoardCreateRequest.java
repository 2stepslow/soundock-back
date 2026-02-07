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

    private String youtubeUrl;

    // 삭제할 기존 이미지 ID 리스트
    private List<Integer> deleteAttachmentIds;

    // 최종 이미지 순서 (기존 ID와 새 파일의 인덱스 구분)
    private List<String> imageOrder;


}
