package dopamine.soundock.controller;

import dopamine.soundock.dto.ApiResponse;
import dopamine.soundock.dto.UserSignupRequest;
import dopamine.soundock.dto.ValidateEmailRequest;
import dopamine.soundock.sevice.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Validated
public class AuthController {
    private final AuthService authService;

    // 이메일 중복 체크
    @GetMapping("/email")
    public ResponseEntity<ApiResponse<Void>> checkEmail(@Valid @ModelAttribute ValidateEmailRequest validateEmailRequest) {
        authService.validateEmail(validateEmailRequest);

        // 이메일 중복 체크 통과시 로직 실행
        return ResponseEntity.ok(ApiResponse.success("사용 가능한 이메일 입니다."));
    }

    // 회원 가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody UserSignupRequest userSignupRequest) {
        authService.signupUser(userSignupRequest);
        // 유효성 검사 통과시 로직 실행
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다."));
    }
}
