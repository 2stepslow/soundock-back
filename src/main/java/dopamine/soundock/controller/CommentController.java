package dopamine.soundock.controller;

import dopamine.soundock.dto.ApiResponse;
import dopamine.soundock.dto.CommentCreateRequest;
import dopamine.soundock.dto.CommentResponse;
import dopamine.soundock.entity.User;
import dopamine.soundock.repository.CommentRepository;
import dopamine.soundock.service.CommentService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/boards/post/{boardId}/comments")
@RestController
public class CommentController {
    private final CommentRepository commentRepository;
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createComment(
            @PathVariable("boardId") Integer boardId,
            @RequestBody CommentCreateRequest createRequest
    ){
        CommentResponse commentResponse = commentService.createComment(boardId, createRequest);
        return ResponseEntity.ok(ApiResponse.success("댓글이 등록되었습니다.", commentResponse));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<?>> deleteComment(@PathVariable("commentId") Integer commentId){
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.success("댓글이 삭제되었습니다."));
    }
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getComment(@PathVariable Integer boardId){
        List<CommentResponse> responses = commentService.getComment(boardId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
