package dopamine.soundock.repository;

import dopamine.soundock.entity.User;
import dopamine.soundock.entity.UserInquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInquiryRepository extends JpaRepository<UserInquiry, Integer> {
    // 유저의 모든 1:1 문의 조회(생성일자로 내림차순) + 페이징
    Page<UserInquiry> findAllByUser(User user, Pageable pageable);
}
