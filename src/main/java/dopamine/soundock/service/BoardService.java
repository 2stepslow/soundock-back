package dopamine.soundock.service;

import dopamine.soundock.dto.BoardCreateRequest;
import dopamine.soundock.dto.BoardResponse;
import dopamine.soundock.entity.Board;
import dopamine.soundock.entity.Category;
import dopamine.soundock.entity.User;
import dopamine.soundock.enums.CategoryType;
import dopamine.soundock.exceptions.AuthRejectedException;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.repository.BoardRepository;
import dopamine.soundock.repository.CategoryRepository;
import dopamine.soundock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@RequiredArgsConstructor
@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    // 게시글 작성
    public int createNewBoard(CategoryType categoryType, BoardCreateRequest createRequest) {
        // 사용자 로그인 확인
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));
        // 카테고리 입력값 검증
       Category category = categoryRepository.findByCategoryType(categoryType)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 카테고리입니다."));

        // 프론트에서 받은 입력값 보여줌
        Board board = new Board();
        board.setTitle(createRequest.getTitle());
        board.setContent(createRequest.getContent());
        board.setCategory(category);
        board.setUser(user);

        // save는 새로운 행을 만들면서 데이터 저장
        Board newBoard = boardRepository.save(board);
        return newBoard.getBoardId();
    }

    // 게시글 상세 조회
    public BoardResponse getDetailBoard(
            Integer boardId, CategoryType categoryType
    ) {
        // 카테고리와 boardId에 해당하는 삭제되지 않은 게시글인지 확인
        Board board = getValidatedBoard(boardId, categoryType);

        BoardResponse boardResponse = BoardResponse.builder()
                .title(board.getTitle())
                .nickname(board.getUser().getNickname())
                .content(board.getContent())
                .views(board.getViews())
                .likes(board.getLikes())
                .createdDateTime(board.getCreatedDateTime())
                .build();

        return boardResponse;
    }

    // 한 카테고리 내의 모든 게시글 조회
    public List<BoardResponse> findAllBoardsByCategoryType(CategoryType categoryType){
        List<Board> boards = boardRepository.findByDeletedDateTimeIsNullAndCategoryCategoryType(categoryType);
        if (boards.isEmpty()){
            throw new ResourceNotFoundException("현재 카테고리에 작성된 게시글이 없습니다.");
        }

        // 게시글 목록 표시
        List<BoardResponse> boardResponses = new ArrayList<>();
        for (Board board : boards){
            BoardResponse newResponse = BoardResponse.builder()
                    .title(board.getTitle())
                    .nickname(board.getUser().getNickname())
                    .createdDateTime(board.getCreatedDateTime())
                    .views(board.getViews())
                    .likes(board.getLikes())
                    .build();

            boardResponses.add(newResponse);
        }
        return boardResponses;
    }
    // 게시글 삭제
    public void deleteBoard(Integer boardId, CategoryType categoryType){
        // 카테고리와 boardId에 해당하는 삭제되지 않은 게시글인지 확인
        Board board = getValidatedBoard(boardId, categoryType);

        // 작성자와 현재 로그인한 유저가 같은지 검사
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 로그인한 유저가 작성한 게시글이 있는지 확인
        if (!board.getUser().getId().equals(user.getId())){
            throw new AuthRejectedException("게시글 작성자와 로그인 정보가 일치하지 않습니다.");
        }
        // 게시글 soft Delete
        board.setDeletedDateTime(LocalDateTime.now());
        board.setDeleted(true);
        boardRepository.save(board);

    }
    // 게시글 수정
    public void updateBoard(Integer boardId, CategoryType categoryType, BoardCreateRequest updaterequest){
        // 카테고리와 boardId에 해당하는 삭제되지 않은 게시글인지 확인
        Board board =getValidatedBoard(boardId, categoryType);

        // 작성자와 현재 로그인한 유저가 같은지 검사
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 로그인한 유저가 작성한 게시글이 있는지 확인
        if (!board.getUser().getId().equals(user.getId())){
            throw new AuthRejectedException("게시글 작성자와 로그인 정보가 일치하지 않습니다.");
        }
        // 수정하려는 사항
        if (updaterequest.getTitle() != null){
            board.setTitle(updaterequest.getTitle());
        }
        if (updaterequest.getContent() != null){
            board.setContent(updaterequest.getContent());
        }
        if (updaterequest.getFileUrl() != null){
            board.setFileUrl(updaterequest.getFileUrl());
        }
        // 게시글 수정일 업데이트
        board.setUpdatedDateTime(LocalDateTime.now());
        boardRepository.save(board);
    }

    // 카테고리와 boardId에 해당하는 삭제되지 않은 게시글 확인 메서드
    @Transactional(readOnly = true)
    public Board getValidatedBoard(Integer boardId, CategoryType categoryType) {
        return boardRepository.findByBoardIdAndDeletedDateTimeIsNullAndCategoryCategoryType(boardId, categoryType)
                .orElseThrow(() -> new ResourceNotFoundException("해당 카테고리에서 게시글을 찾을 수 없거나 삭제된 게시글입니다."));
    }

}
