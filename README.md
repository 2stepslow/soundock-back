# Soundock Backend

음악을 좋아하는 사람들이 자기만의 유튜브 영상 및 플레이리스트를 공유하고, 마음에 드는 게시글에 후원까지 할 수 있는 커뮤니티 플랫폼 **Soundock** 백엔드입니다.

완료된 팀 프로젝트에 추가적인 기능 구현 및 별도의 배포를 진행한 Clone 저장소로, 팀 개발 당시 커밋 이력이 보존되어 있으며 PR기반 협업 이력은 원본 저장소에서 확인이 가능합니다.

**DOPAMINE** | 4명 | 2026.01.02. ~ 2026.03.06. (63일)

- 프론트엔드 저장소: [2stepslow/soundock-front](https://github.com/2stepslow/soundock-front)
- 원본 저장소: [ningsoo/dpm-project-back](https://github.com/ningsoo/dpm-project-back) | [ningsoo/dpm-project-front-v2](https://github.com/ningsoo/dpm-project-front-v2)

## 데모 안내

- 재배포 링크: [SOUNDOCK](https://soundock-front-indol.vercel.app/)
- 로그인 화면의 '데모 계정 사용하기' 버튼으로 쉽게 로그인 가능합니다
- 결제는 토스페이먼츠 테스트 모드로 동작하며 실제 결제가 발생하지 않습니다
- 데모 환경에서는 유튜브 계정 연동이 제한되며, 플레이리스트는 사전 연동된 데이터로 표시됩니다
- Spotlight 게시판의 컨텐츠는 데모용 가상 데이터입니다
- Passwordless 기능은 외부 인증 서버가 화이트리스트 방식이므로 시연 영상으로 대체합니다 - [Passwordless 기능](https://youtu.be/IFLcF_gfqS8)

## 담당 역할

- **FrontEnd** 전담: 사용자 & 인증
- **BackEnd** 풀스택 구현: 마이페이지 활동내역 & 게시판 검색
- **FE** 품질개선 & **BE** 보안 스캔 / 분석 / 보완
- 프로젝트 종료 후 기상청 API 연동해 습도에 따른 악기관리 안내기능 별도 풀스택 구현

### 재배포 구성

중단된 당시 AWS 배포 구성을 조건에 맞게 재구성 (콜드스타트 없음 / 저비용 / 구축 시간 최소화)

| 구성          | 사용 서비스                          | 비고                                           |
| ------------- | ------------------------------------ | ---------------------------------------------- |
| 백엔드        | Render (Docker, 512MB 단일 인스턴스) | JVM 메모리 상한을 실측 기반으로 고정           |
| DB            | TiDB Cloud (MySQL 호환)              | 백엔드와 동일 리전(싱가포르)                   |
| 캐시          | Upstash Redis (TLS)                  | 인기글 랭킹 집계, 조회 처리, 인증 보조         |
| 이미지 저장소 | Cloudflare R2 (S3 호환 API)          | 기존 S3 연동 코드를 엔드포인트 전환으로 재사용 |
| 프론트        | Vercel (Next.js)                     | OAuth 콜백과 API를 동일 출처 프록시로 구성     |

작업내용: 배포 환경 차이에 따른 코드 수정(도커 빌드 구성, 저장소 엔드포인트 전환, Redis TLS 연결, 쿠키와 보안 정책, OAuth 콜백 프록시), 데모 시딩 준비, 512MB 제약 검증(부하 측정으로 JVM 옵션 확정), 의존성 취약점 점검 및 조치

<!-- TODO: 대표 화면 스크린샷 1~2장 삽입 위치 -->

## 기술 스택

- **Java**: 21
- **Spring Boot**: 3.5.9 (Gradle)
- **Spring Security**: JWT, OAuth2 Client (Google, 유튜브 계정 연동용)
- **Spring Data JPA**: MySQL
- **Redis**: 인기글 랭킹 집계, 조회 처리, 인증 보조
- **Spring Mail**: 인증 메일 발송 (Thymeleaf 템플릿)
- **AWS S3**: 파일 업로드(spring-cloud-starter-aws), Apache Tika (업로드 파일 검증)
- **Toss Payments**: 결제 연동
- **YouTube Data API**: 플레이리스트 연동
- **기상청 초단기실황 API**: 날씨 정보
- **외부 API 연동**: WebClient(Spring WebFlux), RestClient
- **Swagger UI**: Springdoc-Openapi 2.8.8
- **Docker**: 컨테이너 배포
- **AWS**: ECR, ECS

## 주요 기능

### 사용자 & 인증

- 이메일 회원가입 (인증 메일 발송)
- JWT 기반 인증 (Access Token + Refresh Token)
- 패스워드리스 로그인 지원
- 마이페이지 (프로필, 내 게시글, 좋아요, 댓글 관리)

### 게시판 & 커뮤니티

- 카테고리별 게시판 (Showcase / Playlists / Spotlight / Community / Reviews / Notice)
- 게시글 CRUD, 좋아요, 조회수, 인기글
- 댓글, 댓글 좋아요
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

### 알림 & 메시지

- 활동 알림 (댓글, 좋아요, 후원 등)
- 사용자 간 1:1 메시지

### 관리자 (`/adm1n`)

- 공지사항 관리
- 사용자 문의 관리
- 후원 취소 승인 / 정산 처리

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

## API 문서

서버 기동 후 springdoc 기반 Swagger UI에서 전체 API 명세를 확인할 수 있습니다.

## API 엔드포인트

### 인증

|  Method  | Endpoint                            | 설명                        |
| :------: | ----------------------------------- | --------------------------- |
| `POST` | `/api/auth/signup`                | 회원가입 후 인증 메일 발송  |
| `POST` | `/api/auth/login`                 | 이메일/비밀번호 로그인      |
| `POST` | `/api/auth/logout`                | 로그아웃, 토큰 무효화       |
| `POST` | `/api/passwordless/login-trigger` | 패스워드리스 로그인 요청    |
| `GET` | `/api/passwordless/result`        | 패스워드리스 인증 결과 확인 |

### 게시판

|   Method   | Endpoint                  | 설명                                            |
| :--------: | ------------------------- | ----------------------------------------------- |
|  `GET`  | `/api/boards`           | 카테고리/검색어/정렬 조건으로 게시글 목록 조회  |
|  `POST`  | `/api/boards`           | 게시글 작성 (파일 첨부, 플레이리스트 연결 가능) |
|  `PUT`  | `/api/boards/{boardId}` | 게시글 수정                                     |
| `DELETE` | `/api/boards/{boardId}` | 게시글 삭제 (Soft Delete)                       |

### 댓글

|   Method   | Endpoint                      | 설명      |
| :--------: | ----------------------------- | --------- |
|  `POST`  | `/api/comments`             | 댓글 작성 |
| `DELETE` | `/api/comments/{commentId}` | 댓글 삭제 |

### 플레이리스트

| Method | Endpoint           | 설명                                         |
| :-----: | ------------------ | -------------------------------------------- |
| `GET` | `/api/playlists` | 연동된 YouTube 계정의 플레이리스트 목록 조회 |

### 결제 & 후원

|  Method  | Endpoint                 | 설명                              |
| :------: | ------------------------ | --------------------------------- |
| `POST` | `/v1/payments/prepare` | Pop 충전 결제 준비                |
| `POST` | `/v1/payments/confirm` | 토스 페이먼츠 결제 승인, Pop 충전 |
| `POST` | `/api/donation`        | Spotlight 게시글에 Pop 후원       |

### Spotlight

| Method | Endpoint                    | 설명                                   |
| :-----: | --------------------------- | -------------------------------------- |
| `GET` | `/api/spotlight/carousel` | 메인 페이지 Spotlight 게시글 목록 조회 |

### 마이페이지

| Method | Endpoint                 | 설명                         |
| :-----: | ------------------------ | ---------------------------- |
| `GET` | `/api/mypage/profile`  | 내 프로필 정보 조회          |
| `GET` | `/api/mypage/my-posts` | 내가 작성한 게시글 목록 조회 |

### 알림 & 메시지

|  Method  | Endpoint              | 설명              |
| :------: | --------------------- | ----------------- |
| `GET` | `/api/notification` | 내 알림 목록 조회 |
| `POST` | `/api/messages`     | 메시지 전송       |

### 날씨

| Method | Endpoint              | 설명                       |
| :-----: | --------------------- | -------------------------- |
| `GET` | `/api/weather/info` | 지역별 현재 습도 정보 조회 |

### 관리자

|  Method  | Endpoint                 | 설명                  |
| :------: | ------------------------ | --------------------- |
| `POST` | `/api/adm1n/login`     | 관리자 로그인         |
| `GET` | `/api/adm1n/inquiries` | 사용자 문의 목록 조회 |

## 추가 기능 구현: 기상 기반 악기 관리 안내

프로젝트 종료 후, 기상청 초단기실황 API에서 습도(REH)를 조회해 지역별 악기 관리 안내 메시지를 제공하는 기능을 개인적으로 구현했습니다.

- Spring RestClient로 API 호출, JsonNode 트리 탐색으로 필요한 값만 추출
- 지역 선택과 매핑되는 Region enum으로 기상청 격자 좌표(nx, ny) 변환
- 습도 5구간별 관리 메시지 생성, 비로그인 조회 허용
- 초기 구현 파일: WeatherInfoController, WeatherInfoService, WeatherInfoRequest/Response
- 이후 팀장의 코드 리뷰를 통해 지역 문자열 변환(RegionConverter 도입), 기상청 발표시각(매시 40분 생성) 보정, 응답의 null 필드 생략이 개선 반영되었습니다

<!-- TODO: 대표 화면 스크린샷 삽입 위치 -->
