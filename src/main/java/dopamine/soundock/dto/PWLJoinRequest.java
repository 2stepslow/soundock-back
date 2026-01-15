package dopamine.soundock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PWLJoinRequest {
    private String email;
    private String name;

    // 이메일만 받는 생성자
    public PWLJoinRequest(String email) {
        this.email = email;
    }
}
