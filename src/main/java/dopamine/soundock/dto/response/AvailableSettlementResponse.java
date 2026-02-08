package dopamine.soundock.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "정산 가능 내역 조회 응답 DTO")
public class AvailableSettlementResponse {
    @Schema(description = "총 정산 가능 금액", example = "50000")
    private Integer totalAmount;

    @Schema(description = "총 정산 가능 건수", example = "5")
    private Integer totalCount;

    @Schema(description = "정산 가능 내역 상세 리스트")
    private List<PopHistoryResponse> popHistoryResponses;
}
