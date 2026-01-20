package dopamine.soundock.service;

import dopamine.soundock.dto.*;
import dopamine.soundock.entity.AccessTokenBlacklist;
import dopamine.soundock.entity.User;
import dopamine.soundock.enums.UserStatus;
import dopamine.soundock.exceptions.*;
import dopamine.soundock.global.TokenProvider;
import dopamine.soundock.repository.AccessTokenBlacklistRepository;
import dopamine.soundock.repository.RefreshTokenRepository;
import dopamine.soundock.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class MypageService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;
    private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;

    @Transactional
    public void updateUserInfo(UpdateInfoRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 닉네임 수정 할 시
        if (request.getNickname() != null
                && !request.getNickname().equals(user.getNickname())
                && !request.getNickname().isBlank()) {
            // 닉네임 중복 체크
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new DuplicateNicknameException();
            }
            user.setNickname(request.getNickname());
        }

        // 휴대폰 번호 수정 할 시
        if (request.getPhoneNumber() != null
                && !request.getPhoneNumber().isBlank()
                && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // 저장
        userRepository.save(user);
    }

    // 비밀번호 검증 and 수정 공통 로직
    private User validatePasswordMatch(String password) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomException("현재 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
        return user;
    }

    // 비밀번호 검증
    @Transactional(readOnly = true)
    public void checkCurrentPassword(CurrentPasswdRequest request) {
        validatePasswordMatch(request.getCurrentPassword());
    }

    // 비밀번호 수정
    @Transactional
    public void updateUserPasswd(UpdatePasswdRequest request) {
        User user = validatePasswordMatch(request.getCurrentPassword());

        // 새 비밀번호가 기존 비밀번호와 같은지 확인
        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException("현재 비밀번호와 동일한 비밀번호로 변경할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);

        // 저장
        userRepository.save(user);
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(DeleteUserRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 이미 탈퇴한 사용자인지 확인
        if (user.isDeleted()) {
            throw new CustomException("이미 탈퇴한 사용자입니다.", HttpStatus.BAD_REQUEST);
        }

        // 닉네임 중복 제약 조건을 해제하기 위해 null 처리
        user.setNickname(null);

        // 유저 상태 변경 (Soft Delete)
        user.setDeleted(true);
        user.setStatus(UserStatus.QUITTED);
        user.setDeletedAt(LocalDateTime.now());

        userRepository.save(user);

        // Refresh Token 무효화
        refreshTokenRepository.deleteByUserId(user.getId());

        // Access Token 유효성 검증
        String accessToken = request.getAccessToken();
        validateAccessToken(accessToken, email);

        // Access Token 블랙리스트 등록
        Date expDate = tokenProvider.getExpiration(accessToken);
        LocalDateTime convertedDate = expDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        AccessTokenBlacklist accessTokenBlacklist = AccessTokenBlacklist
                .builder()
                .accessToken(accessToken)
                .expirationAt(convertedDate)
                .build();

        accessTokenBlacklistRepository.save(accessTokenBlacklist);
    }

    // 토큰 유효성 검증 메서드
    private void validateAccessToken(String accessToken, String currentUserEmail) {
        // 토큰이 비어있는지 확인
        if (accessToken == null || accessToken.isBlank()) {
            throw new InvalidTokenException("액세스 토큰이 비어있습니다.");
        }

        // 토큰 형식 및 만료 여부 검증
        if (!tokenProvider.validateToken(accessToken)) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        }

        // 토큰에서 이메일 추출
        String tokenEmail;
        try {
            tokenEmail = tokenProvider.getEmailFromToken(accessToken);
        } catch (Exception e) {
            throw new InvalidTokenException("토큰에서 사용자 정보를 추출할 수 없습니다.");
        }

        // 현재 로그인한 사용자와 토큰의 사용자가 일치하는지 확인
        if (!currentUserEmail.equals(tokenEmail)) {
            throw new UnauthorizedException("토큰 정보가 현재 사용자와 일치하지 않습니다.");
        }

        // 이미 블랙리스트에 등록된 토큰인지 확인
        if (accessTokenBlacklistRepository.existsByAccessToken(accessToken)) {
            throw new InvalidTokenException("이미 무효화된 토큰입니다.");
        }
    }
}
