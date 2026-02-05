package dopamine.soundock.dto.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PresignedUrlResponse {
    private String uploadUrl;
    private String downloadUrl;
    private String fileKey;
    private String fileUrl;
}
