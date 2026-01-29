package dopamine.soundock.dto.response;

import dopamine.soundock.entity.UserInquiry;
import dopamine.soundock.enums.InquiryStatus;
import dopamine.soundock.enums.InquiryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "1:1 문의 목록 조회 응답")
public class InquirySummaryResponse {
    private Integer inquiryId;
    private String inquiryType;
    private Integer userId;
    private String title;
    private String inquiryStatus;
    private LocalDateTime createdAt;

    public static InquirySummaryResponse from(UserInquiry inquiry) {
        return InquirySummaryResponse.builder()
                .inquiryId(inquiry.getUserInquiryId())
                .inquiryType(inquiry.getInquiryType().name())
                .userId(inquiry.getUser().getId())
                .title(inquiry.getTitle())
                .inquiryStatus(inquiry.getInquiryStatus().name())
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}
