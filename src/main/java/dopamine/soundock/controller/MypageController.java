package dopamine.soundock.controller;

import dopamine.soundock.dto.*;
import dopamine.soundock.exceptions.CustomException;
import dopamine.soundock.service.MypageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Mypage", description = "마이페이지 기능 관련 API")
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/mypage")
public class MypageController {
    private final MypageService mypageService;

    // 유저 정보 수정
    @Operation(
            summary = "회원 정보 수정(닉네임, 연락처)",
            description = "현재 로그인한 사용자의 닉네임 또는 연락처를 수정. 수정하고 싶은 항목만 선택적으로 가능."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 정보 수정 성공", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (표현식 위반 또는 빈 값 전송), 변경된 사항 없음", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "없는 유저", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "409", description = "중복 오류 (이미 존재하는 닉네임)", content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    @PatchMapping("/me")
    public ResponseEntity<RestResponse<Void>> updateUserInfo(@Valid @RequestBody UpdateInfoRequest request) {
        mypageService.updateUserInfo(request);
        return ResponseEntity.ok(RestResponse.success("회원 정보 수정이 완료되었습니다."));
    }

    // 비밀번호 수정
    @Operation(
            summary = "비밀번호 수정",
            description = "현재 로그인한 사용자의 비밀번호 수정"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 수정 성공", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (표현식 위반 또는 빈 값 전송), 변경된 사항 없음", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "없는 유저", content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    @PatchMapping("/mepasswd")
    public ResponseEntity<RestResponse<Void>> updateUserPasswd(@Valid @RequestBody UpdatePasswdRequest request) {
        mypageService.updateUserPasswd(request);
        return ResponseEntity.ok(RestResponse.success("비밀번호 수정이 완료되었습니다."));
    }

    // 현재 비밀번호 검증
    @Operation(summary = "비밀번호 확인", description = "수정 페이지 진입 전 현재 비밀번호 일치 여부 검증")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "현재 비밀번호 확인 성공", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "일치하지 않는 비밀번호", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "없는 유저", content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    @PostMapping("/password/verify")
    public ResponseEntity<RestResponse<Void>> checkCurrentPassword(@Valid @RequestBody CurrentPasswdRequest request) {
        mypageService.checkCurrentPassword(request);
        return ResponseEntity.ok(RestResponse.success("비밀번호 확인에 성공했습니다."));
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<RestResponse<Void>> deleteUser(@Valid @RequestBody DeleteUserRequest request) {
        mypageService.deleteUser(request);
        return ResponseEntity.ok(RestResponse.success("회원 탈퇴가 완료되었습니다."));
    }
}
