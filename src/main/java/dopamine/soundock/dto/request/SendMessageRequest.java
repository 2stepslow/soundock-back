package dopamine.soundock.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageRequest {

    @NotBlank(message = "메시지 내용은 필수입니다")
    @Size(max = 200)
    private String content;

}
