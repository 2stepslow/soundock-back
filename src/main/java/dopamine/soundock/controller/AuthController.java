package dopamine.soundock.controller;

import dopamine.soundock.dto.*;
import dopamine.soundock.dto.request.*;
import dopamine.soundock.dto.response.LoginResponse;
import dopamine.soundock.dto.response.RefreshResponse;
import dopamine.soundock.dto.response.ValidateEmailResponse;
import dopamine.soundock.exceptions.CustomException;
import dopamine.soundock.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@Tag(name = "Auth", description = "유저 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Validated
@Slf4j
public class AuthController {
    private final AuthService authService;

    // 이메일 중복 체크
    @Operation(
            summary = "이메일 중복 및 재가입 가능 여부 체크",
            description = "사용자가 입력한 이메일의 가입 여부와 탈퇴 후 30일 경과 여부를 확인"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "체크 성공 (응답 body의 available 필드로 가용 여부 판단"),
            @ApiResponse(responseCode = "400", description = "이메일 형식 오류 또는 필수값 누락")
    })
    @GetMapping("/email")
    public ResponseEntity<RestResponse<ValidateEmailResponse>> checkEmail(@Valid @ModelAttribute ValidateEmailRequest validateEmailRequest) {
        ValidateEmailResponse response = authService.validateEmail(validateEmailRequest);
        return ResponseEntity.ok(RestResponse.success(response));
    }

    // 닉네임 중복 체크
    @Operation(
            summary = "닉네임 중복 체크",
            description = "커뮤니티 활동에 사용할 닉네임이 이미 사용중인지 확인"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용 가능한 닉네임"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 닉네임 형식"),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 닉네임")
    })
    @GetMapping("/nickname")
    public ResponseEntity<RestResponse<Void>> checkNickname(@Valid @ModelAttribute ValidateNicknameRequest validateNicknameRequest) {
        authService.validateNickname(validateNicknameRequest);

        // 닉네임 중복 체크 통과시 로직 실행
        return ResponseEntity.ok(RestResponse.success("사용 가능한 닉네임 입니다."));
    }

    // 회원 가입
    @Operation(
            summary = "회원 가입 및 인증 메일 발송",
            description = "새로원 회원을 등록하고, 본인 확인을 위한 인증 링크를 이메일로 발송"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 요청 성공 및 인증 이메일 전송"),
            @ApiResponse(responseCode = "400", description = "입력 데이터 유효성 검사 실패"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일 or 닉네임")
    })
    @PostMapping("/signup")
    public ResponseEntity<RestResponse<Void>> register(
            @Valid @RequestBody UserSignupRequest userSignupRequest
    ) {

        // ----------테스트 단계에서는 현재 주소를 자동으로 추적하는 이 코드를 사용하지만 배포환경에서는 변경이 필요함-------------------
        String siteURL = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        // ------------------------------------------------------------------------------------------------------------
        // 유효성 검사 통과시 로직 실행
        authService.signupUser(userSignupRequest, siteURL);
        return ResponseEntity.ok(RestResponse.success("인증 이메일 전송이 완료되었습니다. 이메일을 확인해주세요."));
    }

    // 이메일 전송 (재전송시 사용)
    @Operation(
            summary = "인증 이메일 재전송",
            description = "인증 메일을 받지 못했거나 만료된 경우, 해당 이메일로 인증 링크를 다시 발송"
    )
    @PostMapping("/verification")
    public ResponseEntity<RestResponse<Void>> verification(
            @Valid @RequestBody VerificationEmailRequest verificationEmailRequest) {
        // ----------테스트 단계에서는 현재 주소를 자동으로 추적하는 이 코드를 사용하지만 배포환경에서는 변경이 필요함-------------------
        String siteURL = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        // ------------------------------------------------------------------------------------------------------------
        authService.sendVerificationEmail(verificationEmailRequest, siteURL);
        return ResponseEntity.ok(RestResponse.success("이메일 인증 전송이 완료되었습니다."));
    }


    /**
     * 이메일 인증
     */
    @Operation(
            summary = "이메일 인증 처리",
            description = "사용자 이메일로 발송된 인증 토큰을 검증하고, 성공 또는 실패 결과 메시지가 담긴 HTML 페이지를 반환"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "인증 결과 페이지(HTML) 반환 성공"
            )
    })
    @GetMapping("/verify")
    public ModelAndView verifyUser(@RequestParam("token") String token) {
        ModelAndView mav = new ModelAndView("verification-result");
        try {
            authService.verifyUser(token);
            mav.addObject("success", true);
            mav.addObject("message", "이메일 인증이 완료되었습니다. 원래 페이지로 돌아가 가입을 마무리 해주세요!");
        } catch (CustomException e) {
            mav.addObject("success", false);
            mav.addObject("message", e.getMessage());
        } catch (Exception e) {
            log.error("인증 처리 중 서버 에러 발생: ", e);
            mav.addObject("success", false);
            mav.addObject("message", "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }

        return mav;
    }

    // 유저 로그인
    @Operation(
            summary = "사용자 로그인",
            description = "이메일과 비밀번호를 사용하여 인증을 진행하고, 성공 시 JWT 토큰(Access/Refresh)을 발급 "
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공 및 토큰 발급 완료",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인 실패 (이메일 또는 비밀번호 불일치"),
            @ApiResponse(responseCode = "403", description = "로그인 거부 (이메일 인증 미완료")
    })
    @PostMapping("/login")
    public ResponseEntity<RestResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(RestResponse.success("로그인에 성공 했습니다.", response));
    }

    // 유저 로그아웃
    @Operation(
            summary = "사용자 로그아웃",
            description = "전달된 Access Token을 무효화하고 세션을 종료합니다. 이후 해당 토큰으로는 API 접근이 불가능합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패(유효하지 않은 토큰 or 이미 만료된 토큰)"),
            @ApiResponse(responseCode = "404", description = "토큰에서 추출된 사용자가 검색되지 않는 경우")
    })
    @PostMapping("/logout")
    public ResponseEntity<RestResponse<Void>> logout(@Valid @RequestBody LogoutRequest logoutRequest) {
        authService.logout(logoutRequest);
        return ResponseEntity.ok(RestResponse.success("로그아웃을 완료 했습니다."));
    }

    // 리프레시 토큰
    @Operation(
            summary = "토큰 만료시 재발급",
            description = "만료 기간이 짧은 Access Token 만료 시 Refresh Token을 확인하고 Access Token 재발급"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "재발급 성공", content = @Content(schema = @Schema(implementation = RefreshResponse.class))),
            @ApiResponse(responseCode = "401", description = "재발급 실패: 리프레시 토큰이 유효하지 않거나 만료됨 (로그인 페이지로 이동 필요)", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청: 토큰 값이 누락", content = @Content(schema = @Schema(implementation = RestResponse.class))
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<RestResponse<RefreshResponse>> refresh(@Valid @RequestBody RefreshRequest refreshRequest) {
        RefreshResponse response = authService.refresh(refreshRequest);
        return ResponseEntity.ok(RestResponse.success(response));
    }

    /**
     * 이메일 인증 상태 확인
     */
    @Operation(
            summary = "이메일 인증 상태 확인",
            description = "리액트 가입 대기 화면에서 유저의 상태가 ACTIVE(인증 완료)로 변했는지 확인하기 위해 호출"
    )
    @GetMapping("/verify/status")
    public ResponseEntity<RestResponse<String>> checkStatus(@RequestParam("email") String email) {
        String status = authService.getUserStatus(email);

        return ResponseEntity.ok(RestResponse.success(status));
    }
}
