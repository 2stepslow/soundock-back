package dopamine.soundock.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import dopamine.soundock.enums.PopTarget;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaymentHistoryResponse {
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdDatetime;

    @NotNull
    private Integer changeAmount;

    private PopTarget target;

    @NotNull
    private Integer actualAmount;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime expiredDatetime;

    private boolean isCanceled;
}
