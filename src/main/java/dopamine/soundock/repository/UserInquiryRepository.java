package dopamine.soundock.repository;

import dopamine.soundock.entity.User;
import dopamine.soundock.entity.UserInquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserInquiryRepository extends JpaRepository<UserInquiry, Integer> {
    // 유저의 모든 1:1 문의 조회(생성일자로 내림차순) + 페이징
    Page<UserInquiry> findAllByUser(User user, Pageable pageable);

    Page<UserInquiry> findAllByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Optional<UserInquiry> findByUserInquiryIdAndUser_Email(Integer userInquiryId, String email);

    Optional<UserInquiry> findByUserInquiryId(Integer userInquiryId);
}
