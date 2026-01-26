package dopamine.soundock.controller;

import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.dto.request.PlaylistRegisterRequest;
import dopamine.soundock.dto.response.YouTubePlaylistResponse;
import dopamine.soundock.service.YouTubeAuthService;
import dopamine.soundock.service.YouTubeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "playlist", description = "플레이리스트 기능 관련 API")
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/mypage")
public class YoutubeController {
    private final YouTubeService youTubeService;
    private final YouTubeAuthService youTubeAuthService;

    // 플레이리스트 조회
    @Operation(
            summary = "유튜브 플레이리스트 조회",
            description = "현재 로그인한 사용자의 구글 계정에 연동된 유튜브 재생목록(유튜브 뮤직 포함) 목록을 가져옴."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "플레이리스트 조회 성공",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "유저 또는 구글 연동 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "500", description = "유튜브 API 호출 중 서버 오류 발생",
                    content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    @GetMapping("/playlist/youtube")
    public ResponseEntity<RestResponse<List<YouTubePlaylistResponse>>> getMyYoutubePlaylists(@AuthenticationPrincipal(expression = "username") String email) {
        // @AuthenticationPrincipal을 통해 현재 로그인한 유저의 이메일(Subject)을 바로 받음
        List<YouTubePlaylistResponse> playlists = youTubeService.getUserYouTubePlaylists(email);
        return ResponseEntity.ok(RestResponse.success("플레이리스트 조회가 완료 되었습니다.", playlists));
    }

    // 플레이리스트 등록
    @Operation(
            summary = "유튜브 플레이리스트 등록",
            description = "유튜브에서 조회한 플레이리스트 중 하나를 선택하여 우리 서비스의 DB에 저장"
    )
    @PostMapping("/playlist")
    public ResponseEntity<RestResponse<Void>> registerPlaylist(
            @AuthenticationPrincipal(expression = "username") String email,
            @RequestBody PlaylistRegisterRequest request
            ) {
        youTubeService.registerPlaylist(email, request);
        return ResponseEntity.ok(RestResponse.success("플레이리스트가 성공적으로 등록되었습니다."));
    }


    /** 내가 등록한 플레이리스트 전체조회 */
    @Operation(
            summary = "내가 등록한 플레이리스트 조회",
            description = "현재 로그인한 사용자가 서비스 내에 등록(저장)한 플레이리스트 목록을 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/playlist/me")
    public ResponseEntity<RestResponse<List<YouTubePlaylistResponse>>> getMyPlaylists(@AuthenticationPrincipal(expression = "username") String email) {
        List<YouTubePlaylistResponse> myplaylist = youTubeService.getMyRegisterPlaylist(email);
        return ResponseEntity.ok(RestResponse.success("플레이리스트 조회에 성공했습니다.", myplaylist));
    }


    /** 플레이리스트 삭제 */
    @Operation(
            summary = "등록된 플레이리스트 개별 삭제",
            description = "마이페이지 플레이리스트 목록에서 특정 플레이리스트를 삭제"
    )
    @DeleteMapping("/playlist/{playlistId}")
    public ResponseEntity<RestResponse<Void>> deletePlaylist(
            @Parameter(description = "삭제할 플레이리스트의 PK Id") @PathVariable Integer playlistId,
            @AuthenticationPrincipal(expression = "username") String email
    ) {
        youTubeService.deleteMyPlaylist(playlistId, email);
        return ResponseEntity.ok(RestResponse.success("플레이리스트가 목록에서 삭제되었습니다."));
    }
}
