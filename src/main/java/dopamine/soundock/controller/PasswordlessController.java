package dopamine.soundock.controller;

import dopamine.soundock.dto.PasswordlessApiResponse;
import dopamine.soundock.dto.request.PWLCancelRequest;
import dopamine.soundock.dto.request.PWLLoginTriggerRequest;
import dopamine.soundock.dto.request.PWLRegisterRequest;
import dopamine.soundock.dto.request.PWLResultRequest;
import dopamine.soundock.dto.response.PWLRegisterResponse;
import dopamine.soundock.dto.response.PWLResultResponse;
import dopamine.soundock.dto.response.PWLStatusResponse;
import dopamine.soundock.dto.response.PWLTriggerResponse;
import dopamine.soundock.global.constants.AppConstants;
import dopamine.soundock.service.PasswordlessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@RequestMapping("/api/passwordless")
@Tag(name = "패스워드리스", description = "패스워드리스 인증 관련 API")
public class PasswordlessController {

    private final PasswordlessService passwordlessService;

    @Value("${app.cookie.domain}")
    private String cookieDomain;

    /**
     * 로그인한 사용자 패스워드리스 가입 확인
     */
    @Operation(
            summary = "로그인한 사용자의 패스워드리스 가입 여부 확인",
            description = "현재 로그인한 사용자의 이메일을 기반으로 패스워드리스 서비스에 등록되어 있는지 확인합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PasswordlessApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 이메일을 가진 유저가 시스템에 존재하지 않음",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"존재하지 않는 유저입니다.\"}"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "패스워드리스 서버 통신 실패 또는 데이터 누락",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"패스워드리스 가입 상태 조회에 실패했습니다.\"}"))
            ),
            @ApiResponse(responseCode = "500", description = "서빙 API 통신 실패 또는 서버 내부 오류")
    })
    @GetMapping("/status")
    public ResponseEntity<PasswordlessApiResponse<PWLStatusResponse>> getUserStatus(
            @AuthenticationPrincipal(expression = "username") String email
    ) {
        PasswordlessApiResponse<PWLStatusResponse> response = passwordlessService.updateUserPWL(email);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자의 패스워드리스 등록
     */
    @Operation(
            summary = "로그인한 유저의 패스워드리스 등록",
            description = "사용자의 모바일 앱으로 QR 코드를 통해 패스워드리스 등록"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "등록 데이터 생성 성공 (QR 코드 포함)",
                    content = @Content(schema = @Schema(implementation = PWLRegisterResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미 패스워드리스 서비스를 이용 중인 사용자",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"이미 패스워드리스 서비스를 사용 중입니다.\"}"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "가입되지 않은 이메일 주소",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"존재하지 않는 유저입니다.\"}"))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "인증 서버(FIDO 등)와의 통신 오류",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"서빙 API 통신 실패\"}"))
            )
    })
    @PostMapping("/register")
    public  ResponseEntity<PasswordlessApiResponse<PWLRegisterResponse>> userRegisterPWL(
            @Valid @RequestBody PWLRegisterRequest request
    ) {
        PasswordlessApiResponse<PWLRegisterResponse> response = passwordlessService.registerUserPWL(request.getEmail());
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
            @ApiResponse(
                    responseCode = "200",
                    description = "푸시 알림 전송 성공 및 인증 세션 생성 완료",
                    content = @Content(schema = @Schema(implementation = PWLTriggerResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "패스워드리스 미등록 사용자 (먼저 가입 절차 필요)",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"패스워드리스가 등록 되어있지 않습니다.\"}"))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "비활성화된 계정 (정지 또는 휴면)",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"사용할 수 없는 아이디 입니다. 관리자에게 문의해주세요.\"}"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 회원 이메일",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"존재하지 않는 유저입니다.\"}"))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "인증 서버 통신 실패 또는 푸시 발송 시스템 오류",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"서빙 API 통신 실패\"}"))
            )
    })
    @PostMapping("/login-trigger")
    public ResponseEntity<PasswordlessApiResponse<PWLTriggerResponse>> userLoginTrigger(
            @Valid @RequestBody PWLLoginTriggerRequest pwlRequest,
            HttpServletRequest request
    ) {
        // 사용자의 IP 추출 (로드밸런서 or 프록시 환경을 대비한 application.properties 설정)
        String clientIp = request.getRemoteAddr();

        String email = pwlRequest.getEmail();

        PasswordlessApiResponse<PWLTriggerResponse> response = passwordlessService.triggerLogin(email, clientIp);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그인 인증 결과 확인 및 최종 로그인
     */
    @Operation(
            summary = "패스워드리스 인증 결과 확인 및 JWT 발급",
            description = "사용자가 앱에서 승인하면 우리 서비스의 Access/Refresh Token(쿠키)을 발급합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "인증 성공 및 토큰 발급",
                    headers = @Header(name = "Set-Cookie", description = "refreshToken을 포함한 쿠키 (HttpOnly, Secure)"),
                    content = @Content(schema = @Schema(implementation = PWLResultResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "인증 취소됨, 세션 만료, 또는 잘못된 요청",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"패스워드리스 인증이 취소되었습니다.\"}"))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "비활성화된 사용자 계정",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"사용할 수 없는 아이디 입니다.\"}"))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "인증 서버 통신 실패 또는 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"서빙 API 통신 실패\"}"))
            )
    })
    @PostMapping("/result")
    public ResponseEntity<PasswordlessApiResponse<PWLResultResponse>> getLoginResult(
            @Valid @RequestBody PWLResultRequest request
            ) {
        PasswordlessApiResponse<PWLResultResponse> response = passwordlessService.finalLoginResult(request.getUserId(), request.getSessionId());

        // 인증이 완료된 상태(Y)인 경우에만 쿠키 생성
        if (response.getData() != null && "Y".equals(response.getData().getAuth())) {
            String refreshToken = response.getData().getRefreshToken(); // 서비스에서 임시로 담아준 토큰 추출

            ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(AppConstants.Time.REFRESH_TOKEN_VALIDITY_MS / 1000)
                    .sameSite("none");

            if (cookieDomain != null && !cookieDomain.isBlank() && !cookieDomain.equals("localhost")) {
                cookieBuilder.domain(cookieDomain);
            }

            ResponseCookie cookie = cookieBuilder.build();

            // 응답 바디에서 refreshToken 제거
            response.getData().setRefreshToken(null);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 진행 중인 인증 요청 취소
     */
    @Operation(
            summary = "로그인 인증 취소",
            description = "사용자가 웹에서 취소 버튼을 누를 경우, 진행 중인 푸시 인증 세션을 중단합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "인증 세션 취소 성공 (더 이상 결과 확인 API를 호출할 필요 없음)",
                    content = @Content(schema = @Schema(implementation = PasswordlessApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이미 만료된 세션이거나 필수 파라미터 누락)",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"유효하지 않은 인증 세션입니다.\"}"))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "인증 서버 통신 실패",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"서빙 API 통신 실패\"}"))
            )
    })
    @PostMapping("/cancel")
    public ResponseEntity<PasswordlessApiResponse<Void>> cancelLogin(
            @Valid @RequestBody PWLCancelRequest request
    ) {
        PasswordlessApiResponse<Void> response = passwordlessService.cancelAuthentication(request.getEmail(), request.getSessionId());
        return ResponseEntity.ok(response);
    }

    /**
     * 패스워드리스 사용자 탈퇴
     */
    @Operation(
            summary = "회원 탈퇴",
            description = "패스워드리스 인증 기반 사용자의 탈퇴를 처리"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "패스워드리스 탈퇴 성공 (해당 계정의 패스워드리스 기능 비활성화)",
                    content = @Content(schema = @Schema(implementation = PasswordlessApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "패스워드리스에 가입되어 있지 않은 사용자",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"패스워드리스가 등록 되어있지 않습니다.\"}"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 토큰 누락 또는 만료된 세션",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"Full authentication is required to access this resource\"}"))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "인증 서버 통신 실패 또는 데이터베이스 처리 오류",
                    content = @Content(examples = @ExampleObject(value = "{\"message\": \"서빙 API 통신 실패\"}"))
            )
    })
    @PostMapping("/withdrawal")
    public ResponseEntity<PasswordlessApiResponse<Void>> userWithdrawal(
            @AuthenticationPrincipal(expression = "username") String email
    ) {
        PasswordlessApiResponse<Void> response = passwordlessService.userWithdrawal(email);
        return ResponseEntity.ok(response);
    }
}
