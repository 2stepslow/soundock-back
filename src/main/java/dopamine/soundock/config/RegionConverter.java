package dopamine.soundock.config;

import dopamine.soundock.enums.Region;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RegionConverter implements Converter<String, Region> {

    @Override
    public Region convert(String region) {
        // source가 "seoul"로 들어오면 대소문자 무시하고 Region enum으로 변환
        return Region.valueOf(region.toUpperCase());
    }
}
