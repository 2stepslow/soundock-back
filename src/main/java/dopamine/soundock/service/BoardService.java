package dopamine.soundock.service;

import dopamine.soundock.dto.BoardCreateRequest;
import dopamine.soundock.dto.BoardResponse;
import dopamine.soundock.entity.Board;
import dopamine.soundock.repository.BoardRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class BoardService {
    private BoardRepository boardRepository;

    // 게시글 작성
    public int createNewBoard(BoardCreateRequest createRequest){
        // 사용자 로그인 확인

        // 프론트에서 받은 입력값 보여줌
        Board board = new Board();
        board.setTitle(createRequest.getTitle());
        board.setContent(createRequest.getContent());

        Board newBoard = boardRepository.save(board);
        return newBoard.getId();
    }
}
