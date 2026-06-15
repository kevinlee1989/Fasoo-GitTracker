# GitLab Metrics API Project

Spring Boot 기반으로 GitLab API와 연동하여 프로젝트 정보, 커밋, Merge Request, MR Discussion, MR별 Commit 데이터를 수집하는 백엔드 프로젝트입니다.

수집한 GitLab 개발 활동 데이터는 향후 **AI 활용 성숙도 분석**, **코드 리뷰 품질 분석**, **커밋/MR 기반 개발 지표 분석**에 활용할 수 있습니다.

---

## Tech Stack

| Category     | Technology      |
| ------------ | --------------- |
| Language     | Java            |
| Framework    | Spring Boot     |
| Build Tool   | Gradle          |
| HTTP Client  | RestClient      |
| JSON Mapping | Jackson         |
| Boilerplate  | Lombok          |
| External API | GitLab REST API |

---

## Project Structure

```text
src/main/java/com/example/demo
└── gitlab
    ├── GitlabConfig.java
    ├── GitlabClient.java
    ├── GitlabController.java
    └── dto
        ├── GitlabProjectResponse.java
        ├── GitlabCommitResponse.java
        ├── GitlabMergeRequestResponse.java
        ├── GitlabMrDiscussionResponse.java
        └── GitlabMergeRequestCommitResponse.java
```

---

## Configuration

`application.properties`

```properties
gitlab.base-url=https://gitlab.com/api/v4
gitlab.token=${GITLAB_TOKEN}
gitlab.project-id=83345754
```

`.env`

```env
GITLAB_TOKEN=glpat-your-token
```

`.env`는 Git에 올리지 않도록 `.gitignore`에 추가합니다.

```gitignore
.env
```

---

## API Endpoints

| Method | Endpoint                                     | Description                  |
| ------ | -------------------------------------------- | ---------------------------- |
| GET    | `/gitlab/project`                            | GitLab 프로젝트 기본 정보 조회         |
| GET    | `/gitlab/commits`                            | 프로젝트 전체 commit 목록 조회         |
| GET    | `/gitlab/merge-requests`                     | 프로젝트 Merge Request 목록 조회     |
| GET    | `/gitlab/merge-requests/{mrIid}/discussions` | 특정 MR의 댓글, 리뷰 코멘트, 시스템 노트 조회 |
| GET    | `/gitlab/merge-requests/{mrIid}/commits`     | 특정 MR에 포함된 commit 목록 조회      |

---

## API Details

### 1. Project 조회

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

### 2. Commit 조회

```http
GET /gitlab/commits
```

프로젝트 전체 commit 목록을 조회합니다.

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
commit message 분석
merge commit 여부
```

---

### 3. Merge Request 조회

```http
GET /gitlab/merge-requests
```

프로젝트의 Merge Request 목록을 조회합니다.

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

`iid`는 GitLab에서 보이는 MR 번호입니다.
예를 들어 MR `!1`이면 `iid = 1`입니다.

---

### 4. MR Discussion 조회

```http
GET /gitlab/merge-requests/{mrIid}/discussions
```

특정 MR에 달린 discussion, comment, system note를 조회합니다.

수집 데이터:

```text
discussion id
individual_note
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

리뷰 코멘트 수를 계산할 때는 보통 `system = false`인 note만 사용합니다.

활용 가능 지표:

```text
전체 discussion 수
사람이 작성한 리뷰 코멘트 수
GitLab system note 수
unresolved 코멘트 수
코드 라인 리뷰 코멘트 수
TEST / BUG / REFACTOR 관련 코멘트 분류
```

---

### 5. MR별 Commit 조회

```http
GET /gitlab/merge-requests/{mrIid}/commits
```

특정 MR에 포함된 commit 목록을 조회합니다.

예시:

```http
GET /gitlab/merge-requests/2/commits
```

수집 데이터:

```text
commit id
short_id
title
message
author_name
author_email
authored_date
committed_date
created_at
web_url
```

활용 가능 지표:

```text
MR별 commit 수
리뷰 이후 추가 commit 수
리뷰 수정 횟수
review response count
```

리뷰 수정 횟수는 다음 방식으로 계산할 수 있습니다.

```text
리뷰 코멘트 created_at 이후에 발생한 MR commit 수
= 리뷰 이후 수정 횟수
```

