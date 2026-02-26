package dopamine.soundock.service;

import dopamine.soundock.dto.response.BoardResponse;
import dopamine.soundock.entity.Board;
import dopamine.soundock.entity.BoardAttachments;
import dopamine.soundock.enums.CategoryType;
import dopamine.soundock.global.constants.AppConstants;
import dopamine.soundock.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {

    private final StringRedisTemplate redisTemplate;
    private final BoardRepository boardRepository;

    // 조회수 증가 (1점)
    public void incrementHotViewCount(Board board) {
        updateScore(board, AppConstants.Validation.VIEW_WEIGHT);
    }

    // 추천수 증가 (3점)
    public void incrementHotLikeCount(Board board) {
        updateScore(board, AppConstants.Validation.LIKE_WEIGHT);
    }

    // 추천 취소 (3점 차감)
    public void decrementHotLikeCount(Board board) {
        updateScore(board, -AppConstants.Validation.LIKE_WEIGHT);
    }

    public String getCurrentMonth() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    public String getCurrentWeek() {
        LocalDate today = LocalDate.now();
        // previousOrSame으로 한 주(월~일)을 전부 해당 주의 월요일 날짜로 Redis에 기록해서 주간 인기 게시글 조회 키를 만든다.
        return today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public String getPreviousMonth() {
        return LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    public String getPreviousWeek() {
        LocalDate thisMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return thisMonday.minusWeeks(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public void updateScore(Board board, int score) {
        String monthKey = AppConstants.Redis.RANKING_MONTH_PREFIX + board.getCategory().getCategoryType() + ":" + getCurrentMonth();
        String weekKey = AppConstants.Redis.RANKING_WEEK_PREFIX + board.getCategory().getCategoryType() + ":" + getCurrentWeek();

        // ZINCRBY: 기존 점수에 가중치를 더함
        redisTemplate.opsForZSet().incrementScore(monthKey, board.getBoardId().toString(), score);
        redisTemplate.opsForZSet().incrementScore(weekKey, board.getBoardId().toString(), score);

        // key 만료시간 추가 (month 40일, week 10일)
        redisTemplate.expire(monthKey, 40, TimeUnit.DAYS);
        redisTemplate.expire(weekKey, 10, TimeUnit.DAYS);
    }

    /**
     * 월간 Top8 조회 메서드
     */
    public List<BoardResponse> monthCategoryHotBoard(CategoryType categoryType) {
        String monthKey = AppConstants.Redis.RANKING_MONTH_PREFIX + categoryType + ":" + getCurrentMonth();
        String previousMonthKey = AppConstants.Redis.RANKING_MONTH_PREFIX + categoryType + ":" + getPreviousMonth();

        // 0부터 19까지 총 20개 가져오기 (삭제된 글이 있을 경우를 대비해서)
        int redisLimit = 19;
        // 실제 프론트에 줄 개수
        int targetSize = 8;
        Set<String> range = redisTemplate.opsForZSet().reverseRange(monthKey, 0, redisLimit);

        return getTopBoards(range, targetSize, previousMonthKey);

    }

    /**
     *각 카테고리별 주간 Top10 조회 메서드
     */
    public List<BoardResponse> weekCategoryHotBoard(CategoryType categoryType) {
        String weekKey = AppConstants.Redis.RANKING_WEEK_PREFIX + categoryType + ":" + getCurrentWeek();
        String previousWeekKey = AppConstants.Redis.RANKING_WEEK_PREFIX + categoryType + ":" + getPreviousWeek();

        // Redis에서 가져올 개수 (Showcase는 13개, 나머지는 19개)
        int redisLimit = (categoryType == CategoryType.SHOWCASE) ? 13 : 19;
        // 실제 프론트에 줄 개수 (Showcase는 4개 나머지는 10개 가져오기)
        int targetSize = (categoryType == CategoryType.SHOWCASE) ? 4 : 10;
        Set<String> range = redisTemplate.opsForZSet().reverseRange(weekKey, 0, redisLimit);

        return getTopBoards(range, targetSize, previousWeekKey);
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

    /**
     * 인기 게시글 조회 공통 메서드
     */
    private List<BoardResponse> getTopBoards(Set<String> range, int targetSize, String previousKey) {
        List<Integer> boardIds = new ArrayList<>();

        if (range != null) {
            for (String idString : range) {
                // String 타입을 Integer 타입으로 변환
                boardIds.add(Integer.parseInt(idString));
            }
        }

        if (boardIds.size() < targetSize && previousKey != null) {
            // 개수가 부족 할 경우 저번 주 (또는 저번 달) 인기 게시글을 조회해서 추가 시켜준다.
            Set<String> previousRange = redisTemplate.opsForZSet().reverseRange(previousKey, 0, targetSize);

            if (previousRange != null) {
                for (String key : previousRange) {
                    // String 타입을 Integer 타입으로 변환
                    Integer intKey = Integer.parseInt(key);

                    // 이번달과 저번달의 인기 게시글이 중복 되지 않는 경우에만 추가
                    if (!boardIds.contains(intKey)) {
                        boardIds.add(intKey);
                    }

                    // 8개 꽉 차면 for 문 종료
                    if (boardIds.size() == targetSize) {
                        break;
                    }
                }
            }
        }

        // 만약 둘 다 없으면 빈 리스트 반환
        if (boardIds.isEmpty()) {
            return new ArrayList<>();
        }

        // DB에서 게시글 조회
        List<Board> boards = boardRepository.findAllByBoardIdInAndDeletedDateTimeIsNull(boardIds);

        // 이렇게 하는 이유는 DB에서 게시글 조회 시 Redis에서 뽑아온 top 0~7 순서대로 정렬해서 값을 주지 않기 때문에
        // 순서 보정용으로 Map에 임시 저장 후 다시 재정렬 하는 과정을 거치기 위해서 필요함
        Map<Integer, Board> boardMap = new HashMap<>();
        for (Board board : boards) {
            boardMap.put(board.getBoardId(), board);
        }

        // Redis에서 가져온 boardIds 순서대로 최종 리스트 생성
        List<BoardResponse> responses = new ArrayList<>();
        for (Integer boardId : boardIds) {
            Board board = boardMap.get(boardId);

            // Redis에는 존재하지만 혹시나 DB에서 조회되지 않거나, Hard delete 된 게시글이 생길 경우 NPE(null 예외) 방지를 위해 건너뛴다
            if (board == null) {
                continue;
            }

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
                    .profileUrl(board.getUser().getProfileUrl())
                    .build();
            responses.add(newResponse);

            // Redis에서 뽑아온 최대 개수 중 각 카테고리마다 정한 개수에 도달할 경우 for 문 종료
            if (responses.size() == targetSize) {
                break;
            }
        }

        return responses;
    }
}
