package dopamine.soundock.dto.response;


import dopamine.soundock.entity.Notifications;
import dopamine.soundock.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Integer notificationId;
    private Integer sendingUserId;
    private String senderNickname;
    private NotificationType notificationtype;
    private Integer boardId;
    private LocalDateTime createdAt;
    private boolean isRead;

    public static NotificationResponse from(Notifications notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .sendingUserId(notification.getSendingUser().getId())
                .senderNickname(notification.getSendingUser().getNickname())
                .notificationtype(notification.getNotificationType())
                .boardId(notification.getBoard() != null ? notification.getBoard().getBoardId() : null)
                .createdAt(notification.getCreatedDatetime())
                .isRead(notification.getReadAt() != null)
                .build();
    }
}
