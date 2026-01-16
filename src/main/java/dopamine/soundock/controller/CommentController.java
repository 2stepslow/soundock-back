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

@AllArgsConstructor
@RequestMapping("/api/boards/{categoryId}/{subCategoryType}/{boardId}/comments")
@RestController
public class CommentController {
    private CommentRepository commentRepository;
    private CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createComment(
            @PathVariable("boardId") Integer boardId,
            @RequestBody CommentCreateRequest createRequest
    ){
        CommentResponse commentResponse = commentService.createComment(boardId, createRequest);
        return ResponseEntity.ok(ApiResponse.success("댓글이 등록되었습니다.", commentResponse));
    }

}
