package dopamine.soundock.service;


import dopamine.soundock.dto.request.SendMessageRequest;
import dopamine.soundock.dto.response.MessageResponse;
import dopamine.soundock.entity.Messages;
import dopamine.soundock.entity.User;
import dopamine.soundock.enums.MessageType;
import dopamine.soundock.enums.UserStatus;
import dopamine.soundock.exceptions.CustomException;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.repository.MessageRepository;
import dopamine.soundock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static dopamine.soundock.enums.MessageType.RECEIVED;
import static dopamine.soundock.enums.MessageType.SENT;

@RequiredArgsConstructor
@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    // 메세지 전송
    @Transactional
    public void sendMessage(Integer receivedUserId, SendMessageRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User sendingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 받는 사람 존재 확인
        User receivedUser = userRepository.findById(receivedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("메세지를 받을 사용자를 찾을 수 없습니다."));

        // 받는 사람이 탈퇴한 회원인지 확인
        if (receivedUser.isDeleted() || receivedUser.getStatus().equals(UserStatus.QUITTED)) {
            throw new CustomException("탈퇴한 유저입니다. 메세지를 보낼 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        // 본인에게 메세지 전송 방지
        if (receivedUser.getId().equals(sendingUser.getId())) {
            throw new CustomException("본인에게는 메세지를 보낼 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        Messages message = new Messages();
        message.setContent(request.getContent());
        message.setSendingUser(sendingUser);
        message.setReceivedUser(receivedUser);
        message.setCreatedDatetime(LocalDateTime.now());
        messageRepository.save(message);
    }


    // 메세지 전체 조회
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(MessageType type){

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        List<Messages> messages;

        switch (type) {
            case RECEIVED :
                messages = messageRepository.findByReceivedUser_IdOrderByCreatedDatetimeDesc(user.getId());

                // 안 읽은 메세지 먼저, 읽은 메세지 나중에
                messages.sort((m1, m2) -> {
                    boolean m1Unread = m1.getReadAt() == null;
                    boolean m2Unread = m2.getReadAt() == null;

                    if (m1Unread && !m2Unread) return -1;  // m1 안 읽음, m2 읽음 -> m1 먼저
                    if (!m1Unread && m2Unread) return 1;   // m1 읽음, m2 안 읽음 -> m2 먼저

                    // 둘 다 읽음 또는 둘 다 안 읽음 -> 최신순
                    return m2.getCreatedDatetime().compareTo(m1.getCreatedDatetime());
                });
                break;
            case SENT :
                // 보낸 메세지
                messages = messageRepository.findBySendingUser_IdOrderByCreatedDatetimeDesc(user.getId());
                break;
            default:
                throw new IllegalArgumentException("유효하지 않은 타입입니다.");
        }

        return messages.stream()
                .map(MessageResponse::from)
                .collect(Collectors.toList());
    }


    // 메세지 상세 조회
    @Transactional
    public MessageResponse getMessageDetail(Integer messageId) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));


        Messages message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("메세지를 찾을 수 없습니다."));

        // 보낸 사람 / 받은 사람만 조회 가능
        if (!message.getSendingUser().getId().equals(user.getId())
                && !message.getReceivedUser().getId().equals(user.getId())) {
            throw new CustomException("메세지를 확인 할 수 없습니다.", HttpStatus.UNAUTHORIZED);
        }

        // 아직 읽지 않았으면 읽음 처리
        if (message.getReceivedUser().getId().equals(user.getId()) && message.getReadAt() == null) {
            message.setReadAt(LocalDateTime.now());
            messageRepository.save(message);
        }

        return MessageResponse.from(message);
    }


    // 안 읽은 메세지 개수만 내려줌
    @Transactional(readOnly = true)
    public Integer getUnreadMessageCount() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        return messageRepository.countUnreadMessages(user.getId());
    }

}
