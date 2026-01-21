package dopamine.soundock.service;

import dopamine.soundock.entity.Oauth;
import dopamine.soundock.entity.User;
import dopamine.soundock.exceptions.CustomException;
import dopamine.soundock.repository.OauthRepository;
import dopamine.soundock.repository.UserRepository;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class YouTubeAuthService {

    private final OauthRepository oauthRepository;
    private final UserRepository userRepository;

    @Transactional
    public void saveOrUpdateGoogleTokens(String email, String accessToken, String refreshToken, LocalDateTime expiresAt) {
        // 이메일로 우리 서비스 유저 찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND));

        // 기존 OAuth 정보가 있는지 확인
        Oauth oauth = oauthRepository.findByUser(user)
                .orElse(Oauth.builder()
                        .user(user)
                        .provider("GOOGLE")
                        .build());

        // 토큰 정보 업데이트
        oauth.updateTokens(accessToken, refreshToken, expiresAt);

        // 저장
        oauthRepository.save(oauth);
    }
}
