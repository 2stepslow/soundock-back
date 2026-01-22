package dopamine.soundock.controller;

import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.dto.request.CommentCreateRequest;
import dopamine.soundock.dto.response.CommentResponse;
import dopamine.soundock.enums.CategoryType;
import dopamine.soundock.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/boards/{boardId}/comments")
@RestController
public class CommentController {
    private final CommentService commentService;

    // 댓글 작성
    @PostMapping
    public ResponseEntity<RestResponse<?>> createComment(
            @PathVariable(required = true) Integer boardId,
            @RequestBody CommentCreateRequest createRequest
    ){
        CommentResponse commentResponse = commentService.createComment(boardId, createRequest);
        return ResponseEntity.ok(RestResponse.success("댓글이 등록되었습니다.", commentResponse));
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<RestResponse<?>> deleteComment(
            @PathVariable(required = true) Integer boardId,
            @PathVariable(required = true) Integer commentId
    ){
        commentService.deleteComment(boardId, commentId);
        return ResponseEntity.ok(RestResponse.success("댓글이 삭제되었습니다."));
    }

    // 댓글 조회
    @GetMapping
    public ResponseEntity<RestResponse<?>> getComment(
            @PathVariable(required = true) Integer boardId
    ){
        List<CommentResponse> responses = commentService.getComment(boardId);
        return ResponseEntity.ok(RestResponse.success(responses));
    }
}
