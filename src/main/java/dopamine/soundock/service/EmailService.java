package dopamine.soundock.service;

import dopamine.soundock.entity.User;
import dopamine.soundock.entity.VerificationToken;
import dopamine.soundock.enums.UserStatus;
import dopamine.soundock.exceptions.CustomException;
import dopamine.soundock.repository.UserRepository;
import dopamine.soundock.repository.VerificationTokenRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;

    /**
     * 이메일 발송 메서드 (메일 발송이라는 무거운 작업을 따로 실행하기 위해 비동기 처리)
     */
    @Async
    public void sendMailAsync(String email, String subject, String templateName, Context context) {
        try {
            String htmlContent = templateEngine.process(templateName, context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message); // 여기서 5~6초 걸려도 호출자는 이미 응답하고 떠난 상태!
        } catch (Exception e) {
            log.error("이메일 발송 실패 - 수신자: {}, 원인: {}", email, e.getMessage());
        }
    }

    /**
     * 이메일 원클릭 인증
     */
    @Transactional
    public void verifyUser(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new CustomException("유효하지 않은 인증입니다.", HttpStatus.BAD_REQUEST));

        if (verificationToken.isVerified()) {
            throw new CustomException("이미 인증이 완료된 이메일 입니다.", HttpStatus.CONFLICT);
        }

        // 만료 시간 null 체크
        LocalDateTime expiryDate = verificationToken.getExpiryDate();
        if (expiryDate == null || expiryDate.isBefore(LocalDateTime.now())) {
            throw new CustomException("인증 시간이 만료되었습니다. 다시 시도해주세요.", HttpStatus.GONE);
        }

        User user = verificationToken.getUser();
        if (user != null) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        }
        verificationToken.setVerified(true);
        verificationTokenRepository.save(verificationToken);
    }
}
