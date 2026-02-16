package dopamine.soundock.controller;


import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.dto.response.NotificationResponse;
import dopamine.soundock.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/users/notification")
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "알림 리스트 조회",
            description = "로그인한 유저의 알림 목록을 조회합니다. 읽지 않은 알림이 먼저 표시되고, 그 다음 최신순으로 정렬됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공", content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "403", description = "로그인 필요", content = @Content(schema = @Schema(implementation = RestResponse.class))),
    })
    @GetMapping
    public ResponseEntity<RestResponse<Page<NotificationResponse>>> getNotifications(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<NotificationResponse> notifications = notificationService.getNotifications(page, size);
        return ResponseEntity.ok(RestResponse.success(notifications));
    }

    @Operation(
            summary = "알림 상세 조회",
            description = "특정 알림의 상세 내용을 조회하고 자동으로 읽음 처리됩니다. 게시글로 이동하기 위한 boardId를 포함합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 상세 조회 성공", content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "403", description = "조회 권한 없음", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음", content = @Content(schema = @Schema(implementation = RestResponse.class))),
    })
    @GetMapping("/{notificationId}")
    public ResponseEntity<RestResponse<NotificationResponse>> getNotificationDetail(
            @PathVariable Integer notificationId) {
        NotificationResponse notification = notificationService.getNotificationsdetail(notificationId);
        return ResponseEntity.ok(RestResponse.success(notification));
    }

    @Operation(
            summary = "읽지 않은 알림 개수 조회",
            description = "로그인한 유저의 읽지 않은 알림 개수를 조회합니다. 헤더 뱃지 표시용으로 사용됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "읽지 않은 알림 개수 조회 성공", content = @Content(schema = @Schema(implementation = Integer.class))),
            @ApiResponse(responseCode = "403", description = "로그인 필요", content = @Content(schema = @Schema(implementation = RestResponse.class))),
    })
    @GetMapping("/unread")
    public ResponseEntity<RestResponse<Integer>> getUnreadNotificationCount() {
        Integer unreadCount = notificationService.getUnreadNotificationCount();
        return ResponseEntity.ok(RestResponse.success(unreadCount));
    }

}
