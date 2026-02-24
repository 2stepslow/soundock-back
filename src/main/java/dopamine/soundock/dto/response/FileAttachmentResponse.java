package dopamine.soundock.dto.response;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class FileAttachmentResponse {

    private Integer attachmentId;
    private String filekey;
    private String originalFilename;
}