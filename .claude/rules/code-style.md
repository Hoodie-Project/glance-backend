# 코드 스타일

## Java / Kotlin 공통
- 의존성 주입은 생성자 주입만 사용한다 (`@Autowired` 필드 주입 금지).
- `@RequiredArgsConstructor` (Lombok)로 생성자를 자동 생성한다.
- 매직 넘버는 `private static final` 상수로 추출한다.
- 불필요한 주석은 달지 않는다. 코드 자체로 의도가 드러나게 네이밍한다.

## 레이어 책임
- **Controller**: 요청/응답 변환, 유효성 검사(`@Valid`), IP 추출. 비즈니스 로직 없음.
- **Service**: 모든 비즈니스 로직. 기본적으로 `@Transactional(readOnly = true)`, 쓰기 메서드에만 `@Transactional`.
- **Repository**: 쿼리만. 비즈니스 판단 없음.
- **Entity**: 상태 변경 메서드는 엔티티 안에 둔다 (Setter 대신 의미 있는 메서드).

## DTO
- Request/Response DTO는 `record`로 선언한다.
- Entity를 컨트롤러 응답으로 직접 반환하지 않는다.
- DTO 이름: `XxxRequest`, `XxxResponse`, `XxxCreateRequest` 등 용도를 명시한다.
