package dopamine.soundock.dto.request;

import dopamine.soundock.enums.AnnounceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementCreateRequest {

    @NotNull
    private AnnounceType announceType;

    @NotBlank(message = "최소 1글자 이상 입력해주세요.")
    private String title;

    @NotBlank(message = "최소 1글자 이상 입력해주세요.")
    private String content;

    private String linkUrl;

    @NotNull
    private Integer priority = 0;

    @NotNull
    private Boolean isActive = true;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;
}