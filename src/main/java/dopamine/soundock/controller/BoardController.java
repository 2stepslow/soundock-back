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
    // 게시글 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardResponse>> getDetailBoard(Integer boardId){
        BoardResponse boardResponse = boardService.getDetailBoard(boardId);
        return ResponseEntity.ok(ApiResponse.success(boardResponse));
    }

    // 게시판 카테고리별 목록 조회
    @GetMapping({
            "/{categoryId}",
            "/{categoryId}/{subCategoryId}",
            "/{categoryId}/{subCategoryId}/{boardId}"})// 카테고리 id
    public ResponseEntity<ApiResponse<?>> getBoards(
            @PathVariable Integer categoryId,
            @PathVariable (required = false) String categoryType,
            @PathVariable (required = false) Integer boardId
    ){
        if (boardId!=null){
            BoardResponse boardResponse = boardService.getDetailBoard(boardId);
            return ResponseEntity.ok(ApiResponse.success(boardResponse));
        }
        if (categoryType!=null){
            List<BoardResponse> boardResponses = boardService.findAllBoardsBySubCategory(categoryId,categoryType);
            return ResponseEntity.ok(ApiResponse.success(boardResponses));
        }

        List<BoardResponse> boardResponses = boardService.getAllBoardsByCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(boardResponses));
    }
    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteBoard(int id){
        boardService.deleteBoard(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
    // 게시글 수정
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateBoard(Integer boardId, BoardCreateRequest updaterequest){
        boardService.updateBoard(boardId, updaterequest);
        return ResponseEntity.ok(ApiResponse.success("게시글 수정이 완료되었습니다."));
    }
}
