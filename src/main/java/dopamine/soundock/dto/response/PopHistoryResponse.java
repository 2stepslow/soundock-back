package dopamine.soundock.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import dopamine.soundock.entity.PopHistory;
import dopamine.soundock.enums.PopStatus;
import dopamine.soundock.enums.PopTarget;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PopHistoryResponse {
    // 사용자id, 사용수량, 사용내용(target), 사용대상(boardId, related_user)
    private Integer userId;

    private Integer popHistoryId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdDatetime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime requestedDatetime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime approvedDatetime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime cancelDatetime;

    private Integer changeAmount;
    private PopTarget popTarget;
    private PopStatus popStatus;
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
    public static class RelatedInfo {
        Integer id;
        String name;
    }
}
