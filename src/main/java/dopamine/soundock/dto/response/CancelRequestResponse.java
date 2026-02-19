package dopamine.soundock.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import dopamine.soundock.entity.PopHistory;
import dopamine.soundock.enums.PopStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CancelRequestResponse {

    private String transactionId;
    private Integer donatorId;
    private String donatorNickname;

    private Integer receiverId;
    private String receiverNickname;

    private Integer donationAmount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime donationDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime cancelRequestDate;

    private PopStatus popStatus;

    public static CancelRequestResponse from(PopHistory donatedHistory) {
        return CancelRequestResponse.builder()
                .transactionId(donatedHistory.getTransactionId())
                .donatorId(donatedHistory.getUser().getId())
                .donatorNickname(donatedHistory.getUser().getNickname())
                .receiverId(donatedHistory.getRelatedUser().getId())
                .receiverNickname(donatedHistory.getRelatedUser().getNickname())
                .donationAmount(Math.abs(donatedHistory.getChangeAmount()))
                .donationDate(donatedHistory.getCreatedDatetime())
                .cancelRequestDate(donatedHistory.getRequestedDatetime())
                .popStatus(donatedHistory.getPopStatus())
                .build();
    }
}