package dopamine.soundock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PWLRequest {
    private String email;
    private String sessionId;
}
