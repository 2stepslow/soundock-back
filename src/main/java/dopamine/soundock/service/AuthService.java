package dopamine.soundock.service;

import dopamine.soundock.dto.TokenDto;
import dopamine.soundock.dto.request.*;
import dopamine.soundock.dto.response.*;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import java.security.SecureRandom;
import java.time.Duration;
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
    private final EmailService emailService;
    private final VerificationTokenRepository  verificationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;
    private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;
    private final StringRedisTemplate redisTemplate;

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
    public void signupUser(UserSignupRequest userSignupRequest) {
        // 이메일 상태 체크 및 기존 데이터 정리
        boolean needsCleanup = validateEmailForSignup(userSignupRequest.getEmail());
        if (needsCleanup) {
            User existingUser = userRepository.findByEmail(userSignupRequest.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException(AppConstants.ErrorMessage.USER_NOT_FOUND));
            existingUser.setEmail(userSignupRequest.getEmail() + "_deleted_" + existingUser.getId() + "_" + System.currentTimeMillis());

            userRepository.saveAndFlush(existingUser);
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(userSignupRequest.getNickname())) {
            throw new DuplicateNicknameException();
        }

        // 이메일 인증 여부 최종 확인
        VerificationToken token = verificationTokenRepository.findByEmail(userSignupRequest.getEmail())
                .orElseThrow(() -> new CustomException("인증 정보가 없습니다",HttpStatus.BAD_REQUEST));

        if (!token.isVerified())
            throw new CustomException("이메일 인증이 완료되지 않았습니다.", HttpStatus.BAD_REQUEST);

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userSignupRequest.getPassword());

        // builder 패턴을 통해 좀 더 깔끔하게 수정
        User user = User
                .builder()
                .email(userSignupRequest.getEmail())
                .password(encodedPassword)
                .name(userSignupRequest.getName())
                .nickname(userSignupRequest.getNickname())
                .phoneNumber(userSignupRequest.getPhoneNumber())
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        verificationTokenRepository.deleteByEmail(userSignupRequest.getEmail());
    }

    /**
     * 이메일 인증 토큰 생성
     */
    private void createVerificationToken(String email, String token) {
        // 기존 토큰이 있으면 삭제
        verificationTokenRepository.deleteByEmail(email);

        VerificationToken verificationToken = VerificationToken
                .builder()
                .email(email)
                .token(token)
                .expiryDate(LocalDateTime.now().plusMinutes(AppConstants.Time.VERIFICATION_TOKEN_EXPIRY_MINUTES))
                .build();
        verificationTokenRepository.save(verificationToken);
    }

    /**
     * 이메일 전송 준비
     */
    @Transactional
    public void sendVerificationEmail(VerificationEmailRequest verificationEmailRequest, String siteURL) {
        String email = verificationEmailRequest.getEmail();

        // 기존 토큰 정보 확인
        Optional<VerificationToken> tokenOpt = verificationTokenRepository.findByEmail(email);

        if (tokenOpt.isPresent()) {
            VerificationToken oldToken = tokenOpt.get();

            // 쿨타임 체크 :1분 이내 재요청 시 차단
            if (oldToken.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1))) {
                throw new CustomException("1분 후에 다시 시도해주세요.", HttpStatus.TOO_MANY_REQUESTS);
            }
        }

        String token = UUID.randomUUID().toString();
        createVerificationToken(email, token);

        String subject = "이메일 인증 요청";
        String verificationUrl = siteURL + "/api/auth/verify?token=" + token;

        Context context = new Context();
        context.setVariable("verificationUrl", verificationUrl);

        // 앞의 부분은 전부 동기 지만 메일 보내는 메서드인 sendMailAsync만 비동기(여기를 끝으로 sendMailAsync 메서드는 따로 시작하고 sendVerificationEmail은 종료)
        emailService.sendMailAsync(email, subject, "verification-email", context);
    }

    /**
     * 이메일 인증 확인 메서드
     */
    @Transactional(readOnly = true)
    public VerificationStatusResponse checkEmailVerificationStatus(String email) {
        return verificationTokenRepository.findByEmail(email)
                .map(token -> {
                    boolean isExpired = token.getExpiryDate().isBefore(LocalDateTime.now());

                    return VerificationStatusResponse.builder()
                            .email(email)
                            .isVerified(token.isVerified())
                            .isExpired(isExpired)
                            .build();
                })
                .orElse(VerificationStatusResponse.builder()
                        .email(email)
                        .isVerified(false)
                        .isExpired(false)
                        .build());
    }

    /**
     * 로그인
     */
    @Transactional
    public TokenDto login(
            LoginRequest loginRequest
    ) {
        // 1. 존재 여부 확인 (탈퇴 시에도 Exception 발생)
        User user = userRepository.findByEmailAndIsDeletedFalse(loginRequest.getEmail())
            .orElseThrow(() -> new LoginFailedException("가입되지 않은 계정입니다."));

        if (user.isPasswordless()) {
            throw new LoginFailedException("패스워드리스 서비스를 사용 중입니다. 패스워드리스를 통해 로그인 해주세요");
        }

        // 2. 비밀번호 검증
        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new LoginFailedException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 상태 검증
        if(!user.getStatus().equals(UserStatus.ACTIVE)) {
            throw new CustomException("사용할 수 없는 아이디 입니다. 관리자에게 문의해주세요.", HttpStatus.FORBIDDEN);
        }

        // 로그인 성공 -> 토큰 발급
        String accessToken = tokenProvider.generateAccessToken(user.getEmail(), user.getId(), user.getRole().name());
        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        // Refresh Token 을 DB에 추가
        RefreshToken refresh = RefreshToken
                .builder()
                .token(refreshToken)
                .user(user)
                .expirationAt(LocalDateTime.now().plusSeconds(AppConstants.Time.REFRESH_TOKEN_VALIDITY_MS/1000))
                .build();

        refreshTokenRepository.save(refresh);

        return new TokenDto(accessToken, refreshToken);
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(String accessToken, String refreshToken) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("추출된 인증 정보 : {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.ErrorMessage.USER_NOT_FOUND));

        // Refresh Token 삭제 (로그아웃 하려는 브라우저의 쿠키에 있는 특정 토큰만 삭제 or 유저의 모든 토큰 삭제)
        if (refreshToken != null) {
            // 이 경우 본인이 사용하고 있는 브라우저의 쿠키만 삭제되서 모바일이나 다른 컴퓨터의 로그인은 남아있음
            refreshTokenRepository.deleteByToken(refreshToken);
        } else {
            // 쿠키가 없을 경우 안전하게 유저의 ID로 모든 Refresh Token 삭제
            refreshTokenRepository.deleteByUserId(user.getId());
        }

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
    public RefreshResponse refresh(String refreshToken) {
        // 1. 위/변조 여부 검증
        if (!tokenProvider.validateToken(refreshToken)) {
            // 검증 실패 예외
            throw new InvalidTokenException("위조된 토큰 입니다.");
        }

        // 2. 우리 서버에 존재하는 refresh token 인지 검증
        if(!refreshTokenRepository.existsByToken(refreshToken)){
            throw new InvalidTokenException("올바르지 않은 토큰입니다.");
        }

        // 3. 만료시간 확인
        Date expiration = tokenProvider.getExpiration(refreshToken);
        if(expiration.before(new Date())) {
            // 만료기간 지난 예외
            throw new InvalidTokenException("더 이상 사용할 수 없는 토큰입니다.");
        }

        // 4. 만료 안됐으면 새로운 access token을 만들어서 반환
        String email = tokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND));

        String accessToken = tokenProvider.generateAccessToken(email, user.getId(), user.getRole().name());

        return new RefreshResponse(accessToken);
    }

    /**
     * 이메일 찾기 메서드
     */
    public EmailSearchResponse emailSearch(String name, String phoneNumber) {
        User user = userRepository.findByNameAndPhoneNumberAndIsDeletedFalse(name, phoneNumber)
                .orElseThrow(() -> new CustomException("일치하는 회원 정보가 없습니다.", HttpStatus.BAD_REQUEST));

        return new EmailSearchResponse(user.getEmail());
    }

    /**
     * 비밀번호 찾기 인증용 이메일 전송 메서드
     */
    public void sendPasswordSearch(String email) {
        // 존재 여부 확인 (탈퇴 시에도 Exception 발생)
        userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new CustomException("이메일 주소를 다시 확인해주세요.", HttpStatus.NOT_FOUND));

        // 재발송 제한 확인 (쿨타임 1분)
        String rateLimitKey = AppConstants.Redis.KEY_PREFIX_FIND_PW_LIMIT + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
            throw new CustomException("잠시 후 다시 시도해주세요.(재전송 대기시간 : 1분)", HttpStatus.BAD_REQUEST);
        }

        // 랜덤 인증번호 6자리 생성
        SecureRandom random = new SecureRandom();
        String verificationCode = String.format("%06d", random.nextInt(1000000));

        // Redis에 저장 (Key: "AUTH:FIND_PW:" + email, Value: 인증번호, 만료시간: 5분)
        String redisKey = AppConstants.Redis.KEY_PREFIX_FIND_PW + email;
        redisTemplate.opsForValue().set(redisKey, verificationCode, Duration.ofMinutes(AppConstants.Time.VERIFICATION_EXPIRE_MINUTES));
        // 재발송 제한 키도 같이 저장
        redisTemplate.opsForValue().set(rateLimitKey, "LOCKED", Duration.ofMinutes(AppConstants.Time.VERIFICATION_EXPIRE_LIMIT_MINUTES));

        String subject = "[Soundock] 비밀번호 찾기 인증번호 안내";

        Context context = new Context();
        context.setVariable("code", verificationCode);

        emailService.sendMailAsync(email, subject, "search-passwd", context);
    }

    /**
     * 비밀번호 찾기 인증번호 검증 메서드
     */
    public String verifyPasswordSearch(String email, String code) {
        String redisKey = AppConstants.Redis.KEY_PREFIX_FIND_PW + email;
        String savedCode = redisTemplate.opsForValue().get(redisKey);

        // 만료되었거나 없는 경우
        if (savedCode == null) {
            throw new CustomException("인증 시간이 만료되었거나 잘못된 접근입니다. 다시 시도해주세요.", HttpStatus.BAD_REQUEST);
        }

        // 번호 불일치
        if (!savedCode.equals(code)) {
            throw new CustomException("인증번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        // 인증 성공시 비밀번호 변경용 임시 토큰 발행
        String resetToken = UUID.randomUUID().toString();
        String tokenKey = AppConstants.Redis.KEY_PREFIX_RESET_PW_TOKEN + email;

        // 비밀번호 변경 유효시간 설정 (5분)
        redisTemplate.opsForValue().set(tokenKey, resetToken, Duration.ofMinutes(AppConstants.Time.VERIFICATION_EXPIRE_MINUTES));

        // 사용 완료된 데이터 삭제 (인증번호, 발송제한)
        redisTemplate.delete(redisKey);
        redisTemplate.delete(AppConstants.Redis.KEY_PREFIX_FIND_PW_LIMIT + email);

        return resetToken;
    }

    /**
     * 비밀번호 찾기 전용 비밀번호 재설정 메서드
     */
    @Transactional
    public void resetPassword(ResetPasswdRequest request) {
        // Redis에서 임시 토큰 검증
        String tokenKey = AppConstants.Redis.KEY_PREFIX_RESET_PW_TOKEN + request.getEmail();
        String savedToken = redisTemplate.opsForValue().get(tokenKey);

        // 인증 토큰이 만료(5분 후 삭제) 되었거나 다른 경우
        if (savedToken == null || !savedToken.equals(request.getResetToken())) {
            throw new CustomException("인증 세션이 만료되었거나 유효하지 않은 시도입니다.", HttpStatus.UNAUTHORIZED);
        }

        // 유저 조회
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("유저를 찾을 수 없습니다."));

        // 유저 패스워드 변경
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        // 사용한 임시토큰 삭제
        redisTemplate.delete(tokenKey);
    }
}
