# Soundock

음악을 좋아하는 사람들이 자기만의 플레이리스트를 공유하고, 마음에 드는 게시글에 후원까지 할 수 있는 커뮤니티 플랫폼입니다.

## 개요

Soundock은 음악 커뮤니티 백엔드 REST API 서버입니다. 
카테고리별 게시판과 댓글, YouTube 플레이리스트 공유, 가상 재화(Pop)를 활용한 후원 시스템, 토스 페이먼츠 결제 연동 등 커뮤니티 운영에 필요한 기능을 제공합니다. Google OAuth2와 패스워드리스 로그인을 지원하며, 기상청 API를 연동해 지역별 날씨 정보(습도)도 함께 보여줍니다.

4명의 팀원이 2026년 1월부터 3월까지 약 3개월간 함께 개발했습니다.

## 기술 스택

- **Java**: 21
- **Spring Boot**: 3.5.9
- **Spring Security**: JWT, OAuth2 (Google)
- **Spring Data JPA**: MySQL
- **Redis**: 토큰 관리
- **AWS S3**: 파일 업로드
- **Toss Payments**: 결제 연동
- **YouTube Data API**: 플레이리스트 연동
- **기상청 단기예보 API**: 날씨 정보
- **Swagger**: SpringDoc OpenAPI
- **Docker**: 컨테이너 배포
- **AWS**: EC2, ALB

## 주요 기능

### 게시판 & 커뮤니티
- 카테고리별 게시판 (Showcase / Playlists / Spotlight / Community / Reviews)
- 게시글 CRUD, 좋아요, 조회수, 인기글
- 댓글 & 대댓글, 댓글 좋아요
- 키워드 검색
- 파일 첨부 (S3 업로드, MIME 타입 검증)

### 플레이리스트
- Google OAuth2 연동을 통한 YouTube 계정 인증
- YouTube 플레이리스트 조회 및 게시글에 공유

### Pop 후원 시스템
- 가상 재화(Pop) 충전 (토스 페이먼츠 결제 연동)
- Spotlight 게시글에 Pop 후원
- 후원 취소 요청 & 관리자 승인 처리
- 정산 내역 조회

### 사용자 & 인증
- 이메일 회원가입 (인증 메일 발송)
- JWT 기반 인증 (Access Token + Refresh Token)
- 패스워드리스 로그인 지원
- 마이페이지 (프로필, 내 게시글, 좋아요, 댓글 관리)

### 알림 & 메시지
- 활동 알림 (댓글, 좋아요, 후원 등)
- 사용자 간 1:1 메시지

### 관리자
- 공지사항 관리
- 사용자 문의 관리
- 후원 취소 승인 / 정산 처리

### 날씨
- 기상청 단기예보 API 연동
- 지역별 습도 정보 제공(초단기실황)

## API 엔드포인트

### 인증

| Method | Endpoint | 설명 |
|:------:|----------|-----|
| `POST` | `/api/auth/signup` | 회원가입 후 인증 메일 발송 |
| `POST` | `/api/auth/login` | 이메일/비밀번호 로그인 |
| `POST` | `/api/auth/logout` | 로그아웃, 토큰 무효화 |
| `POST` | `/api/passwordless/login-trigger` | 패스워드리스 로그인 요청 |
| `GET`  | `/api/passwordless/result` | 패스워드리스 인증 결과 확인 |

### 게시판

| Method | Endpoint | 설명 |
|:------:|----------|-----|
| `GET`  | `/api/boards` | 카테고리/검색어/정렬 조건으로 게시글 목록 조회 |
| `POST` | `/api/boards` | 게시글 작성 (파일 첨부, 플레이리스트 연결 가능) |
| `PUT`  | `/api/boards/{boardId}` | 게시글 수정 |
| `DELETE` | `/api/boards/{boardId}` | 게시글 삭제 (Soft Delete) |

### 댓글

| Method | Endpoint | 설명 |
|:------:|----------|-----|
| `POST` | `/api/comments` | 댓글 또는 대댓글 작성 |
| `DELETE` | `/api/comments/{commentId}` | 댓글 삭제 |

### 플레이리스트

| Method | Endpoint | 설명 |
|:------:|----------|-----|
| `GET`  | `/api/playlists` | 연동된 YouTube 계정의 플레이리스트 목록 조회 |

### 결제 & 후원

| Method | Endpoint | 설명 |
|:------:|----------|-----|
| `POST` | `/v1/payments/prepare` | Pop 충전 결제 준비 |
| `POST` | `/v1/payments/confirm` | 토스 페이먼츠 결제 승인, Pop 충전 |
| `POST` | `/api/donation` | Spotlight 게시글에 Pop 후원 |

### Spotlight

| Method | Endpoint | 설명 |
|:------:|----------|-----|
| `GET`  | `/api/spotlight/carousel` | 메인 페이지 Spotlight 게시글 목록 조회 |

### 마이페이지

| Method | Endpoint | 설명 |
|:------:|----------|-----|
| `GET`  | `/api/mypage/profile` | 내 프로필 정보 조회 |
| `GET`  | `/api/mypage/my-posts` | 내가 작성한 게시글 목록 조회 |

### 알림 & 메시지

| Method | Endpoint | 설명 |
|:------:|----------|-----|
| `GET`  | `/api/notification` | 내 알림 목록 조회 |
| `POST` | `/api/messages` | 메시지 전송 |

### 날씨

| Method | Endpoint | 설명 |
|:------:|----------|-----|
| `GET`  | `/api/weather/info` | 지역별 현재 습도 정보 조회 |

### 관리자

| Method | Endpoint | 설명 |
|:------:|----------|-----|
| `POST` | `/api/adm1n/login` | 관리자 로그인 |
| `GET`  | `/api/adm1n/inquiries` | 사용자 문의 목록 조회 |

## 프로젝트 구조

```
src/main/java/dopamine/soundock/
├── config/          # 보안, Redis, S3, OAuth2 등 설정
├── controller/      # REST API 컨트롤러
├── service/         # 비즈니스 로직
├── repository/      # 데이터 접근 계층
├── entity/          # JPA 엔티티
├── dto/
│   ├── request/     # 요청 DTO
│   └── response/    # 응답 DTO
├── enums/           # UserRole, CategoryType, PopStatus 등
├── exceptions/      # 커스텀 예외
├── global/          # JWT 필터, 공통 상수
└── scheduler/       # 토큰 정리, Spotlight 만료 등 스케줄링
```
