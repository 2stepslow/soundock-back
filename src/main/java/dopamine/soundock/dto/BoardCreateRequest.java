package dopamine.soundock.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
public class BoardCreateRequest {
    @NotEmpty(message = "최소 1글자 이상 입력해주세요.")
    private String title;

    @NotEmpty(message = "최소 1글자 이상 입력해주세요.")
    private String content;

    private String fileUrl;
}
