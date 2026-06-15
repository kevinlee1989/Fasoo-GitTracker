# LiveKlass — 크리에이터 정산 + GitLab Metrics API 서버

> 본 문서는 LiveKlass 크리에이터 정산 백엔드 API 서버와 GitLab 개발 활동 데이터 수집 API의 구현 내용을 정리한 최종 보고서입니다.

---

## 1. 프로젝트 개요

강의 플랫폼 **LiveKlass**의 크리에이터 수익 정산을 처리하는 RESTful API 서버입니다.

크리에이터가 개설한 강의에 대한 판매·취소 내역을 관리하고, 이를 기반으로 월별 정산 금액을 자동 산출합니다.

정산은 다음 3단계 상태 흐름을 통해 운영자가 순차적으로 확정 및 지급 처리할 수 있도록 설계되었습니다.

```text
PENDING → CONFIRMED → PAID
```

또한 본 프로젝트에는 GitLab API 연동 기능을 추가하여, 프로젝트의 commit, Merge Request, 리뷰 코멘트, Pipeline 실행 결과를 수집할 수 있습니다.

이 데이터는 향후 **AI 활용 성숙도 분석**, **코드 리뷰 품질 분석**, **빌드 안정성 분석**, **개발 활동 지표 분석**에 활용할 수 있습니다.

---

## 2. 기술 스택

| 구분               | 기술                |
| ---------------- | ----------------- |
| Language         | Java 26           |
| Framework        | Spring Boot 4.0.5 |
| ORM / SQL Mapper | MyBatis 4.0.1     |
| Database         | PostgreSQL 16     |
| DB Migration     | Flyway 11         |
| Build Tool       | Gradle            |
| HTTP Client      | Spring RestClient |
| JSON Mapping     | Jackson           |
| Utility          | Lombok            |
| External API     | GitLab REST API   |

---

## 3. 실행 방법

### 사전 조건

로컬 환경에 PostgreSQL이 설치되어 있어야 합니다.

### 애플리케이션 실행

```bash
./gradlew bootRun
```

서버 기동 시 Flyway가 자동으로 스키마를 생성하고, `DataInitializer`가 샘플 데이터를 자동 삽입합니다.

API 서버 기본 주소:

```text
http://localhost:8080
```

---

## 4. 환경 설정

GitLab API 호출을 위해 `application.properties`에 다음 설정이 필요합니다.

```properties
gitlab.base-url=https://gitlab.com/api/v4
gitlab.token=${GITLAB_TOKEN}
gitlab.project-id=83345754
```

`.env` 예시:

```env
GITLAB_TOKEN=glpat-your-token
```

`.env` 파일은 Git 저장소에 올라가지 않도록 `.gitignore`에 추가합니다.

```gitignore
.env
```

---

## 5. 테스트 실행

```bash
./gradlew test
```

정산 도메인 기준 총 **13개 테스트 클래스, 72개 테스트 케이스**로 구성되어 있습니다.

각 테스트는 실행 전 DB를 초기화하고 샘플 데이터를 재삽입하여 독립적인 환경에서 수행됩니다.

| 테스트 파일                             | 도메인          | 테스트 수 | 검증 내용                                       |
| ---------------------------------- | ------------ | :---: | ------------------------------------------- |
| `CreatorApiTest`                   | Creator      |   3   | 목록 조회, 추가 후 조회, 응답 필드 확인                    |
| `CourseApiTest`                    | Course       |   6   | 제목 수정 성공/실패, 빈 제목 검증, 삭제 성공/실패              |
| `SaleRecordApiTest`                | Sale         |   3   | 등록 성공, 존재하지 않는 강의 ID, 중복 ID 검증              |
| `SaleRecordListApiTest`            | Sale         |   5   | 전체 조회, 기간 필터, 기간 외 제외, 응답 필드, 빈 결과          |
| `SaleRecordListBoundaryTest`       | Sale         |   7   | 시작일·종료일 경계값, from/to 단독 입력 400 처리           |
| `SaleRecordValidationTest`         | Sale         |   8   | amount/studentId/courseId/paidAt 입력값 검증     |
| `CancellationRecordApiTest`        | Cancellation |   3   | 등록 성공, 부분 환불, 존재하지 않는 판매 ID                 |
| `CancellationRecordValidationTest` | Cancellation |   3   | refundAmount/canceledAt/saleRecordId 입력값 검증 |
| `CancellationRefundRuleTest`       | Cancellation |   4   | 초과 환불 거절, 부분 취소 누적, 전액환불 후 추가 취소            |
| `SettlementCalculationApiTest`     | Settlement   |   6   | 정산 시나리오, 수수료 계산, 월 경계                       |
| `SettlementStatusTest`             | Settlement   |   10  | PENDING/CONFIRMED/PAID 전이                   |
| `SettlementEdgeCaseTest`           | Settlement   |   6   | 순판매 0원, 소수점 버림, 음수 netSales                 |
| `SettlementSummaryApiTest`         | Settlement   |   8   | 기간별 집계, 크리에이터 단위 합산                         |

