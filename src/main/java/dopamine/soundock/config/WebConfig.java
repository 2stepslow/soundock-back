package dopamine.soundock.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;


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

    @Value("${file.upload.base-path}")
    private String basePath;

    /**
     * 파일을 서버 로컬 폴더에 업로드하기 위해 필요한 웹 설정
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = new File(basePath).getAbsolutePath() + "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath);
    }
}
