package dopamine.soundock.controller;

import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.dto.response.AnnouncementResponse;
import dopamine.soundock.enums.AnnounceType;
import dopamine.soundock.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/announcement")
@RestController
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(
            summary = "공지사항 목록 조회",
            description = "공지사항 게시판 목록을 페이지 단위로 조회합니다. (페이지당 15개)<br>"
                    + "비회원/일반 사용자 접근 가능, isActive=true만 조회합니다.<br>"
                    + "정렬: priority=0 우선 노출 후 createdAt 내림차순"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "공지사항 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = AnnouncementResponse.class))
            )
    })

    @GetMapping
    public ResponseEntity<RestResponse<Page<AnnouncementResponse>>> getAnnouncements(
            @RequestParam(required = false, defaultValue = "0") Integer page
    ) {
        Page<AnnouncementResponse> responses = announcementService.getAnnouncements(page);
        return ResponseEntity.ok(RestResponse.success(responses));
    }


    @Operation(
            summary = "공지사항 상세 조회",
            description = "announceId와 일치하는 공지사항의 상세 내용을 조회합니다.<br>"
                    + "비회원/일반 사용자 접근 가능, isActive=true만 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "공지사항 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = AnnouncementResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "공지사항이 없거나 비활성화된 경우",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            )
    })

    @GetMapping("/{announceId}")
    public ResponseEntity<RestResponse<AnnouncementResponse>> getAnnouncementDetail(
            @PathVariable(required = true) Integer announceId
    ) {
        AnnouncementResponse response = announcementService.getAnnouncementDetail(announceId);
        return ResponseEntity.ok(RestResponse.success(response));
    }


    @Operation(
            summary = "푸터용 공지사항 조회 (타입별 priority=0)",
            description = "announceType에 해당하는 공지사항 중 priority=0 인 게시글을 1개 조회합니다.<br>"
                    + "푸터에서 클릭 시 해당 게시글로 바로 이동하기 위한 API입니다.<br>"
                    + "비회원/일반 사용자 접근 가능, isActive=true만 조회합니다.<br>"
                    + "예: /api/announcement/announce/GENERAL"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "타입별 priority=0 공지 조회 성공",
                    content = @Content(schema = @Schema(implementation = AnnouncementResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 타입의 priority=0 공지사항이 없거나 비활성화된 경우",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            )
    })

    @GetMapping("/announce/{announceType}")
    public ResponseEntity<RestResponse<AnnouncementResponse>> getPinnedAnnouncementByType(
            @PathVariable(required = true) AnnounceType announceType
    ) {
        AnnouncementResponse response = announcementService.getPinnedAnnouncementByType(announceType);
        return ResponseEntity.ok(RestResponse.success(response));
    }
}