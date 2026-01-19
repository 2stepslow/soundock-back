package dopamine.soundock.controller;

import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.dto.BoardCreateRequest;
import dopamine.soundock.dto.BoardResponse;
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
    public ResponseEntity<RestResponse<?>> createNewBoard(@Valid @RequestBody BoardCreateRequest createRequest){
        int newBoardId = boardService.createNewBoard(createRequest);
        URI location = URI.create("/getDetailBoard/" + newBoardId);
        return ResponseEntity.created(location).body(RestResponse.success("게시글 등록이 완료되었습니다."));
    }
    // 게시글 상세 조회
    // 이렇게 api 받으면 카테고리별 목록 조회 충돌 날 수 있슴
    @GetMapping("/post/{boardId}")
    public ResponseEntity<RestResponse<BoardResponse>> getDetailBoard(@PathVariable Integer boardId){
        BoardResponse boardResponse = boardService.getDetailBoard(boardId);
        return ResponseEntity.ok(RestResponse.success(boardResponse));
    }

    // 게시판 카테고리별 목록 조회
    @GetMapping({
            "/{categoryId}",
            "/{categoryId}/{subCategory}",
            "/{categoryId}/{subCategory}/{boardId}"})// 카테고리 id
    public ResponseEntity<RestResponse<?>> getBoards(
            @PathVariable Integer categoryId,
            @PathVariable (required = false) String subCategory,
            @PathVariable (required = false) Integer boardId
    ){
        if (boardId!=null){
            BoardResponse boardResponse = boardService.getDetailBoard(boardId);
            return ResponseEntity.ok(RestResponse.success(boardResponse));
        }
        if (subCategory!=null){
            List<BoardResponse> boardResponses = boardService.findAllBoardsBySubCategory(categoryId,subCategory);
            return ResponseEntity.ok(RestResponse.success(boardResponses));
        }

        List<BoardResponse> boardResponses = boardService.getAllBoardsByCategory(categoryId);
        return ResponseEntity.ok(RestResponse.success(boardResponses));
    }
    // 게시글 삭제
    @DeleteMapping("/{boardId}")
    public ResponseEntity<RestResponse<?>> deleteBoard(@PathVariable Integer boardId){
        boardService.deleteBoard(boardId);
        return ResponseEntity.ok(RestResponse.success());
    }
    // 게시글 수정
    @PatchMapping("/{boardId}")
    public ResponseEntity<RestResponse<?>> updateBoard(
            @PathVariable Integer boardId,
            @RequestBody BoardCreateRequest updaterequest
    ){
        boardService.updateBoard(boardId, updaterequest);
        return ResponseEntity.ok(RestResponse.success("게시글 수정이 완료되었습니다."));
    }
}
