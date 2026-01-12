package dopamine.soundock.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VerificationEmailRequest {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserSummary {
        private String email;
    }
    private UserSummary user;
}
