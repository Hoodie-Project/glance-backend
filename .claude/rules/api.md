# API 설계

- 모든 API 엔드포인트는 `/api/` 접두사를 붙인다.
- HTTP 메서드: 조회 GET, 생성 POST, 수정 PUT/PATCH, 삭제 DELETE.
- 생성 성공은 `201 Created`, 삭제 성공은 `204 No Content`를 반환한다.
- 모든 컨트롤러 메서드에 `@Operation`, `@ApiResponses` Swagger 어노테이션을 작성한다.
- 에러 응답은 항상 `ErrorResponse` 형식을 따른다.
- URL은 복수 명사로 구성한다 (`/threads`, `/comments`).
- 페이지네이션: 무한스크롤은 Cursor 방식, 일반 목록은 `Pageable` 방식.
