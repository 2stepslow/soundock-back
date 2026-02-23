package dopamine.soundock.dto.request;

import dopamine.soundock.enums.CategoryType;
import dopamine.soundock.enums.SearchType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "제목 또는 닉네임으로 글 검색")
public class BoardSearchRequest {

    @Schema(description = "게시판 카테고리 타입")
    @NotNull
    private CategoryType categoryType;

    @Schema(description = "검색종류 (글제목 / 닉네임)")
    @NotNull
    private SearchType searchType;

    @Schema(description = "검색어")
    @NotBlank(message = "검색어를 입력 해 주세요")
    private String keyword;
}
