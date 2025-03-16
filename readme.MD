# 갓생 백엔드 API 명세서

프론트엔드 개발을 위한 API 명세서

## 1. 사용자 관리 API

### 1.1. 회원가입

- **URL**: `/api/users/signup`
- **Method**: POST
- **Request Body**:

    ```json
    {
      "email": "사용자 이메일",
      "password": "비밀번호",
      "username": "사용자 이름"
    }
    
    ```

- **Response**:

    ```json
    {
      "id": "사용자 ID",
      "email": "사용자 이메일",
      "username": "사용자 이름",
      "role": "ROLE_USER",
      "isGodMode": false
    }
    
    ```

- **Status Code**: 201 Created

### 1.2. 로그인

- **URL**: `/api/users/login`
- **Method**: POST
- **Request Body**:

    ```json
    {
      "email": "사용자 이메일",
      "password": "비밀번호"
    }
    
    ```

- **Response**:

    ```json
    {
      "accessToken": "JWT 액세스 토큰",
      "id": "사용자 ID",
      "email": "사용자 이메일",
      "username": "사용자 이름",
      "role": "ROLE_USER",
      "isGodMode": false
    }
    
    ```

- **Status Code**: 200 OK
- **참고**: 응답에는 refreshToken이 포함되지 않으며, HTTP-only 쿠키로 설정됩니다.

### 1.3. 토큰 갱신

- **URL**: `/api/users/refresh`
- **Method**: POST
- **Request Body** (쿠키에 refreshToken이 없는 경우):

    ```json
    {
      "refreshToken": "리프레시 토큰"
    }
    
    ```

- **Response**:

    ```json
    {
      "accessToken": "새로운 JWT 액세스 토큰"
    }
    
    ```

- **Status Code**: 200 OK

### 1.4. 현재 사용자 정보 조회

- **URL**: `/api/users/me`
- **Method**: GET
- **Headers**: `Authorization: Bearer {accessToken}`
- **Response**:

    ```json
    {
      "id": "사용자 ID",
      "email": "사용자 이메일",
      "username": "사용자 이름",
      "role": "ROLE_USER",
      "isGodMode": false
    }
    
    ```

- **Status Code**: 200 OK

### 1.5. 갓생 모드 업데이트

- **URL**: `/api/users/god-mode`
- **Method**: PUT
- **Headers**: `Authorization: Bearer {accessToken}`
- **Request Body**:

    ```json
    {
      "isGodMode": true
    }
    
    ```

- **Response**:

    ```json
    {
      "id": "사용자 ID",
      "email": "사용자 이메일",
      "username": "사용자 이름",
      "role": "ROLE_USER",
      "isGodMode": true
    }
    
    ```

- **Status Code**: 200 OK

## 2. 강의 관리 API

### 2.1. 강의 생성 (일반)

- **URL**: `/api/lectures`
- **Method**: POST
- **Headers**: `Authorization: Bearer {accessToken}`
- **Request Body**:

    ```json
    {
      "title": "강의 제목",
      "description": "강의 설명",
      "sourceType": "YOUTUBE 또는 UPLOAD",
      "videoUrl": "비디오 URL"
    }
    
    ```

- **Response**:

    ```json
    {
      "id": "강의 ID",
      "title": "강의 제목",
      "description": "강의 설명",
      "sourceType": "YOUTUBE 또는 UPLOAD",
      "videoUrl": "비디오 URL",
      "status": "PROCESSING",
      "createdAt": "생성 시간",
      "updatedAt": "업데이트 시간"
    }
    
    ```

- **Status Code**: 201 Created

### 2.2. YouTube 강의 생성

- **URL**: `/api/lectures/youtube`
- **Method**: POST
- **Headers**: `Authorization: Bearer {accessToken}`
- **Request Body**:

    ```json
    {
      "title": "강의 제목",
      "description": "강의 설명",
      "sourceType": "YOUTUBE",
      "videoUrl": "YouTube URL"
    }
    
    ```

- **Response**: 2.1과 동일
- **Status Code**: 201 Created

### 2.3. 파일 업로드 강의 생성

- **URL**: `/api/lectures/upload`
- **Method**: POST
- **Headers**: `Authorization: Bearer {accessToken}`
- **Content-Type**: `multipart/form-data`
- **Form Data**:
    - `file`: 강의 파일
    - `title`: 강의 제목
    - `description`: 강의 설명 (선택적)
- **Response**: 2.1과 동일
- **Status Code**: 201 Created

### 2.4. 강의 목록 조회

- **URL**: `/api/lectures`
- **Method**: GET
- **Headers**: `Authorization: Bearer {accessToken}`
- **Query Parameters**:
    - `page`: 페이지 번호 (기본값: 0)
    - `size`: 페이지 크기 (기본값: 10)
