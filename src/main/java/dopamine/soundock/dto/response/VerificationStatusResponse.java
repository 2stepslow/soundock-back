package dopamine.soundock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class VerificationStatusResponse {
    private String email;
    private boolean isVerified;
    private boolean isExpired;
}
