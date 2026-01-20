package dopamine.soundock.service;

import dopamine.soundock.dto.UpdateInfoRequest;
import dopamine.soundock.entity.User;
import dopamine.soundock.exceptions.CustomException;
import dopamine.soundock.exceptions.DuplicateNicknameException;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MypageService {
    private final UserRepository userRepository;

    @Transactional
    public void updateUser(UpdateInfoRequest request) {
        // 모든 필드가 null이거나 비어있는지 확인
        if ((request.getNickname() == null || request.getNickname().isBlank()) &&
                (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank())) {
            throw new CustomException("수정할 정보를 입력해주세요.", HttpStatus.BAD_REQUEST);
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        boolean isUpdated = false;

        // 닉네임 수정 할 시
        if (request.getNickname() != null
                && !request.getNickname().equals(user.getNickname())
                && !request.getNickname().isBlank()) {
            // 닉네임 중복 체크
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new DuplicateNicknameException();
            }
            user.setNickname(request.getNickname());
            isUpdated = true;
        }

        // 휴대폰 번호 수정 할 시
        if (request.getPhoneNumber() != null
                && !request.getPhoneNumber().isBlank()
                && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            user.setPhoneNumber(request.getPhoneNumber());
            isUpdated = true;
        }

        if (!isUpdated) {
            throw new CustomException("변경 사항이 없습니다.", HttpStatus.BAD_REQUEST);
        }

        // 저장
        userRepository.save(user);
    }
}