- **Response**:

    ```json
    {
      "content": [
        {
          "id": "강의 ID",
          "title": "강의 제목",
          "description": "강의 설명",
          "sourceType": "YOUTUBE 또는 UPLOAD",
          "videoUrl": "비디오 URL",
          "status": "PROCESSING 또는 COMPLETED",
          "createdAt": "생성 시간",
          "updatedAt": "업데이트 시간"
        }
      ],
      "pageable": { /* 페이지 정보 */ },
      "totalElements": "전체 강의 수",
      "totalPages": "전체 페이지 수",
      "last": "마지막 페이지 여부",
      "size": "페이지 크기",
      "number": "현재 페이지 번호",
      "sort": { /* 정렬 정보 */ },
      "numberOfElements": "현재 페이지 강의 수",
      "first": "첫 페이지 여부",
      "empty": "비어있는지 여부"
    }
    
    ```

- **Status Code**: 200 OK

### 2.5. 강의 상세 조회

- **URL**: `/api/lectures/{lectureId}`
- **Method**: GET
- **Headers**: `Authorization: Bearer {accessToken}`
- **Response**:

    ```json
    {
      "id": "강의 ID",
      "title": "강의 제목",
      "description": "강의 설명",
      "sourceType": "YOUTUBE 또는 UPLOAD",
      "videoUrl": "비디오 URL",
      "status": "PROCESSING 또는 COMPLETED",
      "transcript": "강의 스크립트",
      "summary": "강의 요약",
      "expectedQuestions": "예상 질문",
      "studyPlan": "학습 계획",
      "createdAt": "생성 시간",
      "updatedAt": "업데이트 시간"
    }
    
    ```

- **Status Code**: 200 OK

## 3. 채팅 API

### 3.1. 질문 전송

- **URL**: `/api/chat/lectures/{lectureId}/questions`
- **Method**: POST
- **Headers**: `Authorization: Bearer {accessToken}`
- **Request Body**:

    ```json
    {
      "question": "질문 내용",
      "tone": "normal 또는 다른 말투 (선택적)"
    }
    
    ```

- **Response**:

    ```json
    {
      "questionId": "질문 ID",
      "answerId": "답변 ID",
      "question": "질문 내용",
      "answer": "AI 답변 내용",
      "timestamp": "답변 시간"
    }
    
    ```

- **Status Code**: 200 OK

### 3.2. 채팅 기록 조회

- **URL**: `/api/chat/lectures/{lectureId}/history`
- **Method**: GET
- **Headers**: `Authorization: Bearer {accessToken}`
- **Response**:

    ```json
    {
      "lectureId": "강의 ID",
      "lectureTitle": "강의 제목",
      "messages": [
        {
          "questionId": "질문 ID",
          "answerId": "답변 ID",
          "question": "질문 내용",
          "answer": "AI 답변 내용",
          "timestamp": "답변 시간"
        }
      ]
    }
    
    ```

- **Status Code**: 200 OK

## 4. AI 콜백 API (프론트엔드에서 직접 사용하지 않음)

### 4.1. AI 처리 완료 콜백

- **URL**: `/api/ai/callback/complete`
- **Method**: POST
- **Request Body**:

    ```json
    {
      "lecture_id": "강의 ID",
      "task_id": "AI 작업 ID",
      "transcribed_text": "변환된 텍스트",
      "summary_text": "요약 텍스트",
      "quiz_text": "퀴즈 텍스트",
      "study_plan": "학습 계획"
    }
    
    ```

- **Response**:

    ```json
    {
      "status": "success"
    }
    
    ```

- **Status Code**: 200 OK

## 오류 응답 형식

모든 API는 오류 발생 시 다음과 같은 형식으로 응답합니다:

```json
{
  "error": "오류 메시지",
  "message": "상세 오류 메시지 (선택적)"
}

```

일반적인 HTTP 상태 코드:

- 400 Bad Request: 잘못된 요청
- 401 Unauthorized: 인증 실패
- 403 Forbidden: 권한 없음
- 404 Not Found: 리소스를 찾을 수 없음
- 500 Internal Server Error: 서버 내부 오류

## 인증 요구사항

- `/api/users/signup`와 `/api/users/login`을 제외한 모든 API는 인증이 필요합니다.
- 인증은 요청 헤더에 `Authorization: Bearer {accessToken}` 형식으로 JWT 토큰을 포함해야 합니다.
- 토큰이 만료된 경우 `/api/users/refresh` API를 사용하여 새 액세스 토큰을 발급받아야 합니다.