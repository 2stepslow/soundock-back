package dopamine.soundock.dto;

import dopamine.soundock.entity.User;
import dopamine.soundock.entity.VerificationToken;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VerificationEmailRequest {
    private User user;
    private String token;
}