---

## 6. 전체 API 목록

### Creator

| 메서드      | 엔드포인트                   | 설명             |
| -------- | ----------------------- | -------------- |
| `POST`   | `/creators`             | 크리에이터 등록       |
| `GET`    | `/creators`             | 전체 크리에이터 목록 조회 |
| `DELETE` | `/creators/{creatorId}` | 크리에이터 삭제       |

### Course

| 메서드      | 엔드포인트                       | 설명       |
| -------- | --------------------------- | -------- |
| `POST`   | `/courses`                  | 강의 등록    |
| `PATCH`  | `/courses/{courseId}/title` | 강의 제목 수정 |
| `DELETE` | `/courses/{courseId}`       | 강의 삭제    |

### SaleRecord

| 메서드    | 엔드포인트                                | 설명       |
| ------ | ------------------------------------ | -------- |
| `POST` | `/sale-records`                      | 판매 내역 등록 |
| `GET`  | `/sale-records?creatorId=&from=&to=` | 판매 내역 조회 |

### CancellationRecord

| 메서드    | 엔드포인트                   | 설명          |
| ------ | ----------------------- | ----------- |
| `POST` | `/cancellation-records` | 취소/환불 내역 등록 |

### Settlement

| 메서드     | 엔드포인트                                                | 설명                |
| ------- | ---------------------------------------------------- | ----------------- |
| `GET`   | `/settlements/creators/{creatorId}?month=YYYY-MM`    | 크리에이터 월별 정산 조회    |
| `PATCH` | `/settlements/creators/{creatorId}?month=YYYY-MM`    | 정산 상태 변경          |
| `GET`   | `/settlements/summary?from=YYYY-MM-DD&to=YYYY-MM-DD` | 운영자용 기간별 전체 정산 집계 |

---

## 7. 도메인 구조

```text
com.example
├── creator/          크리에이터 등록 및 목록 조회
├── course/           강의 등록, 제목 수정, 삭제
├── sale/             판매 내역 등록 및 조회
├── cancellation/     취소/환불 내역 등록 및 누적 환불 검증
├── settlement/       월별 정산 계산, 상태 관리, 운영자 집계
├── gitlab/           GitLab 개발 활동 데이터 수집
├── common/           GlobalExceptionHandler, ErrorResponse
└── config/           DataInitializer
```

### 엔티티 관계

```text
Creator (1)
  └── Course (N)              creator_id → creators.id ON DELETE CASCADE
        └── SaleRecord (N)    course_id → courses.id ON DELETE RESTRICT
              └── CancellationRecord (N) sale_record_id → sale_records.id ON DELETE RESTRICT

Creator (1)
  └── Settlement (N)          creator_id → creators.id ON DELETE CASCADE
```

---

## 8. 정산 흐름 및 핵심 로직

### 상태 전이

```text
PENDING → CONFIRMED → PAID
```

| 상태          | 설명                          |
| ----------- | --------------------------- |
| `PENDING`   | 정산 레코드 없음. GET 요청 시 실시간 집계  |
| `CONFIRMED` | 운영자가 확정 처리. 금액을 DB에 스냅샷 저장  |
| `PAID`      | 지급 완료. paidAt 기록 및 추가 전이 차단 |

### 전이 규칙

| 요청                      | 가능 여부 |
| ----------------------- | ----- |
| `PENDING → CONFIRMED`   | 가능    |
| `CONFIRMED → PAID`      | 가능    |
| `PENDING → PAID`        | 불가    |
| `CONFIRMED → CONFIRMED` | 불가    |
| `PAID → 다른 상태`          | 불가    |

### 수수료 계산

```text
netSales         = totalSales - totalRefunds
platformFee      = netSales × 20%
settlementAmount = netSales - platformFee
```

플랫폼 수수료는 소수점 이하를 버림 처리합니다.

```java
RoundingMode.DOWN
```

---

## 9. GitLab Metrics API

GitLab API와 연동하여 개발 활동 데이터를 수집합니다.

### GitLab API 구조

