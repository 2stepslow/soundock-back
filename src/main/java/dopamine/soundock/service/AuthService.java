package dopamine.soundock.service;

import dopamine.soundock.dto.request.*;
import dopamine.soundock.dto.response.EmailCheckResult;
import dopamine.soundock.dto.response.LoginResponse;
import dopamine.soundock.dto.response.RefreshResponse;
import dopamine.soundock.dto.response.ValidateEmailResponse;
import dopamine.soundock.entity.AccessTokenBlacklist;
import dopamine.soundock.entity.RefreshToken;
import dopamine.soundock.entity.User;
import dopamine.soundock.entity.VerificationToken;
import dopamine.soundock.enums.UserRole;
import dopamine.soundock.enums.UserStatus;
import dopamine.soundock.exceptions.*;
import dopamine.soundock.global.TokenProvider;
import dopamine.soundock.global.constants.AppConstants;
import dopamine.soundock.repository.AccessTokenBlacklistRepository;
import dopamine.soundock.repository.RefreshTokenRepository;
import dopamine.soundock.repository.UserRepository;
import dopamine.soundock.repository.VerificationTokenRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final VerificationTokenRepository  verificationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;
    private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;

    /**
     * 이메일 중복체크 메서드
     */
    private EmailCheckResult checkEmailAvailability(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return EmailCheckResult.available();
        }

        User user = userOpt.get();

        if (!user.isDeleted()) {
            return EmailCheckResult.unavailable("이미 사용 중인 이메일입니다.");
        }

        LocalDateTime limitDate = LocalDateTime.now().minusDays(AppConstants.Time.EMAIL_REACTIVATION_COOLDOWN_DAYS);
        if (user.getDeletedAt() != null && user.getDeletedAt().isAfter(limitDate)) {
            return EmailCheckResult.unavailable(AppConstants.ErrorMessage.EMAIL_REACTIVATION_ERROR);
        }

        return EmailCheckResult.availableWithCleanup();
    }

    private boolean validateEmailForSignup(String email) {
        EmailCheckResult result = checkEmailAvailability(email);

        if (!result.isAvailable()) {
            throw new CustomException(result.getMessage(), HttpStatus.CONFLICT);
        }

        return result.isNeedsCleanup();
    }

    @Transactional(readOnly = true)
    public ValidateEmailResponse validateEmail(ValidateEmailRequest request) {
        EmailCheckResult result = checkEmailAvailability(request.getEmail());
        return new ValidateEmailResponse(result.isAvailable(), result.getMessage());
    }

    /**
     * 닉네임 중복 확인
     */
    @Transactional(readOnly = true)
    public void validateNickname(ValidateNicknameRequest validateNicknameRequest) {
        // 중복 일 경우 예외 발생
        if (userRepository.existsByNickname(validateNicknameRequest.getNickname())) {
            throw new DuplicateNicknameException();
        }
    }

    /**
     * 회원가입
     */
    @Transactional
    public void signupUser(UserSignupRequest userSignupRequest, String siteURL) {
        // 이메일 상태 체크 및 기존 데이터 정리
        boolean needsCleanup = validateEmailForSignup(userSignupRequest.getEmail());
        if (needsCleanup) {
            userRepository.deleteByEmail(userSignupRequest.getEmail());
            userRepository.flush();
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(userSignupRequest.getNickname())) {
            throw new DuplicateNicknameException();
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userSignupRequest.getPassword());

        // builder 패턴을 통해 좀 더 깔끔하게 수정
        User user = User
                .builder()
                .email(userSignupRequest.getEmail())
                .password(encodedPassword)
                .nickname(userSignupRequest.getNickname())
                .phoneNumber(userSignupRequest.getPhoneNumber())
                .role(UserRole.USER)
                .status(UserStatus.PENDING)
                .build();

        userRepository.save(user);

        VerificationEmailRequest emailRequest = new VerificationEmailRequest(user.getEmail());
        sendVerificationEmail(emailRequest, siteURL);
    }

    /**
     * 이메일 인증 토큰 생성
     */
    private void createVerificationToken(User user, String token) {
        // 기존 토큰이 있으면 삭제
        verificationTokenRepository.deleteByUser(user);

        VerificationToken verificationToken = VerificationToken
                .builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plusMinutes(AppConstants.Time.VERIFICATION_TOKEN_EXPIRY_MINUTES))
                .build();
        verificationTokenRepository.save(verificationToken);
    }

    /**
     * 이메일 전송
     */
    @Transactional
    public void sendVerificationEmail(VerificationEmailRequest verificationEmailRequest, String siteURL) {
        try {
            User user = userRepository.findByEmail(verificationEmailRequest.getEmail())
                    .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND));

            String token = UUID.randomUUID().toString();
            createVerificationToken(user, token);

            String recipientAddress = verificationEmailRequest.getEmail();
            String subject = "이메일 인증 요청";
            String verificationUrl = siteURL + "/api/auth/verify?token=" + token;

            Context context = new Context();
            context.setVariable("verificationUrl", verificationUrl);
            String htmlContent = templateEngine.process("verification-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(recipientAddress);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("이메일 발송 실패 - 수신자: {}, 원인: {}"
                    , verificationEmailRequest.getEmail(), e.getMessage(), e);
            throw new MailSendingException();
        }
    }

    /**
     * 이메일 원클릭 인증
     */
    @Transactional
    public void verifyUser(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new CustomException("유효하지 않은 토큰입니다.", HttpStatus.BAD_REQUEST));

        if (verificationToken.isVerified()) {
            throw new CustomException("이미 인증이 완료된 링크입니다.", HttpStatus.CONFLICT);
        }

        // 만료 시간 null 체크
        LocalDateTime expiryDate = verificationToken.getExpiryDate();
        if (expiryDate == null || expiryDate.isBefore(LocalDateTime.now())) {
            throw new CustomException("인증 시간이 만료되었습니다. 다시 시도해주세요.", HttpStatus.GONE);
        }

        User user = verificationToken.getUser();
        if (user != null) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        }
        verificationToken.setVerified(true);
        verificationTokenRepository.save(verificationToken);
    }

    /**
     * 로그인
     */
    @Transactional
    public LoginResponse login(
            LoginRequest loginRequest
    ) {
        // 1. 존재 여부 확인 (탈퇴 시에도 Exception 발생)
        User user = userRepository.findByEmailAndIsDeletedFalse(loginRequest.getEmail())
            .orElseThrow(() -> new LoginFailedException("가입되지 않은 계정입니다."));

        // 2. 비밀번호 검증
        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new LoginFailedException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 상태 검증
        if(!user.getStatus().equals(UserStatus.ACTIVE)) {
            throw new CustomException("이메일 인증이 완료되지 않았습니다. 메일을 확인해주세요.", HttpStatus.FORBIDDEN);
        }

        // 로그인 성공 -> 토큰 발급
        String accessToken = tokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        // Refresh Token 을 DB에 추가
        RefreshToken refresh = RefreshToken
                .builder()
                .token(refreshToken)
                .user(user)
                .expirationAt(LocalDateTime.now().plusSeconds(AppConstants.Time.REFRESH_TOKEN_VALIDITY_MS/1000))
                .build();

        refreshTokenRepository.save(refresh);

        return new LoginResponse(accessToken, refreshToken);
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(LogoutRequest logoutRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("추출된 인증 정보 : {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        refreshTokenRepository.deleteByUserId(user.getId());

        String accessToken = logoutRequest.getAccessToken();
        Date expDate = tokenProvider.getExpiration(accessToken);
        LocalDateTime convertedDate = expDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        AccessTokenBlacklist accessTokenBlacklist = AccessTokenBlacklist
                .builder()
                .accessToken(accessToken)
                .expirationAt(convertedDate)
                .build();

        accessTokenBlacklistRepository.save(accessTokenBlacklist);
    }

    /**
     * Access Token 재발급
     */
    @Transactional
    public RefreshResponse refresh(RefreshRequest refreshRequest) {
        String userRefreshToken = refreshRequest.getRefreshToken();
        // 1. 위/변조 여부 검증
        if (!tokenProvider.validateToken(userRefreshToken)) {
            // 검증 실패 예외
            throw new InvalidTokenException("위조된 토큰 입니다.");
        }

        // 2. 우리 서버에 존재하는 refresh token 인지 검증
        if(!refreshTokenRepository.existsByToken(userRefreshToken)){
            throw new InvalidTokenException("올바르지 않은 토큰입니다.");
        }

        // 3. 만료시간 확인
        Date expiration = tokenProvider.getExpiration(userRefreshToken);
        if(expiration.before(new Date())) {
            // 만료기간 지난 예외
            throw new InvalidTokenException("더 이상 사용할 수 없는 토큰입니다.");
        }

        // 4. 만료 안됐으면 새로운 access token을 만들어서 반환
        String username = tokenProvider.getEmailFromToken(userRefreshToken);
        String accessToken = tokenProvider.generateAccessToken(username);

        return new RefreshResponse(accessToken);
    }

    /**
     * 이메일 인증 완료 확인 메서드
     */
    @Transactional(readOnly = true)
    public String getUserStatus(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자 입니다."));

        return user.getStatus().name();
    }
}
