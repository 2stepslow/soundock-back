package dopamine.soundock.controller;

import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.dto.request.BoardCreateRequest;
import dopamine.soundock.dto.request.BoardSearchRequest;
import dopamine.soundock.dto.response.BoardResponse;
import dopamine.soundock.enums.CategoryType;
import dopamine.soundock.service.BoardService;
import dopamine.soundock.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;
@RequestMapping("/api/boards")
@RestController
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final RankingService rankingService;

    @Operation(
            summary = "특정 카테고리 내 게시글 작성",
            description = "categoryType에 해당하는 카테고리 이동 후 게시글을 작성합니다." +
                    "작성이 완료되었다면 완료 메시지를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 등록 성공", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "403", description = "로그인 필요", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "미등록 카테고리", content = @Content(schema = @Schema(implementation = RestResponse.class))),
    })
    // 게시글 작성
    @PostMapping("/category/{categoryType}")
    public ResponseEntity<RestResponse<?>> createNewBoard(
            @PathVariable(required = true) CategoryType categoryType,
            @Valid @RequestPart BoardCreateRequest createRequest,
            @RequestPart(required = false) List<MultipartFile> files
    ) throws IOException {
        int newBoardId = boardService.createBoard(categoryType, createRequest, files);
        URI location = URI.create("/getDetailBoard/" + newBoardId);
        return ResponseEntity.created(location).body(RestResponse.success("게시글 등록이 완료되었습니다."));
    }

    @Operation(
            summary = "특정 카테고리 내 게시글 상세 조회",
            description = "categoryType 내 boarId와 일치하는 게시글의 상세한 내용을 조회."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 상세 조회", content = @Content(schema = @Schema(implementation = BoardResponse.class))),
            @ApiResponse(responseCode = "404", description = "미등록 카테고리", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "미등록 게시글", content = @Content(schema = @Schema(implementation = RestResponse.class))),
    })
    // 게시글 상세 조회
    @GetMapping("/{boardId}")
    public ResponseEntity<RestResponse<BoardResponse>> getDetailBoard(
            @PathVariable(required = true) Integer boardId,
            // 로그인 유저는 username -> email로, 비로그인 유저는 anonymousUser -> null로
            @AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : username") String email,
            HttpServletRequest request
    ){
        // IP 뽑아오기
        String clientIp = request.getRemoteAddr();

        BoardResponse boardResponse = boardService.getDetailBoard(boardId, email, clientIp);
        return ResponseEntity.ok(RestResponse.success(boardResponse));
    }

    @Operation(
            summary = "특정 카테고리 게시글 목록 조회",
            description = "categoryType 작성되어있는 게시글들을 목록을 형태로 조회."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시판 목록 조회", content = @Content(schema = @Schema(implementation = BoardResponse.class))),
            @ApiResponse(responseCode = "404", description = "미등록 카테고리", content = @Content(schema = @Schema(implementation = RestResponse.class))),
    })

    // 게시판 카테고리별 목록 조회
    @GetMapping("/category/{categoryType}")
    public ResponseEntity<RestResponse<?>> getBoards(
            @PathVariable(required = true) CategoryType categoryType,
            @RequestParam(required = false, defaultValue = "0")
            Integer page
    ){
        // subCategory와 일치하는 게시글 목록 조회
        Page<BoardResponse> boardResponses = boardService.getBoardsByCategory(categoryType, page);
        return ResponseEntity.ok(RestResponse.success(boardResponses));
        }

    @Operation(
            summary = "로그인한 유저가 작성한 게시글 삭제",
            description = "로그인한 유저가 categoryType 내 작성한 게시글에 대하여 삭제 가능."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시판 삭제", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "403", description = "작성자 불일치", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "미등록 카테고리, 게시글", content = @Content(schema = @Schema(implementation = RestResponse.class))),
    })
    // 게시글 삭제
    @DeleteMapping("/{boardId}")
    public ResponseEntity<RestResponse<?>> deleteBoard(
            @PathVariable(required = true) Integer boardId
    ){
        boardService.deleteBoard(boardId);
        return ResponseEntity.ok(RestResponse.success());
    }

    @Operation(
            summary = "로그인한 유저가 작성한 게시글 수정",
            description = "로그인한 유저가 categoryType 내 작성한 게시글에 대하여 수정 가능."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시판 수정", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "403", description = "작성자 불일치", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "미등록 카테고리, 게시글", content = @Content(schema = @Schema(implementation = RestResponse.class))),
    })
    // 게시글 수정
    @PatchMapping("/{boardId}")
    public ResponseEntity<?> updateBoard(
            @PathVariable Integer boardId,
            @RequestPart("data") BoardCreateRequest updateRequest,
            @RequestPart(value = "files", required = false) List<MultipartFile> newFiles,
            @RequestParam(value = "deleteIds", required = false) List<Integer> deleteAttachmentIds,
            @RequestParam(value = "imageOrder", required = false) List<String> imageOrder) throws IOException {

        boardService.updateBoard(boardId, updateRequest, newFiles, deleteAttachmentIds, imageOrder);
        return ResponseEntity.ok("게시글 수정 완료");
    }

    @Operation(
            summary = "로그인한 유저가 게시글 좋아요",
            description = "로그인한 유저가 게시글에 좋아요와 좋아요 취소."
    )

    // 게시글 좋아요
    @PostMapping("/{boardId}/like")
    public ResponseEntity<RestResponse<BoardResponse>> likeBoard(@PathVariable Integer boardId){
        BoardResponse boardResponse = boardService.likeBoard(boardId);
        return ResponseEntity.ok(RestResponse.success(boardResponse));
    }

    /**
     * 월간 인기 게시글 조회 (메인페이지)
     */
    @Operation(
            summary = "메인 페이지 카테고리 별 월간 인기 게시글 TOP 8 조회",
            description = "요청된 카테고리를 대상으로 이번 달의 조회수(1점)와 추천수(3점)를 합산하여 상위 8개를 반환" +
                    " 이번 달 데이터가 8개 미만일 경우, 지난달 데이터를 보충하여 순서대로 출력"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = BoardResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (카테고리 타입 오류 등)",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            ),
            @ApiResponse(responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            )
    })
    @GetMapping("/hot/main/{categoryType}")
    public ResponseEntity<RestResponse<List<BoardResponse>>> mainHotBoard(
            @PathVariable(required = true) CategoryType categoryType
    ) {
        List<BoardResponse> response = rankingService.monthCategoryHotBoard(categoryType);
        return ResponseEntity.ok(RestResponse.success(response));
    }

    /**
     * 인기 게시글 조회 (각 카테고리별)
     */
    @Operation(
            summary = "카테고리별 인기 게시글 조회 (주간)",
            description = "특정 카테고리의 이번 주 TOP 랭킹을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (카테고리 타입 오류 등)",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            )
    })
    @GetMapping("/hot/{categoryType}")
    public ResponseEntity<RestResponse<List<BoardResponse>>> getBoards(
            @PathVariable(required = true) CategoryType categoryType
    ){
        List<BoardResponse> response = rankingService.weekCategoryHotBoard(categoryType);
        return ResponseEntity.ok(RestResponse.success(response));
    }


    @Operation(
            summary = "게시글 검색",
            description = "제목 또는 닉네임으로 게시글 검색"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "", content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })

    // 검색기능
    @GetMapping("/search")
    public ResponseEntity<RestResponse<?>> getBoardSearch(
            @ModelAttribute BoardSearchRequest boardSearchRequest,
            @RequestParam(defaultValue = "0") Integer page) {
        Page<BoardResponse> boardResponses = boardService.getBoardSearch(boardSearchRequest, page);
        return ResponseEntity.ok(RestResponse.success(boardResponses));
    }
}
