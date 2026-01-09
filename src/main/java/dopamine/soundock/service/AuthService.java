package dopamine.soundock.service;

import dopamine.soundock.dto.UserSignupRequest;
import dopamine.soundock.dto.ValidateEmailRequest;
import dopamine.soundock.entity.User;
import dopamine.soundock.enums.UserRole;
import dopamine.soundock.enums.UserStatus;
import dopamine.soundock.exceptions.CustomException;
import dopamine.soundock.exceptions.DuplicateEmailException;
import dopamine.soundock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 이메일 중복 확인
    @Transactional(readOnly = true)
    public void validateEmail(ValidateEmailRequest validateEmailRequest) {
        // 중복 일 경우 예외 발생
        if (userRepository.existsByEmail(validateEmailRequest.getEmail())) {
            throw new DuplicateEmailException();
        }
    }

    // 회원가입
    public void signupUser(UserSignupRequest userSignupRequest) {
        String encodedPassword = passwordEncoder.encode(userSignupRequest.getPassword());
        // 이메일 중복 체크
        if (userRepository.existsByEmail(userSignupRequest.getEmail())) {
            throw new DuplicateEmailException();
        }

        // 숫자 이외의 모든 문자 제거
        String fixPhone = userSignupRequest.getPhoneNumber().replaceAll("[^0-9]", "");

        // DB 컬럼 길이와 일치하는지 최종 확인
        if (fixPhone.length() != 11) {
            throw new CustomException("연락처 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setEmail(userSignupRequest.getEmail());
        user.setPassword(encodedPassword);
        user.setNickname(userSignupRequest.getNickname());
        user.setPhoneNumber(fixPhone);
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.PENDING);

        userRepository.save(user);
    }
}
