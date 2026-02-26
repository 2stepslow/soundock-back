package dopamine.soundock.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PWLResultRequest {
    @NotBlank
    String userId;

    @NotBlank
    String sessionId;
}
