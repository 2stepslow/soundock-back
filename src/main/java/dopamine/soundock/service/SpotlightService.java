package dopamine.soundock.service;

import dopamine.soundock.dto.request.BoardCreateRequest;
import dopamine.soundock.dto.response.BoardResponse;
import dopamine.soundock.dto.response.FileUploadResponse;
import dopamine.soundock.entity.*;
import dopamine.soundock.enums.CategoryType;
import dopamine.soundock.enums.FileType;
import dopamine.soundock.enums.PopStatus;
import dopamine.soundock.enums.PopTarget;
import dopamine.soundock.exceptions.CustomException;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.global.constants.AppConstants;
import dopamine.soundock.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotlightService {

    private final BoardRepository boardRepository;
    private final BoardAttachmentsRepository boardAttachmentsRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final S3Service s3Service;
    private final Validatorservice attachmentValidator;
    private final PopHistoryRepository popHistoryRepository;
    private final RedisTemplate<String, String> redisTemplate;

    // Spotlight 게시글 작성
    @Transactional
    public int createSpotlightBoard(BoardCreateRequest createRequest,
                                    List<MultipartFile> files) throws IOException {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        Category category = categoryRepository.findByCategoryType(CategoryType.SPOTLIGHT)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 카테고리입니다."));

        // 첨부파일 검증
        attachmentValidator.validateFilesForCategory(CategoryType.SPOTLIGHT, files, null);

        // 입력된 재화량 검증
        Integer popAmount = createRequest.getPopAmount();
        if (popAmount == null || popAmount < AppConstants.Spotlight.MIN_POP_AMOUNT) {
            throw new CustomException(
                    "최소 " + AppConstants.Spotlight.MIN_POP_AMOUNT + " pop 이상 입력해주세요.",
                    HttpStatus.BAD_REQUEST);
        }

        // 유저 pop Balance 차감
        int updatedRows = userRepository.decreasePopBalance(email, popAmount);
        if (updatedRows == 0) {
            throw new CustomException("재화가 부족합니다. 현재 보유 재화를 확인해주세요.", HttpStatus.BAD_REQUEST);
        }

        // 게시글 저장
        Board board = new Board();
        board.setTitle(createRequest.getTitle());
        board.setContent(createRequest.getContent());
        board.setCategory(category);
        board.setUser(user);
        board.setRemainingPop(popAmount);
        Board savedBoard = boardRepository.save(board);

        // 재화 사용 내역 기록
        LocalDateTime now = LocalDateTime.now();
        PopHistory popHistory = PopHistory.builder()
                .changeAmount(-popAmount)
                .popStatus(PopStatus.PENDING)
                .popTarget(PopTarget.FEATURED_BOARD)
                .createdDatetime(now)
                .requestedDatetime(now)
                .board(savedBoard)
                .user(user)
                .build();
        popHistoryRepository.save(popHistory);

        // 첨부파일 업로드
        List<FileUploadResponse> uploadedFiles = s3Service.uploadFiles(files);
        for (int i = 0; i < uploadedFiles.size(); i++) {
            FileUploadResponse fileResponse = uploadedFiles.get(i);
            BoardAttachments attachment = BoardAttachments.builder()
                    .board(savedBoard)
                    .fileUrl(fileResponse.getFileUrl())
                    .fileKey(fileResponse.getFileKey())
                    .fileType(FileType.IMAGE)
                    .originalFilename(fileResponse.getOriginalFilename())
                    .sequence(i)
                    .build();
            boardAttachmentsRepository.save(attachment);
        }

        return savedBoard.getBoardId();
    }

    // 메인 캐러셀 조회 (랜덤 10개)
    @Transactional
    public List<BoardResponse> getCarouselSpotlights(String email) {
        List<Board> activeBoards = boardRepository.findActiveSpotlightBoards(CategoryType.SPOTLIGHT);

        if (activeBoards.isEmpty()) {
            return List.of();
        }

        // 랜덤 셔플 후 10개 선택
        // 원본 Boards 순서 유지를 위해 복사본 생성
        List<Board> shuffledBoards = new ArrayList<>(activeBoards);
        Collections.shuffle(shuffledBoards);
        List<Board> selectedBoards = shuffledBoards.stream()
                .limit(AppConstants.Spotlight.CAROUSEL_DISPLAY_COUNT)
                .toList();

        // 로그인 유저가 메인 캐러셀 조회했을 경우만 게시글의 pop 차감 (본인 게시글 제외)
        if (email != null) {
            for (Board board : selectedBoards) {
                if (board.getUser().getEmail().equals(email)) continue;

                String key = AppConstants.Redis.SPOTLIGHT_CAROUSEL_PREFIX
                        + board.getBoardId() + ":user:" + email;

                Boolean isFirst = redisTemplate.opsForValue()
                        .setIfAbsent(key, "viewed", Duration.ofHours(AppConstants.Time.VIEW_COOLDOWN_HOURS));

                if (Boolean.TRUE.equals(isFirst)) {
                    boardRepository.decreaseRemainingPop(
                            board.getBoardId(), AppConstants.Spotlight.CAROUSEL_VIEW_COST);
                }
            }
        }

        List<BoardResponse> responses = new ArrayList<>();
        for (Board board : selectedBoards) {
            List<BoardAttachments> attachments = board.getAttachments();
            String imageUrl = null;
            if (!attachments.isEmpty()) {
                imageUrl = attachments.getFirst().getFileUrl();
            }

            BoardResponse response = BoardResponse.builder()
                    .boardId(board.getBoardId())
                    .title(board.getTitle())
                    .nickname(board.getUser().getNickname())
                    .createdDateTime(board.getCreatedDateTime())
                    .views(board.getViews())
                    .likes(board.getLikes())
                    .countComment(board.getCountComment())
                    .imageUrl(imageUrl)
                    .categoryType(board.getCategory().getCategoryType())
                    .isDeleted(board.getUser().isDeleted())
                    .build();
            responses.add(response);
        }

        return responses;
    }

    // Spotlight 게시글 연장 (재화 추가 충전)
    @Transactional
    public void extendSpotlight(Integer boardId, int popAmount) {
        // 사용자 로그인 검증
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 수정하려는 게시글 확인
        Board board = boardRepository.findByBoardIdAndDeletedDateTimeIsNull(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없거나 삭제된 게시글입니다."));

        // 본인 게시글인지 확인
        if (!board.getUser().getId().equals(user.getId())) {
            throw new CustomException("본인이 작성한 게시글만 연장할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        // Spotlight 카테고리인지 확인
        if (board.getCategory().getCategoryType() != CategoryType.SPOTLIGHT) {
            throw new CustomException("Spotlight 게시글만 연장할 수 있습니다.", HttpStatus.BAD_REQUEST);
        }

        // 유저 pop 차감
        int updatedRows = userRepository.decreasePopBalance(email, popAmount);
        if (updatedRows == 0) {
            throw new CustomException("재화가 부족합니다. 현재 보유 재화를 확인해주세요.", HttpStatus.BAD_REQUEST);
        }

        // 잔여 재화 충전
        boardRepository.increaseRemainingPop(boardId, popAmount);

        // 만료된 게시글이면 만료 기록 초기화 (캐러셀 복귀)
        boardRepository.clearFeaturedExpiredDateTime(boardId);

        // 재화 사용 내역 기록
        LocalDateTime now = LocalDateTime.now();
        PopHistory popHistory = PopHistory.builder()
                .changeAmount(-popAmount)
                .popStatus(PopStatus.PENDING)
                .popTarget(PopTarget.FEATURED_BOARD)
                .createdDatetime(now)
                .requestedDatetime(now)
                .board(board)
                .user(user)
                .build();
        popHistoryRepository.save(popHistory);
    }

    // Spotlight 게시글 상세 조회 시 pop 차감 (로그인 유저만, 본인 게시글 제외)
    public void decreaseDetailViewPop(Board board, String email) {
        if (email == null) return;
        if (board.getUser().getEmail().equals(email)) return;

        String key = AppConstants.Redis.SPOTLIGHT_DETAIL_PREFIX
                + board.getBoardId() + ":user:" + email;

        Boolean isFirst = redisTemplate.opsForValue()
                .setIfAbsent(key, "viewed", Duration.ofHours(AppConstants.Time.VIEW_COOLDOWN_HOURS));

        if (Boolean.TRUE.equals(isFirst)) {
            boardRepository.decreaseRemainingPop(board.getBoardId(), AppConstants.Spotlight.DETAIL_VIEW_COST);
        }
    }
}
