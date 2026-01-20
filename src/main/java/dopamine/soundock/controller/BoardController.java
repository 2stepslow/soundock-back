package dopamine.soundock.controller;

import dopamine.soundock.dto.ApiResponse;
import dopamine.soundock.dto.BoardCreateRequest;
import dopamine.soundock.dto.BoardResponse;
import dopamine.soundock.entity.Board;
import dopamine.soundock.enums.CategoryType;
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

    // 게시글 작성
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createNewBoard(@Valid @RequestBody BoardCreateRequest createRequest){
        int newBoardId = boardService.createNewBoard(createRequest);
        URI location = URI.create("/getDetailBoard/" + newBoardId);
        return ResponseEntity.created(location).body(ApiResponse.success("게시글 등록이 완료되었습니다."));
    }
    // 게시글 상세 조회
    @GetMapping("/{categoryType}/{boardId}")
    public ResponseEntity<ApiResponse<BoardResponse>> getDetailBoard(
            @PathVariable(required = true) CategoryType categoryType,
            @PathVariable(required = true) Integer boardId
    ){
        BoardResponse boardResponse = boardService.getDetailBoard(boardId, categoryType);
        return ResponseEntity.ok(ApiResponse.success(boardResponse));
    }

    // 게시판 카테고리별 목록 조회
    @GetMapping("/{categoryType}")
    public ResponseEntity<ApiResponse<?>> getBoards(
            @PathVariable(required = true) CategoryType categoryType
    ){
        // subCategory와 일치하는 게시글 목록 조회
        List<BoardResponse> boardResponses = boardService.findAllBoardsBySubCategory(categoryType);
        return ResponseEntity.ok(ApiResponse.success(boardResponses));
        }

    // 게시글 삭제
    @DeleteMapping("/{categoryType}/{boardId}")
    public ResponseEntity<ApiResponse<?>> deleteBoard(
            @PathVariable(required = true) CategoryType categoryType,
            @PathVariable(required = true) Integer boardId
    ){
        boardService.deleteBoard(boardId, categoryType);
        return ResponseEntity.ok(ApiResponse.success());
    }
    // 게시글 수정
    @PatchMapping("/{categoryType}/{boardId}")
    public ResponseEntity<ApiResponse<?>> updateBoard(
            @PathVariable(required = true) CategoryType categoryType,
            @PathVariable(required = true) Integer boardId,
            @RequestBody BoardCreateRequest updaterequest
    ){
        boardService.updateBoard(boardId, categoryType, updaterequest);
        return ResponseEntity.ok(ApiResponse.success("게시글 수정이 완료되었습니다."));
    }
}
