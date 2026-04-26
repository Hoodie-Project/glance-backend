# 개발 환경 세팅

## 필수 요구사항

- Java 21
- Docker & Docker Compose

## 환경변수

`docker-compose.yml` 또는 시스템 환경변수로 설정한다.

| 변수명 | 설명 | 기본값         |
|--------|------|-------------|
| `DB_USERNAME` | PostgreSQL 사용자명 | -           |
| `DB_PASSWORD` | PostgreSQL 비밀번호 | -           |
| `REDIS_HOST` | Redis 호스트 | `localhost` |
| `REDIS_PORT` | Redis 포트 | `6379`      |
| `NAVER_CLIENT_ID` | 네이버 지도 API Client ID | -           |
| `NAVER_CLIENT_SECRET` | 네이버 지도 API Client Secret | -           |
| `ADMIN_USERNAME` | 어드민 로그인 아이디 | `admin`     |
| `ADMIN_PASSWORD` | 어드민 로그인 비밀번호 | `admin1234` |

네이버 지도 API는 [네이버 클라우드 플랫폼](https://www.ncloud.com/)에서 발급한다. Reverse Geocoding 권한이 필요하다.

## 로컬 실행

```bash
# 인프라 실행 (DB, Redis)
docker compose up -d postgres redis

# 애플리케이션 실행 (dev 프로필)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## 프로필

| 프로필 | 용도 | 로그 레벨 |
|--------|------|-----------|
| `dev` | 로컬 개발 | DEBUG (앱), SQL 출력 |
| `prod` | 운영 서버 | INFO (앱), 파일 저장 |
