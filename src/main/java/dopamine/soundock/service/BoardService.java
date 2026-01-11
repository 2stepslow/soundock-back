package dopamine.soundock.service;

import dopamine.soundock.dto.BoardCreateRequest;
import dopamine.soundock.dto.BoardResponse;
import dopamine.soundock.entity.Board;
import dopamine.soundock.entity.Category;
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
        if (!categoryRepository.findByIdAndIsActive(Integer.valueOf(createRequest.getCategory()))){
            throw new IllegalArgumentException("숨겨진 카테고리입니다.");
        }
        // 프론트에서 받은 입력값 보여줌
        Board board = new Board();
        board.setTitle(createRequest.getTitle());
        board.setContent(createRequest.getContent());
        board.setCategory(category);

        Board newBoard = boardRepository.save(board);
        return newBoard.getId();
    }
}
