package dopamine.soundock.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 패스워드리스 등록 응답
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "패스워드리스 가입 등록 응답 데이터")
public class PWLRegisterResponse {
    @Schema(description = "QR 코드 이미지 데이터 (Base64 인코딩 문자열)", example = "iVBORw0KGgoAAAANSUhEUgAA...")
    private String qr;

    @Schema(description = "패스워드리스 서비스 ID", example = "CORP_001")
    private String corpId;

    @Schema(description = "해당 등록 세션을 식별하는 고유 키", example = "reg_1234567890")
    private String registerKey;

    @Schema(description = "QR 코드 및 등록 세션의 유효 시간 (단위: 초)", example = "180")
    private Integer terms;

    @Schema(description = "인증 서버 메인 엔드포인트 URL", example = "https://auth.api.sample.com")
    private String serverUrl;

    @Schema(description = "푸시 알림 처리를 위한 커넥터 URL", example = "https://push.api.sample.com")
    private String pushConnectorUrl;

    @Schema(description = "푸시 커넥터 인증용 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String pushConnectorToken;

    @Schema(description = "사용자 식별 아이디 (이메일)", example = "user@example.com")
    private String userId;
}
