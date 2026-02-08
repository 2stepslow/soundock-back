package dopamine.soundock.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import dopamine.soundock.entity.PopHistory;
import dopamine.soundock.enums.PopStatus;
import dopamine.soundock.enums.PopTarget;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "후원 및 정산 내역 상세 응답 DTO")
public class PopHistoryResponse {
    // 사용자id, 사용수량, 사용내용(target), 사용대상(boardId, related_user)
    @Schema(description = "사용자 ID", example = "1")
    private Integer userId;

    @Schema(description = "후원(POP) ID", example = "102")
    private Integer popHistoryId;

    @Schema(description = "생성 일시", example = "2024-02-03 14:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdDatetime;

    @Schema(description = "후원 일시(후원,재화 사용시 생성)", example = "2024-02-03 14:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime requestedDatetime;

    @Schema(description = "후원(재화) 사용 승인 일시", example = "2024-02-03 14:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime approvedDatetime;

    @Schema(description = "후원(재화) 사용 취소 일시", example = "2024-02-03 14:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime cancelDatetime;

    @Schema(description = "변동 금액 (후원받은 금액)", example = "10000")
    private Integer changeAmount;

    @Schema(description = "대상 타입 (CHARGE:충전, DONATION:후원, FEATURED_BOARD:게시글홍보, RECEIVED:수령, EVENT:이벤트)", example = "RECEIVED")
    private PopTarget popTarget;

    @Schema(description = "상태 (PENDING:대기, COMPLETED:완료, CANCELED:취소, CANCEL_REQUEST:취소요청, SETTLEMENT_REQUEST:정산요청, SETTLEMENT_COMPLETED:정산완료, EXPIRED:만료)", example = "SETTLEMENT_REQUEST")
    private PopStatus popStatus;

    @Schema(description = "연관 정보 (게시글 제목 또는 유저 닉네임)")
    private RelatedInfo related;

    // PopHistory 정보를 바탕으로 게시글 또는 사용자에 대한 관련 정보를 생성
    public static RelatedInfo createRelatedInfo(PopHistory popHistory){
        PopTarget target = popHistory.getPopTarget();
        // board와 관련된 경우
        if ((target.equals(PopTarget.FEATURED_BOARD))
                && popHistory.getBoard() != null){
            // 글쓰기 상품 구매한 경우

            return new RelatedInfo(
                    popHistory.getBoard().getBoardId(),
                    popHistory.getBoard().getTitle()
            );
        } else if ((target.equals(PopTarget.DONATION))
                && popHistory.getRelatedUser() != null){
            // 사람한테 후원하기
            return new RelatedInfo(
                    popHistory.getRelatedUser().getId(),
                    popHistory.getRelatedUser().getNickname()
            );
        } else if ((target.equals(PopTarget.RECEIVED))
                && popHistory.getRelatedUser() != null){
            return new RelatedInfo(
                    popHistory.getRelatedUser().getId(),
                    popHistory.getRelatedUser().getNickname()
            );
        }
        // 관련 게시글 또는 유저가 없는 경우 빈 RelatedInfo 반환
        return new RelatedInfo(null, null);
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "후원 내역의 연관 대상 정보 (게시글 또는 유저)")
    public static class RelatedInfo {
        @Schema(description = "연관 대상 ID (게시글 ID 또는 유저 ID)", example = "55")
        Integer id;

        @Schema(description = "연관 대상 이름 (게시글 제목 또는 닉네임)", example = "멋진 프로젝트 후원")
        String name;
    }

    public static PopHistoryResponse fromSettlement(PopHistory popHistory){
        return PopHistoryResponse.builder()
                .userId(popHistory.getUser().getId())
                .popHistoryId(popHistory.getPopHistoryId())
                .popStatus(popHistory.getPopStatus())
                .popTarget(popHistory.getPopTarget())
                .changeAmount(popHistory.getChangeAmount())
                .requestedDatetime(popHistory.getRequestedDatetime())
                .approvedDatetime(popHistory.getApprovedDatetime())
                .cancelDatetime(popHistory.getCanceledDatetime())
                .build();
    }
}
