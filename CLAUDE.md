# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

위치 기반 익명 커뮤니티 앱 **Glance**의 백엔드. 회원가입 없이 비밀번호만으로 스레드/댓글을 작성하며, 작성 위치 좌표를 네이버 Reverse Geocoding으로 행정동 정보로 변환해 저장한다.

## 주요 명령어

```bash
# 인프라만 실행 (로컬 개발)
docker compose up -d postgres redis

# 앱 실행 (dev 프로필)
./gradlew bootRun --args='--spring.profiles.active=dev'

# 전체 빌드
./gradlew build

# 전체 테스트
./gradlew test

# 단일 테스트 클래스
./gradlew test --tests "com.hoodiev.glance.thread.service.ThreadServiceIntegrationTest"

# 단일 테스트 메서드
./gradlew test --tests "com.hoodiev.glance.thread.service.ThreadServiceIntegrationTest.메서드명"

# Docker Compose (Make 사용)
make dev        # dev 환경 실행
make build-dev  # dev 환경 재빌드 후 실행
make prod       # prod 환경 실행
```

Swagger UI: `http://localhost:8080/swagger-ui/index.html` (admin 계정으로 HTTP Basic 인증)

## 아키텍처

### 패키지 구조

도메인 중심으로 패키지를 구성한다. 각 도메인은 `controller → service → repository → entity / dto` 레이어를 갖는다.

```
com.hoodiev.glance
├── thread/    # 핵심 도메인: 스레드 CRUD, 좋아요, 지도 핀, 피드
├── comment/   # 댓글 CRUD, 좋아요
├── region/    # 행정동 정보 (네이버 API 연동 결과 저장)
├── report/    # 신고 (스레드/댓글 대상 폴리모픽)
├── admin/     # Thymeleaf 기반 관리자 UI
├── common/    # BusinessException 계층, 공통 DTO, 유틸리티
└── config/    # SecurityConfig, RedisConfig, OpenApiConfig
```

### 보안 구조 (SecurityFilterChain 3개, @Order로 우선순위 지정)

| Order | 경로 | 인증 방식 |
|-------|------|-----------|
| 1 | `/admin/**` | Form Login (세션) |
| 2 | `/api/swagger-ui/**`, `/api/v3/api-docs/**` | HTTP Basic |
| 3 | `/api/**` | 인증 없음 (CORS 허용) |

관리자 계정은 InMemoryUserDetailsManager로 관리 (`ADMIN_USERNAME`, `ADMIN_PASSWORD` 환경변수).

### 주요 설계 결정

**익명 비밀번호 인증**: 스레드/댓글 작성 시 비밀번호를 BCrypt로 저장. 미입력 시 서버가 8자 랜덤 비밀번호 자동 생성 후 응답에 포함. 수정/삭제 시 비밀번호 검증.

**소프트 삭제**: 스레드만 `deleted_at` 소프트 삭제 (어드민에서 복구 가능). 댓글은 하드 삭제.

**IP 기반 레이트 리밋**: 스레드 생성 시 Redis로 IP당 1분 최대 3건 제한 (`RateLimiter` 유틸리티).

**IP 기반 좋아요**: `thread_likes` / `comment_likes` 테이블에 `(target_id, ip_address)` Unique Constraint로 중복 방지. 토글 방식.

**신고 폴리모픽**: `reports` 테이블은 `target_type (THREAD/COMMENT)` + `target_id`로 참조하며 FK 없음. 대상 삭제 후에도 신고 이력 보존.

## 테스트

통합 테스트는 Testcontainers로 PostgreSQL 16 + Redis 7 컨테이너를 자동 실행한다. 모든 통합 테스트는 `AbstractIntegrationTest`를 상속한다. `@ActiveProfiles("test")`가 적용되며 별도 인프라 설치 없이 실행 가능하다.

## 환경변수

필수: `DB_USERNAME`, `DB_PASSWORD`, `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`  
선택: `REDIS_HOST` (기본 localhost), `REDIS_PORT` (기본 6379), `ADMIN_USERNAME` (기본 admin), `ADMIN_PASSWORD` (기본 admin1234)
