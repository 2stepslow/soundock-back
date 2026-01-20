package dopamine.soundock.service;

import dopamine.soundock.dto.CurrentPasswdRequest;
import dopamine.soundock.dto.UpdateInfoRequest;
import dopamine.soundock.dto.UpdatePasswdRequest;
import dopamine.soundock.entity.User;
import dopamine.soundock.exceptions.CustomException;
import dopamine.soundock.exceptions.DuplicateNicknameException;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MypageService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
}
