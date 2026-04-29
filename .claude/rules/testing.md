# 테스트

- 통합 테스트는 `AbstractIntegrationTest`를 상속한다 (Testcontainers로 PostgreSQL + Redis 자동 실행).
- DB 목(Mock)을 사용하지 않는다. 실제 컨테이너를 사용한다.
- 단위 테스트는 순수 Java/Kotlin으로, Spring Context 없이 작성한다.
- 테스트 메서드명은 한국어로 작성해도 된다 (예: `스레드_생성_성공()`).
- 테스트는 given / when / then 구조로 작성한다.
