# Godsaeng-Backend: Spring Boot 기반 학습 도우미 백엔드 서버

## 프로젝트 소개
Godsaeng-Backend는 AI 기반 학습 도우미 서비스의 백엔드 시스템으로, 사용자 관리, 강의 관리, 채팅 기능 등을 제공합니다. Spring Boot 기반으로 개발되었으며, AI 서버와 연동하여 강의 자료를 분석하고 사용자에게 맞춤형 학습 경험을 제공합니다.

## 주요 기능
1. **사용자 관리**
   - 회원가입 및 로그인
   - JWT 기반 인증 및 권한 관리
   - 사용자 프로필 관리
   - 갓생 모드(프리미엄 기능) 지원

2. **강의 관리**
   - 다양한 형식의 강의 자료 업로드 및 처리
   - YouTube URL을 통한 강의 생성
   - 강의 목록 조회 및 페이지네이션
   - 강의 상세 정보 조회 및 삭제

3. **AI 연동 기능**
   - AI 서버와의 통신을 통한 강의 자료 분석
   - 강의 요약, 퀴즈, 학습 계획 데이터 관리
   - 비동기 처리 및 콜백 시스템

4. **채팅 시스템**
   - 강의 내용 기반 질의응답
   - 채팅 기록 저장 및 조회
   - 다양한 말투 설정 지원

## 시스템 구조

```
godsaeng-backend/
├── src/main/java/com/example/godsaengbackend/
│   ├── config/                # 설정 클래스
│   │   ├── SecurityConfig.java    # 보안 설정
│   │   └── WebConfig.java         # 웹 설정
│   ├── controller/            # API 컨트롤러
│   │   ├── AICallbackController.java  # AI 콜백 처리
│   │   ├── ChatController.java        # 채팅 API
│   │   ├── LectureController.java     # 강의 관리 API
│   │   └── UserController.java        # 사용자 관리 API
│   ├── dto/                   # 데이터 전송 객체
│   ├── entity/                # 데이터베이스 엔티티
│   │   ├── ChatMessage.java       # 채팅 메시지
│   │   ├── Lecture.java           # 강의
│   │   └── User.java              # 사용자
│   ├── jwt/                   # JWT 인증 관련
│   ├── repository/            # 데이터 접근 계층
│   ├── service/               # 비즈니스 로직
│   │   ├── AIService.java         # AI 연동 서비스
│   │   ├── ChatService.java       # 채팅 서비스
│   │   ├── LectureService.java    # 강의 서비스
│   │   └── UserService.java       # 사용자 서비스
│   └── GodsaengBackendApplication.java  # 애플리케이션 진입점
└── src/main/resources/
    └── application.properties  # 애플리케이션 설정
```

## 기술 스택
- **언어 및 프레임워크**: Java 17, Spring Boot 3.x
- **보안**: Spring Security, JWT
- **데이터베이스**: MySQL, JPA/Hibernate
- **빌드 도구**: Gradle
- **API 문서화**: Swagger/OpenAPI
- **테스트**: JUnit, Mockito

## 시스템 요구사항
- JDK 17 이상
- MySQL 8.0 이상
- Gradle 7.x 이상
- 최소 4GB RAM
- 10GB 이상의 디스크 공간

## 설치 방법

1. **저장소 클론**
```bash
git clone https://github.com/username/godsaeng-backend.git
cd godsaeng-backend
```

2. **데이터베이스 설정**
```
# application.properties 또는 application.yml 파일 수정
spring.datasource.url=jdbc:mysql://localhost:3306/godsaeng
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. **JWT 시크릿 키 설정**
```
# application.properties 또는 application.yml 파일에 추가
jwt.secret=your_jwt_secret_key_here
jwt.expiration=3600000
jwt.refresh-expiration=604800000
```

4. **AI 서버 URL 설정**
```
# application.properties 또는 application.yml 파일에 추가
ai.service.url=http://localhost:5000
```

5. **빌드 및 실행**
```bash
./gradlew build
java -jar build/libs/godsaeng-backend-0.0.1-SNAPSHOT.jar
```

## AI 서버 연동 구조

이 시스템은 별도의 AI 서버와 통신하여 강의 자료를 처리합니다. 주요 통신 흐름은 다음과 같습니다:

1. **강의 자료 업로드**
   - 사용자가 강의 자료(비디오, 오디오, PDF 등)를 업로드
   - 백엔드 서버는 강의 객체를 생성하고 AI 서버로 파일 전송
   - AI 서버는 작업 ID(task_id)를 반환하고 비동기 처리 시작

2. **처리 상태 관리**
   - 백엔드 서버는 강의 상태를 PROCESSING으로 설정
   - AI 서버의 처리가 완료될 때까지 대기

3. **콜백 처리**
   - AI 서버가 처리를 완료하면 백엔드 서버의 콜백 URL로 결과 전송
   - 백엔드 서버는 강의 객체를 업데이트하고 상태를 COMPLETED로 변경

4. **질의응답 처리**
   - 사용자가 강의 내용에 대한 질문을 입력
   - 백엔드 서버는 질문을 AI 서버로 전송하고 응답을 받아 사용자에게 제공



### 주요 API 엔드포인트

#### 사용자 관리
- `POST /api/users/signup`: 회원가입
- `POST /api/users/login`: 로그인
- `POST /api/users/refresh`: 토큰 갱신
- `GET /api/users/me`: 현재 사용자 정보 조회
- `PUT /api/users/god-mode`: 갓생 모드 업데이트

#### 강의 관리
- `POST /api/lectures`: 강의 생성
- `POST /api/lectures/youtube`: YouTube 강의 생성
- `POST /api/lectures/upload`: 파일 업로드를 통한 강의 생성
- `GET /api/lectures`: 강의 목록 조회
- `GET /api/lectures/{lectureId}`: 강의 상세 정보 조회
- `DELETE /api/lectures/{lectureId}`: 강의 삭제

#### 채팅
- `POST /api/chat/lectures/{lectureId}/questions`: 질문 전송
- `GET /api/chat/lectures/{lectureId}/history`: 채팅 기록 조회

#### AI 콜백
- `POST /api/ai/callback/complete`: AI 처리 완료 콜백

## 데이터 모델

### User (사용자)
- id: Long (PK)
- email: String (유니크)
- password: String (암호화)
- username: String
- role: String (권한)
- isGodMode: Boolean (프리미엄 사용자 여부)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

### Lecture (강의)
- id: Long (PK)
- user: User (FK)
- title: String
- description: String
- studyPlan: String
- sourceType: Enum (YOUTUBE, UPLOAD)
- videoUrl: String
- transcript: String (강의 스크립트)
- summary: String (요약)
- expectedQuestions: String (예상 질문)
- embeddingSynced: Boolean
- vectorDbId: String
- status: Enum (PROCESSING, COMPLETED, FAILED)
- taskId: String
- remainingDays: Integer
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

### ChatMessage (채팅 메시지)
- id: Long (PK)
- lecture: Lecture (FK)
- user: User (FK, 선택적)
- role: Enum (USER, ASSISTANT)
- content: String
- createdAt: LocalDateTime
- parentId: Long (질문-답변 연결)

## 보안 및 인증

- JWT 기반 인증 시스템 구현
- 액세스 토큰과 리프레시 토큰 분리
- 비밀번호 암호화 저장 (BCrypt)
- CORS 설정을 통한 허용된 출처만 접근 가능
- Spring Security를 통한 엔드포인트 보호

## 오류 처리

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

