package dopamine.soundock.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class MultipleFileRequest {
    private List<String> fileNames;
    private List<String> fileTypes;
}
