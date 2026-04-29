# 예외 처리

- 모든 비즈니스 예외는 `BusinessException`을 상속한다.
- 새 예외 추가 시 `BusinessException` 상속 후 `HttpStatus`와 `code`만 지정하면 `GlobalExceptionHandler`가 자동 처리한다.
- 서비스 레이어에서 예외를 직접 `try-catch`로 삼키지 않는다. 복구 불가능한 예외는 위로 전파한다.
- 404는 `EntityNotFoundException`, 403은 `InvalidPasswordException`, 429는 `RateLimitExceededException`을 사용한다.
- 컨트롤러에서 예외를 직접 잡지 않는다. `GlobalExceptionHandler`에 위임한다.
