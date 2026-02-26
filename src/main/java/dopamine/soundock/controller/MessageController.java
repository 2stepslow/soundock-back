package dopamine.soundock.controller;


import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.dto.request.SendMessageRequest;
import dopamine.soundock.dto.response.MessageResponse;
import dopamine.soundock.enums.MessageType;
import dopamine.soundock.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/users/message")
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(
            summary = "메세지 전송",
            description = "특정 유저에게 메세지를 전송합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메세지 전송 성공", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "403", description = "로그인 필요", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "받는 사용자를 찾을 수 없음", content = @Content(schema = @Schema(implementation = RestResponse.class))),
    })
    @PostMapping("/{userId}")
    public ResponseEntity<RestResponse<Void>> sendMessage(
            @PathVariable Integer userId,
            @Valid @RequestBody SendMessageRequest request) {
        messageService.sendMessage(userId, request);
        return ResponseEntity.ok(RestResponse.success("메세지 전송이 완료되었습니다."));
    }

    @Operation(
            summary = "메세지 리스트 조회",
            description = "받은 메세지 또는 보낸 메세지 목록을 조회합니다. type=received(받은 메세지), type=sent(보낸 메세지)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메세지 목록 조회", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "로그인 필요", content = @Content(schema = @Schema(implementation = RestResponse.class))),
    })
    @GetMapping
    public ResponseEntity<RestResponse<List<MessageResponse>>> getMessages(
            @RequestParam MessageType type) {
        List<MessageResponse> messages = messageService.getMessages(type);
        return ResponseEntity.ok(RestResponse.success(messages));
    }

    @Operation(
            summary = "안 읽은 메세지 개수 조회",
            description = "로그인한 유저의 읽지 않은 메세지 개수를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "읽지 않은 메세지 개수 조회", content = @Content(schema = @Schema(implementation = Integer.class))),
            @ApiResponse(responseCode = "403", description = "로그인 필요", content = @Content(schema = @Schema(implementation = RestResponse.class))),
    })
    @GetMapping("/unread")
    public ResponseEntity<RestResponse<Integer>> getUnreadMessageCount() {
        Integer count = messageService.getUnreadMessageCount();
        return ResponseEntity.ok(RestResponse.success(count));
    }

    @Operation(
            summary = "메세지 상세 조회",
            description = "특정 메세지의 상세 내용을 조회하고, 받은 메세지인 경우 자동으로 읽음 처리됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메세지 상세 조회", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "조회 권한 없음", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "메세지를 찾을 수 없음", content = @Content(schema = @Schema(implementation = RestResponse.class))),
    })
    @GetMapping("/{messageId}")
    public ResponseEntity<RestResponse<MessageResponse>> getMessageDetail(
            @PathVariable Integer messageId) {
        MessageResponse message = messageService.getMessageDetail(messageId);
        return ResponseEntity.ok(RestResponse.success(message));
    }
}
