package dopamine.soundock.service;

import dopamine.soundock.dto.BoardCreateRequest;
import dopamine.soundock.dto.BoardResponse;
import dopamine.soundock.entity.Board;
import dopamine.soundock.entity.Category;
import dopamine.soundock.entity.User;
import dopamine.soundock.exceptions.AuthRejectedException;
import dopamine.soundock.exceptions.AuthenticationFailException;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.repository.BoardRepository;
import dopamine.soundock.repository.CategoryRepository;
import dopamine.soundock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@RequiredArgsConstructor
@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private CategoryRepository categoryRepository;

    // 게시글 작성
    public int createNewBoard(BoardCreateRequest createRequest) {
        // 사용자 로그인 확인
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationFailException("로그인 정보가 일치하지 않습니다."));

        // 프론트에서 받은 입력값 보여줌
        Board board = new Board();
        board.setTitle(createRequest.getTitle());
        board.setContent(createRequest.getContent());
        board.setUser(user);

        // save는 새로운 행을 만들면서 데이터 저장
        Board newBoard = boardRepository.save(board);
        return newBoard.getBoardId();
    }

    // 게시글 상세 조회
    public BoardResponse getDetailBoard(
            Integer parentId, String categoryType, Integer boardId
    ) {
        Board board = boardRepository.findByBoardIdAndDeletedDateTimeIsNullAndCategoryCategoryTypeAndCategoryParentId(boardId, categoryType, parentId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 게시글이거나 카테고리 정보가 일치하지 않습니다."));

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

    // 한 상위 카테고리의 특정 하위 카테고리 모든 게시글 조회
    public List<BoardResponse> findAllBoardsBySubCategory(Integer parentId, String categoryType){
        List<Board> boards = boardRepository.findByDeletedDateTimeIsNullAndCategoryParentIdAndCategoryCategoryType(parentId, categoryType);
        if (boards.isEmpty()){
            throw new ResourceNotFoundException("해당 카테고리에 표시할 게시글이 없습니다.");
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
    public void deleteBoard(Integer boardId, String categoryType, Integer parentId){
        // 삭제되지 않고 해당 api 경로에 해당하는 게시글이 존재하는지 확인
        Board board = boardRepository.findByBoardIdAndDeletedDateTimeIsNullAndCategoryCategoryTypeAndCategoryParentId(boardId, categoryType, parentId)
                        .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 게시글이거나 카테고리 정보가 일치하지 않습니다."));

        // 작성자와 현재 로그인한 유저가 같은지 검사
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationFailException("로그인 정보가 일치하지 않습니다."));

        // 로그인한 유저의 id를 requestId로 대입
        Integer requestId = user.getId();

        // 로그인한 유저가 작성한 게시글이 있는지 확인
        Optional<Board> boardOptional = boardRepository.findById(requestId);
        if (boardOptional.isEmpty()){
            throw new ResourceNotFoundException("작성자의 게시글을 찾을 수 없습니다.");
        }
        Board targetBoard = boardOptional.get();

        // 작성자가 현재 로그인한 유저의 id와 게시글 작성한 유저 id가 같은지 확인
        if (!targetBoard.getUser().getId().equals(requestId)) {
            throw new AuthRejectedException("작성자의 id와 일치하지 않습니다.");
        }
        boardRepository.deleteById(boardId);
    }
    // 게시글 수정
    public void updateBoard(Integer boardId, String categoryType, Integer parentId, BoardCreateRequest updaterequest){
        // 삭제되지 않고 해당 api 경로에 해당하는 게시글이 존재하는지 확인
        Board board = boardRepository.findByBoardIdAndDeletedDateTimeIsNullAndCategoryCategoryTypeAndCategoryParentId(boardId, categoryType, parentId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 게시글이거나 카테고리 정보가 일치하지 않습니다."));

        // 작성자와 현재 로그인한 유저가 같은지 검사
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationFailException("로그인 정보가 일치하지 않습니다."));

        // 로그인한 유저의 id를 requestId로 대입
        Integer requestId = user.getId();

        // 로그인한 유저가 작성한 게시글이 있는지 확인
        Optional<Board> boardOptional = boardRepository.findById(requestId);
        if (boardOptional.isEmpty()){
            throw new ResourceNotFoundException("작성자의 게시글을 찾을 수 없습니다.");
        }
        Board targetBoard = boardOptional.get();

        // 작성자가 현재 로그인한 유저의 id와 게시글 작성한 유저 id가 같은지 확인
        if (!targetBoard.getUser().getId().equals(requestId)) {
            throw new AuthRejectedException("작성자의 id와 일치하지 않습니다.");
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
}
