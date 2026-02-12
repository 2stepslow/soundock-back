package dopamine.soundock.config;

import dopamine.soundock.enums.UserStatus;
import org.springframework.core.convert.converter.Converter;

public class UserStatusConverter implements Converter<String, UserStatus> {
    @Override
    public UserStatus convert(String userStatus) {
        return UserStatus.fromString(userStatus);
    }
}
