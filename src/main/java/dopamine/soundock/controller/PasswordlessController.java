package dopamine.soundock.controller;

import dopamine.soundock.dto.PasswordlessApiResponse;
import dopamine.soundock.dto.response.PWLRegisterResponse;
import dopamine.soundock.dto.response.PWLStatusResponse;
import dopamine.soundock.service.PasswordlessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/passwordless")
@Tag(name = "패스워드리스", description = "패스워드리스 인증 관련 API")
public class PasswordlessController {

    private final PasswordlessService passwordlessService;

    /**
     * 로그인한 사용자 패스워드리스 가입 확인
     */
    @Operation(
            summary = "로그인한 사용자의 패스워드리스 가입 여부 확인",
            description = "현재 로그인한 사용자의 이메일을 기반으로 패스워드리스 서비스에 등록되어 있는지 확인합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)"),
            @ApiResponse(responseCode = "500", description = "서빙 API 통신 실패 또는 서버 내부 오류")
    })
    @GetMapping("/status")
    public ResponseEntity<PasswordlessApiResponse<PWLStatusResponse>> getUserStatus(
            @AuthenticationPrincipal(expression = "username") String email
    ) {
        PasswordlessApiResponse<PWLStatusResponse> response = passwordlessService.checkUserStatus(email);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그인한 사용자의 패스워드리스 등록
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "가입 등록 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (로그인 필요)"),
            @ApiResponse(responseCode = "500", description = "서빙 API 통신 실패 또는 서버 내부 오류")
    })
    @PostMapping("/register")
    public  ResponseEntity<PasswordlessApiResponse<PWLRegisterResponse>> userRegisterPWL(
            @AuthenticationPrincipal(expression = "username") String email
    ) {
        PasswordlessApiResponse<PWLRegisterResponse> response = passwordlessService.registerUserPWL(email);
        return ResponseEntity.ok(response);
    }
}
