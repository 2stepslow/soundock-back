package dopamine.soundock.controller;

import dopamine.soundock.dto.BoardCreateRequest;
import dopamine.soundock.dto.BoardResponse;
import dopamine.soundock.entity.Board;
import dopamine.soundock.repository.BoardRepository;
import dopamine.soundock.service.BoardService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@AllArgsConstructor
@RequestMapping("api/boards")
@RestController
public class BoardController {
    private BoardService boardService;
    private BoardRepository boardRepository;

    // 게시판 목록 조회
    @GetMapping("/{id}")// 카테고리 id
    public BoardResponse getAllBoards(@PathVariable int id){
         = boardService.getAllBoards(id);
    }

    // 게시글 작성
    @PostMapping("/{id}")
    public int createNewBoard(BoardCreateRequest createRequest){
        return boardService.createNewBoard(createRequest);
    }

    // 게시글 상세 조회
    @GetMapping("/{id}/")
    public BoardResponse getDetailBoard(@PathVariable int id){
        return boardService.getDetailBoard(id);
    }
}
