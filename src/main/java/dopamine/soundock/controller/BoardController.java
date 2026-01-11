package dopamine.soundock.controller;

import dopamine.soundock.dto.ApiResponse;
import dopamine.soundock.dto.BoardCreateRequest;
import dopamine.soundock.dto.BoardResponse;
import dopamine.soundock.entity.Board;
import dopamine.soundock.repository.BoardRepository;
import dopamine.soundock.service.BoardService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
@AllArgsConstructor
@RequestMapping("/api/boards")
@RestController
public class BoardController {
    private BoardService boardService;
    private BoardRepository boardRepository;


    // 게시글 작성
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createNewBoard(@Valid @RequestBody BoardCreateRequest createRequest){
        int newBoardId = boardService.createNewBoard(createRequest);
        URI location = URI.create("/getDetailBoard/" + newBoardId);
        return ResponseEntity.created(location).body(ApiResponse.success("게시글 등록이 완료되었습니다."));
    }
}
