package dopamine.soundock.service;


import dopamine.soundock.dto.response.NotificationResponse;
import dopamine.soundock.entity.Board;
import dopamine.soundock.entity.Notifications;
import dopamine.soundock.entity.User;
import dopamine.soundock.enums.NotificationType;
import dopamine.soundock.exceptions.CustomException;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.global.constants.AppConstants;
import dopamine.soundock.repository.NotificationRepository;
import dopamine.soundock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // 알림 전체 조회 (읽지 않은 알림 먼저 조회 후 최신순 조회)
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Integer page, Integer size) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.ErrorMessage.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);

        Page<Notifications> notifications = notificationRepository.findByReceivedUserId(user.getId(), pageable);

        return notifications.map(NotificationResponse::from);
    }


    // 알림 상세 조회
    @Transactional
    public NotificationResponse getNotificationsdetail(Integer notificationId) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        Notifications notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("알림을 찾을 수 없습니다."));

        // 본인의 알림만 읽음 처리 가능
        if (!notification.getReceivedUser().getId().equals(user.getId())) {
            throw new CustomException("알림을 확인할 수 없습니다.", HttpStatus.UNAUTHORIZED);
        }

        // 읽음 처리
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }

        return NotificationResponse.from(notification);
    }

    // 읽지 않은 알림 개수 조회
    @Transactional(readOnly = true)
    public Integer getUnreadNotificationCount() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        return notificationRepository.countUnreadNotifications(user.getId());
    }


    // 알림 추가 - 좋아요/댓글/후원 시 사용
    @Transactional
    public void createNotification(User receivedUser, User sendingUser,
                                   NotificationType notificationtype, Board board) {

        // 알림 중복 생성 방지  (한 게시글의 반복된 좋아요와 취소 시 작성 방지), 댓글/후원은 중복 가능
        if (notificationtype == NotificationType.LIKE) {
            boolean alreadyExists = notificationRepository.existsByCondition(
                    sendingUser.getId(),
                    receivedUser.getId(),
                    board.getBoardId(),
                    notificationtype
            );

            // 있다면 종료
            if (alreadyExists) {
                return;
            }
        }


        // 없으면 알림 생성
        Notifications notification = new Notifications();
        notification.setReceivedUser(receivedUser);
        notification.setSendingUser(sendingUser);
        notification.setNotificationType(notificationtype);
        notification.setBoard(board);

        notificationRepository.save(notification);
    }

}
