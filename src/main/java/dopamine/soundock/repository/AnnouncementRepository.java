package dopamine.soundock.repository;


import dopamine.soundock.entity.Announcement;
import dopamine.soundock.enums.AnnounceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {

    // 카테고리 별 새 글 작성시 기존 글의 priority는 1로 변경, 새글은 0 저장
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Announcement a
           set a.priority = 1
         where a.announceType = :type
           and a.priority = 0
    """)
    int bumpOnlyZeroToOne(@Param("type") AnnounceType type);


    // 카테고리 별 글 수정시 수정글의 priority를 0으로 변경한다면, 기존 0인 글을 1로 변경
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update Announcement a
       set a.priority = 1
     where a.announceType = :type
       and a.priority = 0
       and a.announceId <> :announceId
""")
    int bumpOnlyZeroToOneExceptSelf(@Param("type") AnnounceType type,
                                    @Param("announceId") Integer announceId);


    // 파일 업로드 실패 시 게시글 노출 X
    @Modifying
    @Query("update Announcement a set a.isActive = false where a.announceId = :id")
    int deactivateById(@Param("id") Integer id);


    // 리스트 조회: priority=0 먼저, 그 다음 createdAt desc + isActive=1만
    @Query("""
        select a
          from Announcement a
         where a.isActive = true
         order by
           case when a.priority = 0 then 0 else 1 end asc,
           a.createdAt desc
    """)
    Page<Announcement> findActiveForList(Pageable pageable);

    // 상세 조회: isActive=1만
    Optional<Announcement> findByAnnounceIdAndIsActiveTrue(Integer announceId);

    // 푸터용: 타입별 priority=0
    Optional<Announcement> findFirstByAnnounceTypeAndIsActiveTrueAndPriorityOrderByCreatedAtDesc(
            AnnounceType announceType, Integer priority
    );


}
