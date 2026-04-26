# glance-backend

위치 기반 익명 커뮤니티 앱 Glance의 백엔드 서버.

## 기술 스택

Java 21 · Spring Boot 4.0.5 · PostgreSQL 16 · Redis 7 · Docker

## 문서

- [개발 환경 세팅](docs/setup.md)
- [아키텍처](docs/architecture.md)
- [데이터베이스](docs/database.md)
- [API 가이드](docs/api.md)
- [배포 가이드](docs/deployment.md)
- [PRD](docs/prd.md)

## 빠른 시작

```bash
docker compose up -d postgres redis
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Swagger UI: `http://localhost:8080/swagger-ui/index.html`
