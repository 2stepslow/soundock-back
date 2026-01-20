package dopamine.soundock.controller;

import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.dto.UpdateInfoRequest;
import dopamine.soundock.service.MypageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "Mypage", description = "마이페이지 기능 관련 API")
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/mypage")
public class MypageController {
    private final MypageService mypageService;

    // 유저 정보 수정
    @Operation(
            summary = "회원 정보 수정",
            description = "현재 로그인한 사용자의 닉네임 또는 연락처를 수정. 수정하고 싶은 항목만 선택적으로 가능."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 정보 수정 성공", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (표현식 위반 또는 빈 값 전송), 변경된 사항 없음", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "409", description = "중복 오류 (이미 존재하는 닉네임)", content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    @PatchMapping("/me")
    public ResponseEntity<RestResponse<Void>> updateUser(@Valid @RequestBody UpdateInfoRequest request) {
        mypageService.updateUser(request);
        return ResponseEntity.ok(RestResponse.success("회원 정보 수정이 완료되었습니다."));
    }
}