```text
gitlab
├── GitlabConfig.java       RestClient Bean 설정
├── GitlabClient.java       GitLab API 호출 담당
├── GitlabController.java   외부 요청 엔드포인트 제공
└── dto
    ├── GitlabProjectResponse.java
    ├── GitlabCommitResponse.java
    ├── GitlabMergeRequestResponse.java
    ├── GitlabMrDiscussionResponse.java
    ├── GitlabMergeRequestCommitResponse.java
    ├── GitlabPipelineResponse.java
    └── GitlabJobResponse.java
```

### GitLab Metrics API 목록

| 메서드   | 엔드포인트                                        | 설명                           |
| ----- | -------------------------------------------- | ---------------------------- |
| `GET` | `/gitlab/project`                            | GitLab 프로젝트 기본 정보 조회         |
| `GET` | `/gitlab/commits`                            | 프로젝트 전체 commit 목록 조회         |
| `GET` | `/gitlab/merge-requests`                     | 프로젝트 MR 목록 조회                |
| `GET` | `/gitlab/merge-requests/{mrIid}/discussions` | 특정 MR의 댓글, 리뷰 코멘트, 시스템 노트 조회 |
| `GET` | `/gitlab/merge-requests/{mrIid}/commits`     | 특정 MR에 포함된 commit 목록 조회      |
| `GET` | `/gitlab/pipelines`                          | GitLab Pipeline 목록 조회        |
| `GET` | `/gitlab/pipelines/{pipelineId}/jobs`        | 특정 Pipeline의 Job 목록 조회       |

---

## 10. GitLab 수집 데이터와 활용 지표

### Project

```http
GET /gitlab/project
```

수집 데이터:

```text
project id
name
path_with_namespace
web_url
default_branch
visibility
last_activity_at
```

---

### Commit

```http
GET /gitlab/commits
```

수집 데이터:

```text
commit id
short_id
title
message
author_name
author_email
committed_date
web_url
```

활용 가능 지표:

```text
전체 commit 수
작성자별 commit 수
날짜별 commit 수
merge commit 여부
commit message 분석
```

---

### Merge Request

```http
GET /gitlab/merge-requests
```

수집 데이터:

```text
MR id
MR iid
title
description
state
author
source_branch
target_branch
created_at
merged_at
web_url
```

활용 가능 지표:

```text
MR 수
merged/opened/closed 비율
PR 생성부터 merge까지 걸린 시간
작성자별 MR 수
브랜치 흐름 분석
```

`iid`는 GitLab 화면에서 보이는 MR 번호입니다.
예를 들어 MR `!3`이면 API 호출 시 `mrIid = 3`입니다.

---

### MR Discussion

```http
GET /gitlab/merge-requests/{mrIid}/discussions
```

수집 데이터:

```text
discussion id
notes.body
notes.author
notes.system
notes.resolvable
notes.resolved
notes.position
notes.created_at
```

`system` 값 의미:

```text
system = true  → GitLab 자동 시스템 기록
system = false → 사람이 직접 작성한 댓글
```

활용 가능 지표:

```text
사람이 작성한 리뷰 코멘트 수
GitLab system note 수
unresolved 코멘트 수
코드 라인 리뷰 코멘트 수
TEST / BUG / REFACTOR / PRAISE 코멘트 분류
```

---

### MR별 Commit

```http
GET /gitlab/merge-requests/{mrIid}/commits
```

특정 MR에 포함된 commit 목록을 조회합니다.

활용 가능 지표:

```text
MR별 commit 수
리뷰 이후 추가 commit 수
리뷰 수정 횟수
review response count
```

리뷰 수정 횟수 계산 방식:

```text
리뷰 코멘트 created_at 이후에 발생한 MR commit 수
= 리뷰 이후 수정 횟수
```

예시 흐름:

```text
first commit
→ 리뷰 코멘트 발생
→ second commit
→ 추가 리뷰 코멘트 발생
→ final commit

리뷰 이후 수정 횟수 = 2
```

---

### Pipeline / Job

```http
GET /gitlab/pipelines
GET /gitlab/pipelines/{pipelineId}/jobs
```

수집 데이터:

```text
pipeline id
pipeline status
pipeline source
branch/ref
job stage
job name
job status
failure reason
duration
```

활용 가능 지표:

```text
pipeline 총 개수
pipeline 성공률
build failure count
test failure count
test skipped count
failure reason 분석
```

현재 테스트 예시:

```text
전체 pipeline 수: 5
failed pipeline 수: 5
success pipeline 수: 0
pipeline success rate: 0%
build failure count: 1
test failure count: 0
test skipped count: 1
failure reason: script_failure
```

해석:

