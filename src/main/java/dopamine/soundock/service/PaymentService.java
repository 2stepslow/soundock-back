package dopamine.soundock.service;

import dopamine.soundock.dto.request.CancelPaymentRequest;
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
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class PaymentService {
    private final WebClient tossWebClient;
    private final PopHistoryRepository popHistoryRepository;
    private final TossPaymentRepository tossPaymentRepository;
    private final UserRepository userRepository;

    // 결제 주문 정보 생성
    @Transactional
    public PreparePaymentRequest preparePayment(PreparePaymentRequest prepareRequest){

         // 결제 시도자가 로그인한 유저인지 검증
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // 테스트용
//            String email = "linlin@gmail.com";
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
        if (!Objects.equals(popHistory.getActualAmount(), confirmRequest.getAmount())){
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
                                 clientResponse -> clientResponse.bodyToMono(String.class)
                                         .flatMap(body -> Mono.error(
                                         new IllegalArgumentException("토스 서버 내부에서 오류가 발생했습니다."))
                                 ))
                         .bodyToMono(ConfirmPaymentResponse.class)
                         .block();

        // status가 DONE이 아닐 경우
        if (!Objects.requireNonNull(confirmPaymentResponse).getStatus().equals("DONE")){
            throw new IllegalArgumentException("결제가 완료되지 않았습니다. 현재 결제 상태 : " + confirmPaymentResponse.getStatus());
        }

        // orderId랑 일치하는 객체인 popHistory의 결제 정보 내역 업데이트
        popHistory.completeChargePayment(PopStatus.COMPLETED, PopTarget.CHARGE);
        popHistoryRepository.save(popHistory);

        // TossPayment 객체에서 받은 정보를 DB에 저장
        TossPayment tossPayment = confirmPaymentResponse.toEntity(popHistory);
        tossPaymentRepository.save(tossPayment);

         return confirmPaymentResponse;
    }

    // PaymentKey를 통한 승인된 결제 조회
    public ConfirmPaymentResponse getPaymentByKey(String paymentKey){

        // 결제 내역 조회자가 로그인한 유저인지 검증
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // 테스트용
//        String email = "linlin@gmail.com" ;
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));


        TossPayment tossPayment = tossPaymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new ResourceNotFoundException("유효하지 않은 요청입니다. PaymentKey를 확인해주세요."));

        // 승인된 결제에 대해서만 조회 가능
        if (tossPayment.getApprovedDatetime() == null){
            throw new IllegalArgumentException("승인 완료되지 않은 결제입니다.");
        }

        return tossWebClient.get()
                .uri("/payments/{paymentKey}", paymentKey)
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
    }

    // orderId를 통한 승인된 결제 조회
    public ConfirmPaymentResponse getPaymentById(String orderId) {

        // 결제 내역 조회자가 로그인한 유저인지 검증
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // 테스트용
//        String email = "linlin@gmail.com" ;
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));


        TossPayment tossPayment = tossPaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문번호와 일치하는 주문 내역이 없습니다."));

        if (tossPayment.getApprovedDatetime() == null){
            throw new IllegalArgumentException(("승인 완료되지 않은 결제입니다."));
        }

        return tossWebClient.get()
                .uri("/payments/orders/{orderId}", orderId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new ResourceNotFoundException("요청을 처리할 수 없습니다."))
                                ))
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new IllegalArgumentException("토스 서버 내부에서 오류가 발생했습니다."))
                                ))
                .bodyToMono(ConfirmPaymentResponse.class)
                .block();
    }
    // 결제 취소
    @Transactional
    public ConfirmPaymentResponse cancelPayment(
            String paymentKey,
            CancelPaymentRequest cancelPaymentRequest
    ){
        // 결제 취소 시도자가 로그인한 유저인지 검증
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // 테스트용
//        String email = "linlin@gmail.com" ;
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 유효한 paymentKey 값인지 확인
        TossPayment tossPayment = tossPaymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new ResourceNotFoundException("유효하지 않은 PaymentKey입니다."));

        if (tossPayment.getTossPaymentStatus().equals("CANCELED")) {
            throw new IllegalArgumentException("이미 취소된 결제 내역입니다.");
        }

        if (cancelPaymentRequest == null){
            throw new IllegalArgumentException("결제 취소 사유를 입력해주세요.");
        }

        // 멱등키 존재 여부 확인
        String idempotencyKey;
        if (tossPayment.getCancelId() != null){
            // 이미 생성된 멱등키 사용
           idempotencyKey = tossPayment.getCancelId();
           log.info("기존 멱등키 사용 : {}", idempotencyKey);
        } else {
            // 새로운 멱등키 생성
            idempotencyKey = UUID.randomUUID().toString();

            // 멱등키 DB로 저장
            tossPayment.setCancelId(idempotencyKey);
            tossPaymentRepository.save(tossPayment);
            log.info("새 멱등키 생성 및 저장 : {}", idempotencyKey);
        }

        // 토스에서 받은 내역
        ConfirmPaymentResponse cancelPaymentResponse
                = tossWebClient.post()
                .uri("/payments/{paymentKey}/cancel", paymentKey)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey)
                .bodyValue(cancelPaymentRequest)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new ResourceNotFoundException("요청을 처리할 수 없습니다."))
                                ))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new IllegalArgumentException("토스 서버 내부에서 오류가 발생했습니다."))
                                ))
                .bodyToMono(ConfirmPaymentResponse.class)
                .block();

        // paymentKey 값을 통해 찾은 tossPayment
        if (cancelPaymentResponse == null){
            throw new NullPointerException("결제 취소 내역에 대한 정보를 전달 받지 못했습니다.");
        }
        // 취소 내역 DB 업데이트 후 저장
        tossPayment.cancelUpdatePayment(cancelPaymentResponse);
        tossPaymentRepository.save(tossPayment);

        // tossPayment 객체의 orderId와 일치하는 popHistory 내역 업데이트
        PopHistory popHistory = popHistoryRepository.findByOrderId(tossPayment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("취소할 주문 내역이 존재하지 않습니다."));

        // popHistory 테이블에서 취소된 상태인지 재검증
        if (popHistory.getPopStatus() == PopStatus.CANCELED){
            log.warn("이미 주문 내역 취소가 완료된 건입니다. orderId : {}", popHistory.getOrderId());
            return cancelPaymentResponse;
        }

        popHistory.completeCancelPayment(popHistory.getChangeAmount()-tossPayment.getAmount(), PopStatus.CANCELED);
        popHistoryRepository.save(popHistory);

        return cancelPaymentResponse;
    }
}
