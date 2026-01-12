package dopamine.soundock.service;

import dopamine.soundock.dto.BoardCreateRequest;
import dopamine.soundock.dto.BoardResponse;
import dopamine.soundock.entity.Board;
import dopamine.soundock.entity.Category;
import dopamine.soundock.entity.User;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.repository.BoardRepository;
import dopamine.soundock.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class BoardService {
    private BoardRepository boardRepository;
    private CategoryRepository categoryRepository;

    // 게시글 작성
    public int createNewBoard(BoardCreateRequest createRequest) {
        // 사용자 로그인 확인
        // 작성하려는 카테고리가 존재하는지 확인
        Category category = categoryRepository.findById(Integer.valueOf(createRequest.getCategory()))
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 카테고리 입니다."));
        // 카테고리 활성화 여부 확인
        if (!categoryRepository.findByIdAndIsActive(Integer.valueOf(createRequest.getCategory()), category.isActive())){
            throw new IllegalArgumentException("숨겨진 카테고리입니다.");
        }
        // 프론트에서 받은 입력값 보여줌
        Board board = new Board();
        board.setTitle(createRequest.getTitle());
        board.setContent(createRequest.getContent());
        board.setCategory(category);
        // 로그인한 유저의 닉네임을 받아올거임
        // board.setAuthor(user);

        // save는 새로운 행을 만들면서 데이터 저장
        Board newBoard = boardRepository.save(board);
        return newBoard.getId();
    }

    // 게시글 상세 조회
    public BoardResponse getDetailBoard(
            Integer boardId
    ) {
        Optional<Board> optionalBoard = boardRepository.findById(boardId);
        // 게시글 유무 확인
        if (optionalBoard.isEmpty()) {
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다.");
        }
        Board board = optionalBoard.get();

        // 게시글 삭제 여부 확인
        if (board.getDeletedDateTime() == null) {
            throw new ResourceNotFoundException("삭제된 게시글입니다.");
        }
        BoardResponse boardResponse = BoardResponse.builder()
                .title(board.getTitle())
                .content(board.getContent())
                .fileUrl(board.getFileUrl())
                .views(board.getViews())
                .likes(board.getLikes())
                .createdDateTime(board.getCreatedDateTime())
                .build();

        return boardResponse;
    }

    // 한 게시판의 모든 하위 카테고리를 포함한 게시판 목록 조회
    public List<BoardResponse> getAllBoardsByCategory(Integer categoryId) {
        // 부모 id가 존재하는 카테고리 찾기(서브 카테고리)
        List<Category> subCategories = categoryRepository.findByParentId(categoryId);
        // 하위 카테고리를 포함한 카테고리 배열 생성
        List<Integer> categoryIds = new ArrayList<>();
        for (Category category : subCategories){
            categoryIds.add(category.getId());
        }
        // 현재 카테고리 아이디도 포함 시킴
        categoryIds.add(categoryId);

        // 삭제되지 않은 게시글만을 조회
        List<Board> boards = boardRepository.findByDeletedDateTimeIsNullAndCategoryIdIn(categoryIds);

        List<BoardResponse> boardResponses = new ArrayList<>();

        for (Board board : boards) {
            BoardResponse newResponse = BoardResponse.builder()
                    .title(board.getTitle())
                    // .nickname()
                    .content(board.getContent())
                    .createdDateTime(board.getCreatedDateTime())
                    .views(board.getViews())
                    .likes(board.getLikes())
                    .build();

            boardResponses.add(newResponse);
        }
        return boardResponses;
    }

    // 한 게시판의 특정 하위 카테고리 모든 게시글 조회
    public List<BoardResponse> findAllBoardsBySubCategory(Integer parentId, String categoryType){
        // 부모 카테고리 밑의 하위 카테고리 조회
        List<Category> parentIdAndCategoryType = categoryRepository.findByParentIdAndCategoryType(parentId, categoryType);
        if (parentIdAndCategoryType.isEmpty()){
            throw new ResourceNotFoundException("올바른 경로가 아닙니다.");
        }
        // 부모 카테고리 밑의 하위 카테고리가 가지는 게시글 조회
        List<Board> boards = boardRepository.findByDeletedDateTimeIsNullAndCategoryParentIdAndCategoryCategoryType(parentId, categoryType);
        if (boards.isEmpty()){
            throw new ResourceNotFoundException("조회할 게시글이 존재하지 않습니다.");
        }
        List<BoardResponse> boardResponses = new ArrayList<>();
        for (Board board : boards){
            BoardResponse newResponse = BoardResponse.builder()
                    .title(board.getTitle())
                    .content(board.getContent())
                    .createdDateTime(board.getCreatedDateTime())
                    .views(board.getViews())
                    .likes(board.getLikes())
                    .build();

            boardResponses.add(newResponse);
        }
        return boardResponses;
    }
    // 게시글 삭제
    public void deleteBoard(Integer boardId){
        // 작성자와 현재 로그인한 유저가 같은지 검사
        // 삭제하려는 글의 id가 존재하는지 검사
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 게시글입니다.."));

        // 삭제된 게시글인지 조회
        List<Board> existBoard = boardRepository.findByDeletedDateTimeIsNull(boardId);
        if (existBoard!=null){
            throw new IllegalArgumentException("이미 삭제된 게시글입니다.");
        }
        boardRepository.deleteById(boardId);
    }

}
