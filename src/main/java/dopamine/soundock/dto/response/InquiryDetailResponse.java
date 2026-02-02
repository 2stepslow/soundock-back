package dopamine.soundock.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import dopamine.soundock.entity.UserInquiry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class InquiryDetailResponse {
    // 문의 기본 내용
    private Integer userInquiryId;
    private String inquiryType;
    private String title;
    private String content;
    private String inquiryStatus;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // 첨부파일 정보
    private String fileUrl;

    // 관리자 답변 정보
    private String adminComment;
    private String commentStatus;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime commentCreatedAt;
    private String adminName;

    public static InquiryDetailResponse from(UserInquiry inquiry) {
        return InquiryDetailResponse.builder()
                .userInquiryId(inquiry.getUserInquiryId())
                .title(inquiry.getTitle())
                .inquiryType(inquiry.getInquiryType().name())
                .content(inquiry.getContent())
                .inquiryStatus(inquiry.getInquiryStatus().name())
                .createdAt(inquiry.getCreatedAt())
                .fileUrl(inquiry.getFileUrl())
                .adminComment(inquiry.getAdminComment())
                .commentStatus(inquiry.getCommentStatus() != null ? inquiry.getCommentStatus().name() : "PENDING")
                .commentCreatedAt(inquiry.getCommentCreatedAt())
                .adminName(inquiry.getAdmin() != null ? inquiry.getAdmin().getNickname() : null)
                .build();
    }
}
