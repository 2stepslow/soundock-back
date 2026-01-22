package dopamine.soundock.controller;

import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.dto.response.YouTubePlaylistResponse;
import dopamine.soundock.service.YouTubeAuthService;
import dopamine.soundock.service.YouTubeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "playlist", description = "플레이리스트 기능 관련 API")
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/mypage")
public class YoutubeController {
    private final YouTubeService youTubeService;
    private final YouTubeAuthService youTubeAuthService;

    // 플레이리스트
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
    @GetMapping("/playlists/me")
    public ResponseEntity<RestResponse<List<YouTubePlaylistResponse>>> getMyPlaylists(@AuthenticationPrincipal(expression = "username") String email) {
        // @AuthenticationPrincipal을 통해 현재 로그인한 유저의 이메일(Subject)을 바로 받음
        List<YouTubePlaylistResponse> playlists = youTubeService.getUserYouTubePlaylists(email);
        return ResponseEntity.ok(RestResponse.success("플레이리스트 조회가 완료 되었습니다.", playlists));
    }
}
