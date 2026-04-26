# API 가이드

모든 API는 인증 없이 사용 가능하다. 상세 스펙은 Swagger에서 확인한다.

**Swagger UI**: `https://dev.partyguham.com/swagger-ui/index.html`

## 에러 응답 형식

```json
{
  "status": 404,
  "code": "ENTITY_NOT_FOUND",
  "message": "스레드를 찾을 수 없습니다.",
  "timestamp": "2026-04-27T13:00:00"
}
```

| code | status | 설명 |
|------|--------|------|
| `ENTITY_NOT_FOUND` | 404 | 스레드/댓글 없음 |
| `INVALID_PASSWORD` | 403 | 비밀번호 불일치 |
| `RATE_LIMIT_EXCEEDED` | 429 | IP 레이트 리밋 초과 |
| `BOUNDING_BOX_TOO_LARGE` | 400 | 지도 조회 범위 초과 |
| `VALIDATION_FAILED` | 400 | 요청 값 유효성 실패 |
| `INTERNAL_SERVER_ERROR` | 500 | 서버 오류 |

## 엔드포인트 목록

### Thread

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/threads` | 스레드 생성 |
| GET | `/api/threads/feed` | 전체 피드 (cursor 기반) |
| GET | `/api/threads/feed/nearby` | 내 주변 피드 |
| GET | `/api/threads/search` | 태그 검색 |
| GET | `/api/threads/map/pins` | 지도 핀 조회 |
| GET | `/api/threads/map/dong` | 지도 동 마커 조회 |
| GET | `/api/threads/{id}` | 스레드 상세 |
| POST | `/api/threads/{id}/likes` | 좋아요 토글 |
| DELETE | `/api/threads/{id}` | 스레드 삭제 |

### Comment

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/threads/{threadId}/comments` | 댓글 작성 |
| DELETE | `/api/threads/{threadId}/comments/{commentId}` | 댓글 삭제 |
| POST | `/api/threads/{threadId}/comments/{commentId}/likes` | 댓글 좋아요 토글 |

## 주요 제약사항

- 스레드 생성: **IP당 1분에 최대 3건**
- 지도 핀 조회: 위도/경도 범위 각 **0.072° 이하**
- 비밀번호 미입력 시 서버가 자동 생성, 응답의 `password` 필드에 포함
