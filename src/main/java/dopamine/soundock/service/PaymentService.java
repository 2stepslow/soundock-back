package dopamine.soundock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dopamine.soundock.dto.request.ConfirmPaymentRequest;
import dopamine.soundock.dto.response.ConfirmPaymentResponse;
import dopamine.soundock.dto.request.PreparePaymentRequest;
import dopamine.soundock.entity.PopHistory;
import dopamine.soundock.entity.TossPayment;
import dopamine.soundock.entity.User;
import dopamine.soundock.enums.PopStatus;
import dopamine.soundock.enums.PopTarget;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.repository.PopHistoryRepository;
import dopamine.soundock.repository.TossPaymentRepository;
import dopamine.soundock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class PaymentService {
    private final WebClient tossWebClient;
    private final PopHistoryRepository popHistoryRepository;
    private final TossPaymentRepository tossPaymentRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // 결제 주문 정보 생성
    @Transactional
    public PreparePaymentRequest preparePayment(PreparePaymentRequest prepareRequest){

         // 결제 시도자가 로그인한 유저인지 검증
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // 테스트용
            String email = "linlin@gmail.com";
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 토스에게 줄 orderId 생성
        String orderId = UUID.randomUUID().toString();

        // 우리 DB에 기록할 주문 정보
        PopHistory popHistory = PopHistory.builder()
                .user(user)
                .orderId(orderId)
                .changeAmount(prepareRequest.getChangeAmount())
                .actualAmount(prepareRequest.getAmount())
                .popStatus(PopStatus.PENDING)
                .popTarget(PopTarget.CHARGE)
                .createdDatetime(LocalDateTime.now())
                .build();
        popHistoryRepository.save(popHistory);

        return PreparePaymentRequest.builder()
                .orderId(orderId)
                .amount(prepareRequest.getAmount())
                .build();
    }

    // 결제 승인 요청
    @Transactional
    public ConfirmPaymentResponse confirmPayment(ConfirmPaymentRequest confirmRequest) {
        // 결제 시도자가 로그인한 유저인지 검증
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // 테스트용
//        String email = "linlin@gmail.com" ;
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 받은 orderId와 DB에 저장된 값 일치하는지 검증
        PopHistory popHistory = popHistoryRepository.findByOrderId(confirmRequest.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("주문 Id가 일치하지 않는 결제 요청입니다."));

        // 받은 실제 결제 금액과 DB에 저장된 값 일치하는지 검증
        if (!popHistory.getActualAmount().equals(confirmRequest.getAmount())){
            throw new ResourceNotFoundException("결제 요청 금액과 일치하지 않습니다.");
        }

        // tossPayment 객체 받아옴
         ConfirmPaymentResponse confirmPaymentResponse =
                 tossWebClient.post()
                         .uri("/payments/confirm")
                         .contentType(MediaType.APPLICATION_JSON)
                         .bodyValue(confirmRequest)
                         .retrieve()
                         .onStatus(HttpStatusCode::is4xxClientError,
                                 clientResponse -> clientResponse.bodyToMono(String.class)
                                     .flatMap(body -> Mono.error(
                                             new ResourceNotFoundException("요청을 처리할 수 없습니다.")
                                     ))
                         )
                         .onStatus(HttpStatusCode::is5xxServerError,
                                 clientResponse -> Mono.error(
                                         new IllegalArgumentException("토스 서버 내부에서 오류가 발생했습니다.")
                                 ))
                         .bodyToMono(ConfirmPaymentResponse.class)
                         .block();

        // status가 DONE이 아닐 경우
        if (!"DONE".equals(confirmPaymentResponse.getStatus())){
            throw new IllegalArgumentException("결제가 완료되지 않았습니다. 현재 결제 상태 : " + confirmPaymentResponse.getStatus());
        }

        // orderId랑 일치하는 객체인 popHistory의 결제 정보 내역 업데이트
        popHistory.completeChargePayment(PopStatus.COMPLETED, PopTarget.CHARGE);
        popHistoryRepository.save(popHistory);

        // TossPayment 객체에서 받은 정보를 DB에 저장
        TossPayment tossPayment = confirmPaymentResponse.toEntity();
        tossPaymentRepository.save(tossPayment);

         return confirmPaymentResponse;
    }
}
