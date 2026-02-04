package dopamine.soundock.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "패스워드리스 로그인 트리거 요청 데이터")
public class PWLLoginTriggerRequest {
    @Schema(description = "로그인을 요청한 사용자 아이디(이메일)")
    private String email;

}
