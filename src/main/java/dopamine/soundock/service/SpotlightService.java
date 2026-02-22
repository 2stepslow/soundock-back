package dopamine.soundock.service;

import dopamine.soundock.dto.request.BoardCreateRequest;
import dopamine.soundock.dto.response.BoardResponse;
import dopamine.soundock.dto.response.FileUploadResponse;
import dopamine.soundock.entity.*;
import dopamine.soundock.enums.CategoryType;
import dopamine.soundock.enums.FileType;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

        // 로그인 유저가 메인 캐러셀 조회했을 경우만 게시글의 pop 차감
        if (email != null) {
            for (Board board : selectedBoards) {
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

    // Spotlight 게시글 상세 조회 시 pop 차감 (로그인 유저만)
    public void decreaseDetailViewPop(Integer boardId, String email) {
        if (email == null) return;

        String key = AppConstants.Redis.SPOTLIGHT_DETAIL_PREFIX
                + boardId + ":user:" + email;

        Boolean isFirst = redisTemplate.opsForValue()
                .setIfAbsent(key, "viewed", Duration.ofHours(AppConstants.Time.VIEW_COOLDOWN_HOURS));

        if (Boolean.TRUE.equals(isFirst)) {
            boardRepository.decreaseRemainingPop(boardId, AppConstants.Spotlight.DETAIL_VIEW_COST);
        }
    }
}
