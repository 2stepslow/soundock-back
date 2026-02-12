package dopamine.soundock.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RestResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> RestResponse<T> success(String message, T data) {
        return RestResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    public static <T> RestResponse<T> success(T data) {
        return RestResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> RestResponse<T> success(String message) {
        return RestResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    public static <T> RestResponse<T> success() {
        return RestResponse.<T>builder()
                .success(true)
                .build();
    }

    public static <T> RestResponse<T> fail(String message) {
        return RestResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
