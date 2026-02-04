package dopamine.soundock.controller;

import dopamine.soundock.dto.PasswordlessApiResponse;
import dopamine.soundock.dto.request.PWLLoginTriggerRequest;
import dopamine.soundock.dto.request.PWLResultRequest;
import dopamine.soundock.dto.response.PWLRegisterResponse;
import dopamine.soundock.dto.response.PWLResultResponse;
import dopamine.soundock.dto.response.PWLStatusResponse;
import dopamine.soundock.dto.response.PWLTriggerResponse;
import dopamine.soundock.global.IPUtils;
import dopamine.soundock.service.PasswordlessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    @Operation(
            summary = "로그인한 유저의 패스워드리스 등록",
            description = "사용자의 모바일 앱으로 QR 코드를 통해 패스워드리스 등록"
    )
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

    /**
     * 로그인 트리거 API
     */
    @Operation(
            summary = "로그인 인증 요청 (Trigger)",
            description = "사용자의 모바일 앱으로 로그인 승인 푸시 알림을 전송합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "푸시 알림 전송 성공"),
            @ApiResponse(responseCode = "500", description = "서빙 API 통신 오류")
    })
    @PostMapping("/login-trigger")
    public ResponseEntity<PasswordlessApiResponse<PWLTriggerResponse>> userLoginTrigger(
            @Valid @RequestBody PWLLoginTriggerRequest pwlRequest,
            HttpServletRequest request
    ) {
        // 사용자의 IP 추출 (로드밸런서 or 프록시 환경을 대비한 IP유틸리티 클래스 이용)
        String clientIp = IPUtils.getClientIp(request);

        String email = pwlRequest.getEmail();

        PasswordlessApiResponse<PWLTriggerResponse> response = passwordlessService.triggerLogin(email, clientIp);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그인 인증 결과 확인 및 최종 로그인
     */
    @Operation(
            summary = "패스워드리스 인증 결과 확인 및 JWT 발급",
            description = "사용자가 앱에서 승인하면 우리 서비스의 Access/Refresh Token을 발급합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결과 확인 완료"),
            @ApiResponse(responseCode = "500", description = "서빙 API 통신 오류")
    })
    @GetMapping("/result")
    public ResponseEntity<PasswordlessApiResponse<PWLResultResponse>> getLoginResult(
            @RequestParam("userId") String email,
            @RequestParam("sessionId") String sessionId
    ) {
        PasswordlessApiResponse<PWLResultResponse> result = passwordlessService.finalLoginResult(email, sessionId);
        return ResponseEntity.ok(result);
    }
}
