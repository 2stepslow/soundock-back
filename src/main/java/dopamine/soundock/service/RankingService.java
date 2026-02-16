package dopamine.soundock.service;

import dopamine.soundock.dto.response.BoardResponse;
import dopamine.soundock.entity.Board;
import dopamine.soundock.entity.BoardAttachments;
import dopamine.soundock.global.constants.AppConstants;
import dopamine.soundock.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {

    private final StringRedisTemplate redisTemplate;
    private final BoardRepository boardRepository;

    // 조회수 증가 (1점)
    public void incrementHotViewCount(Integer boardId) {
        updateScore(boardId, AppConstants.Validation.VIEW_WEIGHT);
    }

    // 추천수 증가 (5점)
    public void incrementHotLikeCount(Integer boardId) {
        updateScore(boardId, AppConstants.Validation.LIKE_WEIGHT);
    }

    // 추천 취소 (5점 차감)
    public void decrementHotLikeCount(Integer boardId) {
        updateScore(boardId, -AppConstants.Validation.LIKE_WEIGHT);
    }

    public String getCurrentMonth() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    public void updateScore(Integer boardId, int score) {
        String key = AppConstants.Redis.RANKING_MONTH_PREFIX + getCurrentMonth();
        // ZINCRBY: 기존 점수에 가중치를 더함
        redisTemplate.opsForZSet().incrementScore(key, boardId.toString(), score);
    }

    /**
     * 메인 페이지 월가나 Top8 조회 메서드
     */
    public List<BoardResponse> mainHotBoard() {
        String key = AppConstants.Redis.RANKING_MONTH_PREFIX + getCurrentMonth();

        // 0부터 7까지 총 8개 가져오기
        Set<String> range = redisTemplate.opsForZSet().reverseRange(key, 0, 7);
        // 만약 없으면 빈 리스트 반환
        if (range == null || range.isEmpty()) {
            return new ArrayList<>();
        }

        // String 타입을 Integer 타입으로 변환
        List<Integer> boardIds = new ArrayList<>();
        for (String idString : range) {
            boardIds.add(Integer.parseInt(idString));
        }

        // DB에서 게시글 조회
        List<Board> boards = boardRepository.findAllByBoardIdInAndDeletedDateTimeIsNull(boardIds);

        // 순서 보정용 Map에 임시 저장
        Map<Integer, Board> boardMap = new HashMap<>();
        for (Board board : boards) {
            boardMap.put(board.getBoardId(), board);
        }

        // Redis에서 가져온 boardIds 순서대로 최종 리스트 생성
        List<BoardResponse> responses = new ArrayList<>();
        for (Integer id : boardIds) {
            Board board = boardMap.get(id);
            String imageUrl = getImageUrl(board);

            BoardResponse newResponse = BoardResponse.builder()
                    .boardId(board.getBoardId())
                    .title(board.getTitle())
                    .nickname(board.getUser().getNickname())
                    .createdDateTime(board.getCreatedDateTime())
                    .views(board.getViews())
                    .likes(board.getLikes())
                    .countComment(board.getCountComment())
                    .imageUrl(imageUrl)
                    .categoryType(board.getCategory().getCategoryType())
                    .build();
            responses.add(newResponse);
        }

        return responses;
    }

    /**
     * 이미지 Url 뽑는 메서드
     */
    private static @Nullable String getImageUrl(Board board) {
        List<BoardAttachments> attachments = board.getAttachments();
        String imageUrl = null;
        if (!attachments.isEmpty()) {
            imageUrl = attachments.getFirst().getFileUrl();
        }

        // PLAYLIST 썸네일 넣어주기
        if (board.getPlaylist() != null
                && board.getPlaylist().getThumbnailUrl() != null) {
            imageUrl = board.getPlaylist().getThumbnailUrl();
        }

        // Board.linkUrl이 있으면 우선 사용 (SHOWCASE 썸네일,자동재생용)
        if (board.getLinkUrl() != null && !board.getLinkUrl().isEmpty()) {
            imageUrl = board.getLinkUrl();
        }
        return imageUrl;
    }
}
