package dopamine.soundock.dto;

import lombok.Data;

@Data // Getter, Setter 등을 자동 생성
public class PWLRegisterRequest {
    private String email;
    private String password;
}
