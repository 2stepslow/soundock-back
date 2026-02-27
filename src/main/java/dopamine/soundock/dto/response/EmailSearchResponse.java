package dopamine.soundock.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "이메일 찾기 조회 응답")
public class EmailSearchResponse {

    @Schema(description = "실명, 전화번호 매칭으로 찾은 이메일 정보")
    private List<String> email;
}
