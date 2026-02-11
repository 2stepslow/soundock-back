package dopamine.soundock.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final CategoryTypeConverter categoryTypeConverter;

    public WebConfig(CategoryTypeConverter categoryTypeConverter) {
        this.categoryTypeConverter = categoryTypeConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(categoryTypeConverter);
    }

}
