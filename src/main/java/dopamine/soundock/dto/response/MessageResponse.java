package dopamine.soundock.dto.response;


import dopamine.soundock.entity.Messages;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
    private Integer messageId;
    private String content;
    private LocalDateTime createdDatetime;
    private LocalDateTime readAt;

    private Integer sendingUserId;
    private String sendingUserNickname;

    private Integer receivedUserId;
    private String receivedUserNickname;

    private Boolean isRead;     // null이면 안 읽음, null이 아니면 읽음 처리

    public static MessageResponse from(Messages message) {
        return MessageResponse.builder()
                .messageId(message.getMessageId())
                .content(message.getContent())
                .createdDatetime(message.getCreatedDatetime())

                .readAt(message.getReadAt())
                .isRead(message.getReadAt() != null)

                .sendingUserId(message.getSendingUser().getId())
                .sendingUserNickname(message.getSendingUser().getNickname())

                .receivedUserId(message.getReceivedUser().getId())
                .receivedUserNickname(message.getReceivedUser().getNickname())
                .build();
    }

}
