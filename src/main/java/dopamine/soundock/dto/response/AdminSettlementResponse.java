package dopamine.soundock.dto.response;

import dopamine.soundock.enums.PopStatus;
import lombok.*;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSettlementResponse {
    private Integer popHistoryId;
    private Integer userId;
    private String nickName;
    private Integer changeAmount;
    private LocalDateTime requestedDatetime;
    private LocalDateTime approvedDatetime;
    private PopStatus popStatus;
}
