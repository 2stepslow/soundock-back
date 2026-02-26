package dopamine.soundock.controller;

import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.dto.request.CommentCreateRequest;
import dopamine.soundock.dto.response.CommentListResponse;
import dopamine.soundock.dto.response.CommentResponse;
import dopamine.soundock.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/boards/{boardId}/comments")
@RestController
public class CommentController {
    private final CommentService commentService;

    @Operation(
            summary = "특정 카테고리 내 게시글에 댓글 작성",
            description = "categoryType 내 boarId와 일치하는 게시글 내 댓글 작성."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 등록 성공", content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "403", description = "로그인 필요", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "미등록 카테고리, 게시글", content = @Content(schema = @Schema(implementation = RestResponse.class))),
    })
    // 댓글 작성
    @PostMapping
    public ResponseEntity<RestResponse<CommentResponse>> createComment(
            @PathVariable(required = true) Integer boardId,
            @Valid @RequestBody CommentCreateRequest createRequest
    ){
        CommentResponse commentResponse = commentService.createComment(boardId, createRequest);
        return ResponseEntity.ok(RestResponse.success("댓글이 등록되었습니다.", commentResponse));
    }

    @Operation(
            summary = "로그인한 유저가 작성한 댓글 삭제",
            description = "로그인한 유저가 categoryType 내 boarId와 일치하는 게시글에 작성한 댓글 삭제."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 삭제 성공", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "403", description = "로그인 필요", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "미등록 카테고리, 게시글", content = @Content(schema = @Schema(implementation = RestResponse.class))),
    })
    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<RestResponse<Integer>> deleteComment(
            @PathVariable(required = true) Integer boardId,
            @PathVariable(required = true) Integer commentId
    ){
        Integer countComment = commentService.deleteComment(boardId, commentId);
        return ResponseEntity.ok(RestResponse.success("댓글이 삭제되었습니다.", countComment));
    }

    @Operation(
            summary = "게시글에 등록된 댓글 조회",
            description = "categoryType 내 boarId와 일치하는 게시글에 등록된 모든 댓글 조회"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 조회", content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "404", description = "미등록 카테고리, 게시글", content = @Content(schema = @Schema(implementation = RestResponse.class))),
    })
    // 댓글 조회
    @GetMapping
    public ResponseEntity<RestResponse<CommentListResponse>> getComment(
            @PathVariable(required = true) Integer boardId
    ){
        CommentListResponse responses = commentService.getComment(boardId);
        return ResponseEntity.ok(RestResponse.success(responses));
    }

    // 댓글 수정
    @PatchMapping("/{commentId}")
    public ResponseEntity<RestResponse<CommentResponse>> updateComment(
            @PathVariable Integer commentId,
            @Valid @RequestBody CommentCreateRequest updateRequest
    ){
        CommentResponse commentResponse = commentService.updateComment(commentId, updateRequest);
        return ResponseEntity.ok(RestResponse.success("댓글 수정이 완료되었습니다.", commentResponse));
    }

    // 댓글 추천
    @PostMapping("/{commentId}/like")
    public ResponseEntity<RestResponse<CommentResponse>> recommendComment(
            @PathVariable Integer commentId
    ){
        CommentResponse commentResponse = commentService.recommendComment(commentId);
        return ResponseEntity.ok(RestResponse.success(commentResponse));
    }
}
