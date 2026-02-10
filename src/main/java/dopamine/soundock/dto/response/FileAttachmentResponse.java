package dopamine.soundock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class FileAttachmentResponse {

    private String filekey;
    private String originalFilename;
}