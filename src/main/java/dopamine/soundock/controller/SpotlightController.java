package dopamine.soundock.controller;

import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.dto.request.BoardCreateRequest;
import dopamine.soundock.dto.request.SpotlightExtendRequest;
import dopamine.soundock.dto.response.BoardResponse;
import dopamine.soundock.service.SpotlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RequestMapping("/api/spotlight")
@RestController
@RequiredArgsConstructor
public class SpotlightController {

    private final SpotlightService spotlightService;

    @Operation(
            summary = "Spotlight 게시글 작성",
            description = "재화를 소모하여 Spotlight 홍보 게시글 작성합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "게시글 등록 성공", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "재화 부족 또는 파일 검증 실패", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "403", description = "로그인 필요", content = @Content(schema = @Schema(implementation = RestResponse.class))),
    })
    // Spotlight 게시글 작성
    @PostMapping
    public ResponseEntity<RestResponse<?>> createSpotlightBoard(
            @Valid @RequestPart("data") BoardCreateRequest createRequest,
            @RequestPart("files") List<MultipartFile> files
    ) throws IOException {
        int newBoardId = spotlightService.createSpotlightBoard(createRequest, files);
        URI location = URI.create("/getDetailBoard/" + newBoardId);
        return ResponseEntity.created(location).body(RestResponse.success("Spotlight 게시글 등록이 완료되었습니다."));
    }

    @Operation(
            summary = "메인 캐러셀 Spotlight 게시글 랜덤 조회",
            description = "메인 페이지 캐러셀에 표시할 Spotlight 게시글 10개를 랜덤 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = BoardResponse.class))),
    })
    // 메인 캐러셀 조회
    @GetMapping("/carousel")
    public ResponseEntity<RestResponse<List<BoardResponse>>> getCarouselSpotlights(
            @AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : username") String email
    ) {
        List<BoardResponse> responses = spotlightService.getCarouselSpotlights(email);
        return ResponseEntity.ok(RestResponse.success(responses));
    }

    @Operation(
            summary = "Spotlight 게시글 연장",
            description = "재화를 추가 소모하여 Spotlight 게시글의 게시 기간을 연장합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "연장 성공", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "재화 부족 또는 최소 금액 미달", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "403", description = "본인 게시글이 아님", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "게시글 없음", content = @Content(schema = @Schema(implementation = RestResponse.class))),
    })
    // Spotlight 게시글 연장 (재화 추가 충전)
    @PostMapping("/{boardId}/extend")
    public ResponseEntity<RestResponse<?>> extendSpotlight(
            @PathVariable Integer boardId,
            @Valid @RequestBody SpotlightExtendRequest request
    ) {
        spotlightService.extendSpotlight(boardId, request.getPopAmount());
        return ResponseEntity.ok(RestResponse.success("Spotlight 게시글 연장이 완료되었습니다."));
    }

}
