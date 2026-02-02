package dopamine.soundock.config;

import dopamine.soundock.enums.CategoryType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CategoryTypeConverter implements Converter<String, CategoryType> {

    @Override
    public CategoryType convert(String categoryType) {
        // source가 "SPOTLIGHT"로 들어오면 위에서 만든 fromString 호출
        return CategoryType.fromString(categoryType);
    }
}
