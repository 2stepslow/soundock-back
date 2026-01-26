package dopamine.soundock.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dopamine.soundock.entity.PopHistory;
import dopamine.soundock.entity.TossPayment;
import lombok.*;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ConfirmPaymentResponse {
    private String paymentKey;
    private String type;
    private String orderId;
    private String orderName;
    @JsonProperty("mId")
    private String mid;
    private String currency;
    private String method;
    private int totalAmount;
    private int balanceAmount;
    private String status;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime requestedAt;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime approvedAt;

    private Integer taxExemptionAmount;
    @JsonProperty("requestedAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ssXXX", timezone = "Asia/Seoul")
    public OffsetDateTime getRequestAtDisplay(){
        return this.requestedAt;
    }

    @JsonProperty("approvedAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ssXXX", timezone = "Asia/Seoul")
    public OffsetDateTime getApprovedAtDisplay(){
        return this.approvedAt;
    }

    // TossPayment Entity로 전환
    public TossPayment toEntity(PopHistory popHistory){
        return TossPayment.builder()
                .orderId(this.orderId)
                .popHistory(popHistory)
                .paymentKey(this.paymentKey)
                .tossPaymentMethod(this.method)
                .amount(this.totalAmount)
                .tossPaymentStatus(this.status)
                .requestedDatetime(this.requestedAt != null ? this.requestedAt.toLocalDateTime() : null)
                .approvedDatetime(this.approvedAt != null ? this.approvedAt.toLocalDateTime() : null)
                .build();
    }
}
