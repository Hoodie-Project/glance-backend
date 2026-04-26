# 배포 가이드

## 환경

- 서버: Mac Mini (on-premise)
- 도메인: `dev.partyguham.com`
- 리버스 프록시: nginx

## Docker 빌드 및 실행

```bash
# 이미지 빌드 + 컨테이너 재시작
docker compose build app && docker compose up -d app

# 전체 인프라 실행
docker compose up -d
```

## 컨테이너 구성

| 컨테이너 | 이미지 | 포트 |
|----------|--------|------|
| glance-app | glance-backend-app | 8081 → 8080 |
| glance-postgres | postgres:16-alpine | 5435 → 5432 |
| glance-redis | redis:7-alpine | 6381 → 6379 |

## nginx 설정

`/admin/` 경로는 Thymeleaf 기반 관리자 UI, `/api/` 경로는 REST API로 라우팅한다.

```nginx
location /admin/ {
    proxy_pass http://127.0.0.1:8081;
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

## 로그 확인

```bash
# 실시간 로그
docker logs -f glance-app

# 최근 50줄
docker logs glance-app --tail=50

# 특정 시간대 필터
docker logs glance-app 2>&1 | grep "14:"
```

prod 프로필에서는 컨테이너 내부 `/var/log/glance/app.log`에 날짜별 롤링으로 저장된다 (최대 30일/1GB).

## 재시작 정책

Docker 컨테이너는 `restart: unless-stopped` 정책으로 서버 재시작 시 자동으로 올라온다.
