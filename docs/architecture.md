# 아키텍처

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 21, Kotlin |
| Framework | Spring Boot 4.0.5 |
| ORM | Spring Data JPA (Hibernate) |
| DB | PostgreSQL 16 |
| Cache | Redis 7 |
| 보안 | Spring Security, BCrypt |
| 관리자 UI | Thymeleaf |
| API 문서 | Swagger (springdoc-openapi) |
| 빌드 | Gradle (Kotlin DSL) |

## 패키지 구조

```
com.hoodiev.glance
├── thread/          # 스레드 도메인 (핵심)
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
├── comment/         # 댓글 도메인
├── region/          # 지역 정보 도메인
├── admin/           # 관리자 (Thymeleaf)
│   ├── controller/
│   └── service/
├── common/          # 공통
│   ├── exception/   # 예외 처리
│   ├── dto/         # 공통 DTO
│   └── util/        # 유틸리티
└── config/          # 설정 (Security, OpenAPI 등)
```

## 예외 처리 구조

모든 비즈니스 예외는 `BusinessException`을 상속한다. `GlobalExceptionHandler`가 이를 잡아 일관된 `ErrorResponse`를 반환한다.

```
BusinessException (abstract)
├── EntityNotFoundException     → 404 ENTITY_NOT_FOUND
├── InvalidPasswordException    → 403 INVALID_PASSWORD
├── RateLimitExceededException  → 429 RATE_LIMIT_EXCEEDED
└── BoundingBoxTooLargeException → 400 BOUNDING_BOX_TOO_LARGE

ErrorResponse { status, code, message, timestamp }
```

새 예외 추가 시 `BusinessException`을 상속하고 `HttpStatus`와 `code`만 지정하면 Handler 수정 없이 동작한다.

## 주요 설계 결정

**익명 게시판 (비밀번호 기반)**
회원가입 없이 스레드/댓글 작성 시 비밀번호를 설정한다. 미입력 시 서버가 8자 랜덤 비밀번호를 자동 생성해 응답에 포함한다. 수정/삭제 시 비밀번호 검증.

**소프트 삭제**
스레드는 `deleted_at`을 기록하는 소프트 삭제 방식이다. 어드민에서 복구 가능하다. 댓글은 하드 삭제한다.

**IP 기반 레이트 리밋**
스레드 생성 시 Redis를 이용해 IP당 1분에 최대 3건으로 제한한다.

**IP 기반 좋아요**
스레드/댓글 좋아요는 IP당 1회로 제한한다. DB Unique Constraint로 보장하며 토글 방식으로 동작한다.

**위치 기반 지도**
네이버 Reverse Geocoding으로 좌표 → 지역 정보를 자동 변환해 저장한다. 지도 핀 조회 시 범위 제한(±0.072°)을 초과하면 400 에러를 반환한다.
