package com.hoodiev.glance.report.controller;

import com.hoodiev.glance.common.dto.ErrorResponse;
import com.hoodiev.glance.common.util.ClientIpExtractor;
import com.hoodiev.glance.report.dto.ReportCreateRequest;
import com.hoodiev.glance.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Report", description = "신고 API")
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(
            summary = "신고 접수",
            description = """
                    스레드 또는 댓글을 신고합니다.

                    ### 신고 대상 (targetType)
                    | 값 | 설명 |
                    |---|---|
                    | `THREAD` | 스레드(힐끔 글) |
                    | `COMMENT` | 댓글 |

                    ### 신고 사유 (reason)
                    | 값 | 설명 |
                    |---|---|
                    | `SPAM` | 스팸/광고 |
                    | `PROFANITY` | 욕설/비방 |
                    | `ADULT_CONTENT` | 성인 콘텐츠 |
                    | `PRIVACY_VIOLATION` | 개인정보 침해 |
                    | `FRAUD` | 사기/허위정보 |
                    | `OTHER` | 기타 |

                    ### 제한
                    - 동일 IP에서 같은 대상을 중복 신고할 수 없습니다.
                    - 이미 삭제된 스레드/댓글은 신고할 수 없습니다.

                    ### 처리 방식
                    신고는 관리자 검토 후 수동으로 처리됩니다. 신고 접수 즉시 콘텐츠가 숨겨지지 않습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "신고 접수 완료"),
            @ApiResponse(responseCode = "400", description = "유효성 실패 (targetType, targetId, reason 누락 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 삭제된 대상",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "동일 IP로 이미 신고한 대상",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/api/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public void report(
            @RequestBody @Valid ReportCreateRequest request,
            HttpServletRequest httpRequest) {
        reportService.report(
                request,
                ClientIpExtractor.extract(httpRequest),
                httpRequest.getHeader("User-Agent"));
    }
}
