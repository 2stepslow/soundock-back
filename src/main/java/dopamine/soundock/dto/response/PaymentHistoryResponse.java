package dopamine.soundock.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import dopamine.soundock.enums.PopStatus;
import dopamine.soundock.enums.PopTarget;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "결제 내역 및 재화(POP) 변동 내역 응답")
public class PaymentHistoryResponse {
    private Integer popHistoryId;

    private String orderId;

    private String paymentKey;
    @Schema(
            description = "결제(충전) 승인 일시",
            example = "2026-02-10 15:30:00",
            type = "string" // LocalDateTime을 문자열로 인식하게 강제
    )
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdDatetime;

    @Schema(
            description = "변동된 재화(POP) 수량 (충전 시 양수)",
            example = "1000"
    )
    @NotNull
    private Integer changeAmount;


    @Schema(
            description = "변동 사유 (CHARGE: 충전, USE: 사용, REFUND: 환불)",
            implementation = PopTarget.class
    )
    private PopTarget target;

    @Schema(
            description = "실제 결제 금액 (KRW, 원화)",
            example = "11000"
    )
    @NotNull
    private Integer actualAmount;

    @Schema(
            description = "재화 유효기간 만료 일시 (충전일로부터 5년 등 정책에 따름)",
            example = "2031-02-10 15:30:00",
            type = "string"
    )
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime expiredDatetime;

    @Schema(
            description = "결제 취소 여부 (true: 취소된 건, false: 정상)",
            example = "false"
    )
    private boolean isCanceled;

    @Schema(description = "실제 결제 여부")
    private PopStatus popStatus;
}
