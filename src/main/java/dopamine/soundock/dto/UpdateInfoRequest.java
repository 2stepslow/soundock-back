package dopamine.soundock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "회원 정보 수정 요청 정보")
public class UpdateInfoRequest {

    @Schema(description = "변경할 커뮤니티 활동 닉네임 (한글/영문/숫자 10자 이내, 값이 없을 경우 아예 빼고 보내줘야함")
    @Pattern(
            regexp = "^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ]{1,10}$",
            message = "닉네임은 특수문자를 제외하고 10자 이내로 입력해주세요."
    )
    private String nickname;

    @Schema(description = "변경할 연락처 (숫자만 11자리), 값이 없을 경우 아예 빼고 보내줘야함")
    @Pattern(
            regexp = "^010[0-9]{8}$",
            message = "연락처는 숫자만 8자리 입력하세요."
    )
    private String phoneNumber;
}