예시:

```text
리뷰 코멘트 시간: 2026-06-15 05:13
MR commit A: 2026-06-15 05:10 → 리뷰 전 commit
MR commit B: 2026-06-15 05:20 → 리뷰 후 commit
MR commit C: 2026-06-15 05:25 → 리뷰 후 commit

리뷰 이후 수정 횟수 = 2
```

---

## Current Test Result

현재 확인된 API 상태입니다.

| API                                          | Status        |
| -------------------------------------------- | ------------- |
| `GET /gitlab/project`                        | Success       |
| `GET /gitlab/commits`                        | Success       |
| `GET /gitlab/merge-requests`                 | Success       |
| `GET /gitlab/merge-requests/1/discussions`   | Success       |
| `GET /gitlab/merge-requests/{mrIid}/commits` | Ready to Test |

현재 테스트 데이터 기준:

```text
Project ID: 83345754
Project Name: creatorSettlementapp
MR !1 State: merged
Commit 목록 조회 성공
MR Discussion 5개 조회 성공
```

MR Discussion 분석 결과:

```text
전체 discussion 수: 5
전체 note 수: 5
GitLab system note 수: 3
사람이 직접 작성한 comment 수: 2
코드 라인 리뷰 comment 수: 0
```

---

## Available Metrics From Current APIs

현재 API들로 계산 가능한 지표는 다음과 같습니다.

| Category       | Metrics                                  |
| -------------- | ---------------------------------------- |
| Commit         | 전체 commit 수, 작성자별 commit 수, 날짜별 commit 수 |
| Merge Request  | MR 수, MR 상태, PR 생성→Merge 시간              |
| MR Commit      | MR별 commit 수, 리뷰 이후 추가 commit 수          |
| Review         | 사람 댓글 수, system note 수, 코드 라인 리뷰 수       |
| Review Quality | TEST / BUG / REFACTOR / PRAISE 등 코멘트 분류  |

아직 직접 수집하기 어려운 지표:

```text
AI 기여도
AI 결과물 수정률
테스트 커버리지
테스트 케이스 수
빌드 실패 횟수
정적 코드 분석 결과
Merge 이후 실제 버그 발생 건수
```

---

## Run Project

Build:

```powershell
.\gradlew clean build
```

Run:

```powershell
.\gradlew bootRun
```

Test:

```http
GET http://localhost:8080/gitlab/project
GET http://localhost:8080/gitlab/commits
GET http://localhost:8080/gitlab/merge-requests
GET http://localhost:8080/gitlab/merge-requests/1/discussions
GET http://localhost:8080/gitlab/merge-requests/1/commits
```

---

## Future Improvements

향후 추가할 기능:

```text
1. GitLab 원본 데이터 요약 API 추가
2. /gitlab/metrics/summary 구현
3. 리뷰 이후 추가 commit 수 계산 로직 구현
4. Pipeline API 연동으로 빌드 실패 횟수 수집
5. JaCoCo/JUnit 리포트 연동으로 테스트 커버리지 및 테스트 개수 수집
6. MR description 또는 PR template 기반 AI 사용 정보 수집
7. Review comment LLM 분류 기능 추가
```

예상 Metrics Summary API:

```http
GET /gitlab/metrics/summary
```

예상 응답:

```json
{
  "commitCount": 14,
  "mergeRequestCount": 1,
  "mergedMergeRequestCount": 1,
  "humanReviewCommentCount": 2,
  "systemNoteCount": 3,
  "lineReviewCommentCount": 0,
  "reviewAfterCommitCount": 2
}
```

---

## Summary

현재 프로젝트는 GitLab API와 Spring Boot를 연동하여 다음 데이터를 수집합니다.

```text
1. GitLab 프로젝트 정보
2. GitLab 전체 commit 목록
3. GitLab Merge Request 목록
4. GitLab MR discussion/comment 목록
5. GitLab MR별 commit 목록
```

현재까지 GitLab 연동, RestClient 설정, DTO 매핑, Controller API 테스트가 진행되었습니다.

다음 단계는 수집한 데이터를 기반으로 **개발 활동 요약 지표**, **리뷰 이후 수정 횟수**, **AI 활용 성숙도 분석 지표**를 계산하는 것입니다.
