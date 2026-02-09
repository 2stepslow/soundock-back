package dopamine.soundock.dto.request;

import dopamine.soundock.enums.InquiryType;
import dopamine.soundock.global.constants.AppConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "1:1문의 등록 요청")
public class InquiryCreateRequest {
    @Schema(description = "1:1 문의 타입")
    @NotNull(message = "문의 타입을 선택해주세요.")
    private InquiryType inquiryType;

    @Schema(description = "1:1 문의 제목")
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Pattern(
            regexp = AppConstants.ValidationPattern.INQUIRY_TITLE_PATTERN,
            message = AppConstants.ErrorMessage.INQUIRY_TITLE_ERROR
    )
    private String title;

    @Schema(description = "1:1 문의 내용")
    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    @Pattern(
            regexp = AppConstants.ValidationPattern.INQUIRY_CONTENT_PATTERN,
            message = AppConstants.ErrorMessage.INQUIRY_CONTENT_ERROR
    )
    private String content;

    @Schema(description = "S3 업로드 후 받은 파일 접근 URL (파일 미첨부 시 null)",
            example = "https://my-bucket.s3.amazonaws.com/inquiries/uuid-file.png")
    private String fileUrl;

    @Schema(description = "S3 업로드 후 받은 파일 고유 Key (파일 미첨부 시 null, 향후 삭제/수정 시 필요)",
            example = "inquiries/uuid-file.png")
    private String fileKey;
}
