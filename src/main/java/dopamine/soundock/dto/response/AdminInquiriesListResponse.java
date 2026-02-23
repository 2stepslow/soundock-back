package dopamine.soundock.dto.response;

import dopamine.soundock.entity.UserInquiry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AdminInquiriesListResponse {
    private Integer inquiryId;
    private String inquiryType;
    private String nickName;
    private Integer userId;
    private String title;
    private String inquiryStatus;
    private LocalDateTime createdAt;

    public static AdminInquiriesListResponse from(UserInquiry inquiry) {
        return AdminInquiriesListResponse.builder()
                .inquiryId(inquiry.getUserInquiryId())
                .inquiryType(inquiry.getInquiryType().name())
                .userId(inquiry.getUser().getId())
                .nickName(inquiry.getUser().getNickname())
                .title(inquiry.getTitle())
                // inquiryStatus가 null 값이면 .name() 호출 시 NullPointerException 발생할 수 있으니 삼항 연산자로 안전하게 처리
                .inquiryStatus(inquiry.getInquiryStatus() !=null ? inquiry.getInquiryStatus().name() : null)
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}