```text
현재 CI/CD 파이프라인은 build 단계에서 실패하고 있으며,
test 단계는 build 실패로 인해 skipped 상태입니다.
따라서 이 경우는 test failure가 아니라 build failure로 분류합니다.
```

---

## 11. 현재 GitLab API 테스트 결과

| API                                              | Status  |
| ------------------------------------------------ | ------- |
| `GET /gitlab/project`                            | Success |
| `GET /gitlab/commits`                            | Success |
| `GET /gitlab/merge-requests`                     | Success |
| `GET /gitlab/merge-requests/{mrIid}/discussions` | Success |
| `GET /gitlab/merge-requests/{mrIid}/commits`     | Success |
| `GET /gitlab/pipelines`                          | Success |
| `GET /gitlab/pipelines/{pipelineId}/jobs`        | Success |

현재 확인된 수집 결과 예시:

```text
Project ID: 83345754
Project Name: creatorSettlementapp
MR Discussion 조회 성공
MR별 Commit 조회 성공
Pipeline 조회 성공
Pipeline Job 조회 성공
```

---

## 12. 현재 API로 계산 가능한 지표

| 카테고리            | 계산 가능 지표                                 |
| --------------- | ---------------------------------------- |
| Commit          | 전체 commit 수, 작성자별 commit 수, 날짜별 commit 수 |
| Merge Request   | MR 수, MR 상태, PR 생성→Merge 시간              |
| Review          | 사람 댓글 수, system note 수, 코드 라인 리뷰 수       |
| Review Response | 리뷰 이후 추가 commit 수, 리뷰 수정 횟수              |
| Pipeline        | pipeline 성공률, build 실패 횟수, test 실패/스킵 횟수 |
| Review Quality  | TEST / BUG / REFACTOR / PRAISE 코멘트 분류    |

아직 직접 수집하기 어려운 지표:

```text
AI 기여도
AI 결과물 수정률
테스트 커버리지
테스트 케이스 수
정적 코드 분석 결과
Merge 이후 실제 운영 버그 발생 건수
```

---

## 13. 향후 개선 방향

```text
1. /gitlab/metrics/summary API 구현
2. 리뷰 이후 추가 commit 수 자동 계산
3. pipeline success rate 자동 계산
4. build/test failure count 자동 계산
5. JaCoCo/JUnit 리포트 연동으로 테스트 커버리지 및 테스트 개수 수집
6. MR description 또는 PR template 기반 AI 사용 정보 수집
7. Review comment LLM 분류 기능 추가
8. SonarQube 연동으로 정적 코드 분석 지표 수집
```

예상 Metrics Summary API:

```http
GET /gitlab/metrics/summary
```

예상 응답:

```json
{
  "commitCount": 14,
  "mergeRequestCount": 3,
  "mergedMergeRequestCount": 2,
  "humanReviewCommentCount": 5,
  "systemNoteCount": 8,
  "lineReviewCommentCount": 1,
  "reviewAfterCommitCount": 2,
  "pipelineCount": 5,
  "failedPipelineCount": 5,
  "pipelineSuccessRate": 0.0,
  "buildFailureCount": 1,
  "testFailureCount": 0,
  "testSkippedCount": 1
}
```

---

## 14. 문서

| 문서                           | 내용                         |
| ---------------------------- | -------------------------- |
| [API 목록 및 예시](docs/API.md)   | 전체 엔드포인트, 요청/응답 예시, 에러 케이스 |
| [데이터 모델](docs/DATA_MODEL.md) | 엔티티 구조, 테이블 컬럼, 관계도        |
| [테스트 실행](docs/TESTING.md)    | 테스트 방법, 시나리오 설명            |

---

## 15. Summary

본 프로젝트는 두 가지 기능을 포함합니다.

```text
1. LiveKlass 크리에이터 정산 API 서버
2. GitLab 개발 활동 데이터 수집 API 서버
```

정산 API는 판매·취소 데이터를 기반으로 월별 정산 금액을 계산하고, `PENDING → CONFIRMED → PAID` 상태 전이를 관리합니다.

GitLab Metrics API는 commit, MR, discussion, MR별 commit, pipeline, job 데이터를 수집하여 개발 활동과 코드 리뷰 품질을 분석할 수 있도록 합니다.

현재까지 GitLab 연동, RestClient 설정, DTO 매핑, Controller API 테스트가 완료되었으며, 다음 단계는 수집 데이터를 기반으로 **개발 활동 요약 지표**와 **AI 활용 성숙도 분석 지표**를 계산하는 것입니다.

*작성일: 2026-06-15*
