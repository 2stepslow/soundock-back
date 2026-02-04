package dopamine.soundock.dto.response;

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
public class PWLRegisterResponse {
    private String qr;
    private String corpId;
    private String registerKey;
    private Integer terms;
    private String serverUrl;
    private String pushConnectorUrl;
    private String pushConnectorToken;
    private String userId;
}
