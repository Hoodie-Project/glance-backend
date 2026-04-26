# 데이터베이스

## 테이블 관계

```
regions ──< threads ──< comments
                  └──< thread_likes
                  └──< comment_likes (comment_id 기반)
```

## 테이블 상세

### threads

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| nickname | VARCHAR(20) | 작성자 닉네임 |
| title | VARCHAR(30) | 제목 |
| content | VARCHAR(500) | 내용 |
| latitude | DOUBLE | 위도 (WGS84) |
| longitude | DOUBLE | 경도 (WGS84) |
| region_id | FK | regions 참조 |
| password | VARCHAR | BCrypt 인코딩 |
| gender | ENUM | ALL / MALE / FEMALE |
| like_count | INT | 기본값 0 |
| comment_count | INT | 기본값 0 |
| client_ip | VARCHAR(45) | 작성자 IP (불변) |
| user_agent | VARCHAR(512) | 작성자 기기 정보 (불변) |
| created_at | TIMESTAMP | 생성일시 (불변) |
| deleted_at | TIMESTAMP | 소프트 삭제일시, null이면 활성 |

**ElementCollection**
- `tags` — 해시태그 목록 (최대 5개), 별도 테이블 저장, 인덱스 있음
- `animal_looks` — 동물상 (DOG, CAT, FOX, RABBIT, DINOSAUR, DEER, WOLF, HAMSTER, BEAR)
- `vibe_styles` — 분위기 (COLD_HANDSOME, COLD_BEAUTY, WARM_HANDSOME, WARM_BEAUTY, HARMLESS, DECADENT, CLASSIC_HANDSOME, CLASSIC_BEAUTY, FRESH)

### comments

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| thread_id | FK | threads 참조 |
| nickname | VARCHAR(20) | |
| content | VARCHAR(300) | |
| password | VARCHAR | BCrypt 인코딩 |
| like_count | INT | 기본값 0 |
| created_at | TIMESTAMP | 불변 |

### thread_likes

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| thread_id | BIGINT | |
| ip_address | VARCHAR(45) | |
| created_at | TIMESTAMP | |

Unique Constraint: `(thread_id, ip_address)` — IP당 1회 좋아요 보장

### comment_likes

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| comment_id | BIGINT | |
| ip_address | VARCHAR(45) | |
| created_at | TIMESTAMP | |

Unique Constraint: `(comment_id, ip_address)`

### regions

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | |
| legal_code | VARCHAR(20) UNIQUE | 법정동 코드 |
| sido | VARCHAR(20) | 시/도 |
| sigungu | VARCHAR(30) | 시/군/구 |
| dong | VARCHAR(30) | 동/읍/면 |
| center_lat | DOUBLE | 행정동 중심 위도 |
| center_lng | DOUBLE | 행정동 중심 경도 |
