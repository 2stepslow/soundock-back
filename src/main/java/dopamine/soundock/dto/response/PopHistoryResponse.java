package dopamine.soundock.dto.response;

import dopamine.soundock.entity.PopHistory;
import dopamine.soundock.enums.PopTarget;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PopHistoryResponse {
    // 사용일시, 사용수량, 사용내용(target), 사용대상(boardId, related_user)
    private LocalDateTime usageDatetime;
    private Integer changeAmount;
    private PopTarget popTarget;
    private RelatedInfo related;

    //
    public static RelatedInfo createRelatedInfo(PopHistory popHistory){
        PopTarget target = popHistory.getPopTarget();
        // board와 관련된 경우
        if ((target.equals(PopTarget.DONATION) || target.equals(PopTarget.FEATURED_BOARD))
                && popHistory.getBoard() != null){
            // 게시글에 후원하거나 글쓰기 상품 구매한 경우

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
        }
        return null;
    }

    @Getter
    @AllArgsConstructor
    public static class RelatedInfo {
        Integer id;
        String name;
    }
}
