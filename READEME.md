# GitLab Metrics API Project

Spring Boot 기반으로 GitLab API와 연동하여 프로젝트 정보, 커밋 목록, Merge Request 목록, MR Discussion/Comment 데이터를 수집하는 백엔드 프로젝트입니다.

이 프로젝트의 목적은 GitLab에서 개발 활동 데이터를 가져와 향후 **AI 활용 성숙도 분석**, **코드 리뷰 품질 분석**, **커밋/MR 기반 개발 지표 분석**에 활용하는 것입니다.

---

## 1. Project Overview

현재 프로젝트는 GitLab API를 호출하여 다음 데이터를 수집합니다.

| 기능               | 설명                           |
| ---------------- | ---------------------------- |
| Project 조회       | GitLab 프로젝트 기본 정보 조회         |
| Commit 조회        | 프로젝트의 commit 목록 조회           |
| Merge Request 조회 | 프로젝트의 MR 목록 조회               |
| MR Discussion 조회 | 특정 MR의 댓글, 리뷰 코멘트, 시스템 노트 조회 |

현재까지 총 4개의 API가 구현되어 있습니다.

```text
GET /gitlab/project
GET /gitlab/commits
GET /gitlab/merge-requests
GET /gitlab/merge-requests/{mrIid}/discussions
```

---

## 2. Tech Stack

| Category              | Technology        |
| --------------------- | ----------------- |
| Language              | Java              |
| Framework             | Spring Boot       |
| Build Tool            | Gradle            |
| HTTP Client           | Spring RestClient |
| JSON Mapping          | Jackson           |
| Boilerplate Reduction | Lombok            |
| External API          | GitLab REST API   |

---

## 3. Project Structure

```text
src/main/java/com/example/demo
├── DemoApplication.java
└── gitlab
    ├── GitlabConfig.java
    ├── GitlabClient.java
    ├── GitlabController.java
    └── dto
        ├── GitlabProjectResponse.java
        ├── GitlabCommitResponse.java
        ├── GitlabMergeRequestResponse.java
        └── GitlabMrDiscussionResponse.java
```

---

## 4. Configuration

GitLab API 호출을 위해 `application.properties` 또는 `application.yml`에 GitLab 설정이 필요합니다.

### application.properties 예시

```properties
gitlab.base-url=https://gitlab.com/api/v4
gitlab.token=${GITLAB_TOKEN}
gitlab.project-id=83345754
```

### .env 예시

```env
GITLAB_TOKEN=glpat-your-token
```

`.env` 파일은 GitLab Access Token을 보관하기 위한 파일이며, Git 저장소에 올라가면 안 됩니다.

`.gitignore`에 다음 항목을 추가합니다.

```gitignore
.env
```

---

## 5. GitLabConfig

`GitlabConfig`는 GitLab API 호출에 사용할 `RestClient` Bean을 생성하는 설정 클래스입니다.

```java
@Configuration
public class GitlabConfig {

    @Bean
    public RestClient gitlabRestClient(
            @Value("${gitlab.base-url}") String baseUrl,
            @Value("${gitlab.token}") String token
    ) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("PRIVATE-TOKEN", token)
                .build();
    }
}
```

### 역할

* GitLab API 기본 URL 설정
* GitLab Access Token을 HTTP Header에 자동 추가
* GitLab API 전용 `RestClient` Bean 생성

`defaultHeader("PRIVATE-TOKEN", token)`을 설정했기 때문에, 이후 GitLab API를 호출할 때마다 매번 토큰을 직접 넣지 않아도 됩니다.

---

## 6. GitlabClient

`GitlabClient`는 실제로 GitLab API에 요청을 보내는 클래스입니다.

```java
@Component
@RequiredArgsConstructor
public class GitlabClient {

    private final RestClient gitlabRestClient;

    @Value("${gitlab.project-id}")
    private String projectId;

    ...
}
```

### 역할

| Method                                      | 설명                              |
| ------------------------------------------- | ------------------------------- |
| `getProject()`                              | GitLab 프로젝트 기본 정보 조회            |
| `getCommits()`                              | 프로젝트 commit 목록 조회               |
| `getMergeRequests()`                        | 프로젝트 MR 목록 조회                   |
| `getMergeRequestDiscussions(Integer mrIid)` | 특정 MR의 discussion/comment 목록 조회 |

`GitlabClient`는 일반적인 DB 구조에서 `Repository` 또는 `Mapper`와 비슷한 위치에 있습니다.

차이는 다음과 같습니다.

```text
Repository / Mapper → DB에 접근
GitlabClient        → 외부 GitLab API에 접근
```

---

## 7. GitlabController

`GitlabController`는 외부 클라이언트가 호출할 수 있는 REST API를 제공합니다.

```java
@RestController
@RequestMapping("/gitlab")
@RequiredArgsConstructor
public class GitlabController {

    private final GitlabClient gitlabClient;

    @GetMapping("/project")
    public GitlabProjectResponse getProject() {
        return gitlabClient.getProject();
    }

    @GetMapping("/commits")
    public List<GitlabCommitResponse> getCommits() {
        return gitlabClient.getCommits();
    }

    @GetMapping("/merge-requests")
    public List<GitlabMergeRequestResponse> getMergeRequests() {
        return gitlabClient.getMergeRequests();
    }

    @GetMapping("/merge-requests/{mrIid}/discussions")
    public List<GitlabMrDiscussionResponse> getMergeRequestDiscussions(
            @PathVariable Integer mrIid
    ) {
        return gitlabClient.getMergeRequestDiscussions(mrIid);
    }
}
```

Controller와 Client의 역할 차이는 다음과 같습니다.

```text
Controller   → 우리 서버로 들어오는 요청을 받음
GitlabClient → 우리 서버에서 GitLab API로 요청을 보냄
```

---

## 8. API Endpoints

### 8.1 Project 조회 API

```http
GET /gitlab/project
```

### 설명

GitLab 프로젝트의 기본 정보를 가져옵니다.

### 내부 GitLab API

```text
GET /projects/{projectId}
```

### Sample Response

```json
{
  "id": 83345754,
  "name": "creatorSettlementapp",
  "path_with_namespace": "kevinlee1989/creatorsettlementapp",
  "web_url": "https://gitlab.com/kevinlee1989/creatorsettlementapp",
  "default_branch": "main",
  "visibility": "public",
  "last_activity_at": "2026-06-15T00:30:42.743Z"
}
```

### 주요 수집 데이터

| Field                 | 설명                   |
| --------------------- | -------------------- |
| `id`                  | GitLab Project ID    |
| `name`                | 프로젝트 이름              |
| `path_with_namespace` | namespace 포함 프로젝트 경로 |
| `web_url`             | GitLab 웹 URL         |
| `default_branch`      | 기본 브랜치               |
| `visibility`          | public/private 여부    |
| `last_activity_at`    | 마지막 활동 시간            |

---

### 8.2 Commit 목록 조회 API

```http
GET /gitlab/commits
```

### 설명

GitLab 프로젝트의 commit 목록을 가져옵니다.

### 내부 GitLab API

```text
GET /projects/{projectId}/repository/commits?per_page=100
```

### Sample Response

```json
[
  {
    "id": "d750e284ec9575f59c871fcf1b9c8502904eb673",
    "short_id": "d750e284",
    "title": "Merge branch 'feature/init' into 'main'",
    "message": "Merge branch 'feature/init' into 'main'",
    "author_name": "Kevin Jaewoo Lee",
    "author_email": "kjmin99999@gmail.com",
    "committed_date": "2026-06-15T00:44:14.000+00:00",
    "web_url": "https://gitlab.com/kevinlee1989/creatorsettlementapp/-/commit/d750e284ec9575f59c871fcf1b9c8502904eb673"
  }
]
```

### 주요 수집 데이터

| Field            | 설명             |
| ---------------- | -------------- |
| `id`             | 전체 commit hash |
| `short_id`       | 짧은 commit hash |
| `title`          | commit 제목      |
| `message`        | commit 메시지     |
| `author_name`    | commit 작성자 이름  |
| `author_email`   | commit 작성자 이메일 |
| `committed_date` | commit 시간      |
| `web_url`        | commit 웹 URL   |

### 활용 가능 지표

* commit 수
* 작성자별 commit 수
* 날짜별 commit 수
* merge commit 여부
* commit message 분석
* 작업량 추정

---

### 8.3 Merge Request 목록 조회 API

```http
GET /gitlab/merge-requests
```

### 설명

GitLab 프로젝트의 Merge Request 목록을 가져옵니다.

### 내부 GitLab API

```text
GET /projects/{projectId}/merge_requests?state=all&per_page=100
```

### Sample Response

```json
[
  {
    "id": 495794629,
    "iid": 1,
    "title": "Feature/init",
    "description": "ADSF",
    "state": "merged",
    "author": {
      "id": 31508875,
      "username": "kevinlee1989",
      "name": "Kevin Jaewoo Lee"
    },
    "source_branch": "feature/init",
    "target_branch": "main",
    "created_at": "2026-06-15T00:41:07.713Z",
    "updated_at": "2026-06-15T00:44:16.072Z",
    "merged_at": "2026-06-15T00:44:14.878Z",
    "closed_at": null,
    "changes_count": null,
    "web_url": "https://gitlab.com/kevinlee1989/creatorsettlementapp/-/merge_requests/1"
  }
]
```

### 주요 수집 데이터

| Field           | 설명                          |
| --------------- | --------------------------- |
| `id`            | GitLab 전체 MR ID             |
| `iid`           | 프로젝트 내부 MR 번호               |
| `title`         | MR 제목                       |
| `description`   | MR 설명                       |
| `state`         | opened / closed / merged 상태 |
| `author`        | MR 작성자 정보                   |
| `source_branch` | 작업 브랜치                      |
| `target_branch` | 병합 대상 브랜치                   |
| `created_at`    | MR 생성 시간                    |
| `merged_at`     | MR merge 시간                 |
| `web_url`       | MR 웹 URL                    |

### 중요 개념: id vs iid

GitLab MR에는 `id`와 `iid`가 있습니다.

```text
id  = GitLab 전체에서 사용하는 내부 고유 ID
iid = 해당 프로젝트 안에서 사용하는 MR 번호
```

MR Discussion API를 호출할 때는 보통 `iid`를 사용합니다.

예를 들어 GitLab에서 MR이 `!1`이면:

```text
iid = 1
```

따라서 discussion 조회는 다음과 같이 호출합니다.

```http
GET /gitlab/merge-requests/1/discussions
```

### 활용 가능 지표

* MR 개수
* merged/opened/closed 비율
* MR 생성부터 merge까지 걸린 시간
* 브랜치 흐름 분석
* 작성자별 MR 수
* MR 설명 작성 여부

---

### 8.4 MR Discussion 조회 API

```http
GET /gitlab/merge-requests/{mrIid}/discussions
```

### 설명

특정 Merge Request에 달린 discussion, comment, system note 목록을 가져옵니다.

### 내부 GitLab API

```text
GET /projects/{projectId}/merge_requests/{mrIid}/discussions?per_page=100
```

### Sample Response

```json
[
  {
    "id": "ecd32594b87f31664b2a02b1524d066ac1633b44",
    "individual_note": true,
    "notes": [
      {
        "id": 3454502932,
        "type": null,
        "body": "hi I am your code reviewer and I think you did a great job making this project!",
        "author": {
          "id": 31508875,
          "username": "kevinlee1989",
          "name": "Kevin Jaewoo Lee"
        },
        "system": false,
        "resolvable": false,
        "resolved": null,
        "position": null,
        "created_at": "2026-06-15T05:13:39.696Z",
        "updated_at": "2026-06-15T05:13:39.696Z"
      }
    ]
  }
]
```

### 주요 수집 데이터

| Field              | 설명                     |
| ------------------ | ---------------------- |
| `id`               | discussion thread ID   |
| `individual_note`  | 단일 note 여부             |
| `notes`            | discussion 내부 실제 댓글 목록 |
| `notes.body`       | 댓글 내용                  |
| `notes.author`     | 댓글 작성자                 |
| `notes.system`     | GitLab 자동 시스템 노트 여부    |
| `notes.resolvable` | resolve 가능한 댓글 여부      |
| `notes.resolved`   | 해결 여부                  |
| `notes.position`   | 코드 라인 댓글 위치            |
| `notes.created_at` | 댓글 생성 시간               |

### system 값의 의미

```text
system: true  → GitLab이 자동으로 남긴 시스템 기록
system: false → 사람이 직접 작성한 댓글
```

예시:

```text
system: true
- assigned to @kevinlee1989
- added 2 commits
- mentioned in commit ...

system: false
- 사람이 작성한 리뷰 코멘트
- 일반 댓글
- 테스트용 댓글
```

리뷰 코멘트 수를 분석할 때는 보통 `system = false`인 note만 세는 것이 적절합니다.

### position 값의 의미

`position`은 코드 라인에 달린 리뷰 코멘트 위치를 나타냅니다.

```json
"position": {
  "old_path": "src/main/java/Example.java",
  "new_path": "src/main/java/Example.java",
  "old_line": null,
  "new_line": 25,
  "position_type": "text"
}
```

현재 MR 전체 댓글처럼 코드 라인에 직접 달리지 않은 댓글은 다음과 같이 나타납니다.

```json
"position": null
```

### 활용 가능 지표

* 전체 discussion 수
* 전체 note/comment 수
* 사람이 작성한 리뷰 코멘트 수
* GitLab system note 수
* unresolved 코멘트 수
* 코드 라인 리뷰 코멘트 수
* TEST / BUG / REFACTOR 관련 리뷰 코멘트 수
* 리뷰 코멘트 품질 분석

---

## 9. DTO Structure

### GitlabProjectResponse

GitLab 프로젝트 기본 정보를 담는 DTO입니다.

```java
public record GitlabProjectResponse(
        Long id,
        String name,
        String pathWithNamespace,
        String webUrl,
        String defaultBranch,
        String visibility,
        String lastActivityAt
) {
}
```

---

### GitlabCommitResponse

GitLab commit 정보를 담는 DTO입니다.

```java
public record GitlabCommitResponse(
        String id,
        String shortId,
        String title,
        String message,
        String authorName,
        String authorEmail,
        String committedDate,
        String webUrl
) {
}
```

---

### GitlabMergeRequestResponse

GitLab Merge Request 정보를 담는 DTO입니다.

`author`가 중첩 객체이므로 내부 record를 사용합니다.

```java
public record GitlabMergeRequestResponse(
        Long id,
        Integer iid,
        String title,
        String description,
        String state,
        Author author,
        String sourceBranch,
        String targetBranch,
        String createdAt,
        String updatedAt,
        String mergedAt,
        String closedAt,
        String changesCount,
        String webUrl
) {
    public record Author(
            Long id,
            String username,
            String name
    ) {
    }
}
```

---

### GitlabMrDiscussionResponse

GitLab MR discussion 정보를 담는 DTO입니다.

GitLab Discussion 응답은 다음처럼 중첩 구조입니다.

```text
Discussion
 └── notes
      ├── author
      └── position
```

따라서 DTO 내부에 `DiscussionNote`, `Author`, `Position` record를 둡니다.

```java
public record GitlabMrDiscussionResponse(
        String id,
        Boolean individualNote,
        List<DiscussionNote> notes
) {
    public record DiscussionNote(
            Long id,
            String type,
            String body,
            Author author,
            Boolean system,
            Boolean resolvable,
            Boolean resolved,
            Position position,
            String createdAt,
            String updatedAt
    ) {
    }

    public record Author(
            Long id,
            String username,
            String name
    ) {
    }

    public record Position(
            String oldPath,
            String newPath,
            Integer oldLine,
            Integer newLine,
            String positionType
    ) {
    }
}
```

---

## 10. Why record DTO?

이 프로젝트에서는 GitLab API 응답 DTO를 `record`로 작성했습니다.

`record`를 사용한 이유는 다음과 같습니다.

* 응답 데이터를 담는 용도에 적합
* 코드가 짧고 간결함
* 생성자, getter 역할의 메서드, `equals`, `hashCode`, `toString` 자동 생성
* 기본적으로 값을 변경하지 않는 immutable DTO 구조에 적합

record DTO는 getter가 `getName()` 형태가 아니라 다음처럼 사용됩니다.

```java
project.name();
commit.shortId();
mr.iid();
note.body();
```

---

## 11. JsonProperty

GitLab API 응답은 snake_case를 사용합니다.

```json
{
  "path_with_namespace": "...",
  "web_url": "...",
  "created_at": "..."
}
```

Java에서는 camelCase를 사용합니다.

```java
pathWithNamespace
webUrl
createdAt
```

따라서 이름이 다른 필드는 `@JsonProperty`를 사용하여 매핑합니다.

```java
@JsonProperty("web_url")
String webUrl
```

의미:

```text
JSON의 web_url 값을 Java의 webUrl 필드에 매핑
```

---

## 12. Run Project

### Build

```powershell
.\gradlew clean build
```

### Run

```powershell
.\gradlew bootRun
```

정상 실행 시 다음과 같이 Tomcat이 8080 포트에서 실행됩니다.

```text
Tomcat started on port 8080
Started DemoApplication
```

---

## 13. API Test

Postman 또는 브라우저에서 다음 API를 테스트할 수 있습니다.

### Project

```http
GET http://localhost:8080/gitlab/project
```

### Commits

```http
GET http://localhost:8080/gitlab/commits
```

### Merge Requests

```http
GET http://localhost:8080/gitlab/merge-requests
```

### MR Discussions

```http
GET http://localhost:8080/gitlab/merge-requests/1/discussions
```

---

## 14. Current Test Result

현재 다음 API 호출이 정상 동작함을 확인했습니다.

| API                                        | Status  |
| ------------------------------------------ | ------- |
| `GET /gitlab/project`                      | Success |
| `GET /gitlab/commits`                      | Success |
| `GET /gitlab/merge-requests`               | Success |
| `GET /gitlab/merge-requests/1/discussions` | Success |

수집된 데이터 예시:

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

## 15. Future Improvements

향후 확장 가능한 기능은 다음과 같습니다.

### 1. Service Layer 추가

현재는 Controller가 GitlabClient를 직접 호출하지만, 이후에는 Service Layer를 추가하여 분석 로직을 분리할 수 있습니다.

```text
Controller
   ↓
Service
   ↓
GitlabClient
   ↓
GitLab API
```

예상 Service 역할:

* commit 수 계산
* MR merge 소요 시간 계산
* review comment 수 계산
* system note 제외한 사람 댓글만 필터링
* TEST / BUG / REFACTOR 코멘트 분류

---

### 2. Metrics API 추가

GitLab 원본 데이터를 그대로 반환하는 API 외에, 분석 결과를 반환하는 API를 만들 수 있습니다.

예시:

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
  "lineReviewCommentCount": 0
}
```

---

### 3. Review Comment Classification

MR discussion의 `body`를 분석하여 리뷰 코멘트를 분류할 수 있습니다.

예시 분류:

| Category | Example   |
| -------- | --------- |
| TEST     | 테스트 추가 요청 |
| BUG      | 버그 가능성 지적 |
| REFACTOR | 구조 개선 요청  |
| SECURITY | 보안 문제 지적  |
| PRAISE   | 칭찬        |
| GENERAL  | 일반 코멘트    |

---

### 4. AI Maturity Analysis

최종적으로 다음과 같은 AI 활용 성숙도 지표로 확장할 수 있습니다.

| Metric               | Description               |
| -------------------- | ------------------------- |
| AI Contribution Rate | AI 생성 코드 기여 비율            |
| AI Survival Rate     | AI 생성 코드가 최종 코드에 남은 비율    |
| Test Verification    | 테스트 커버리지 또는 테스트 관련 코멘트 분석 |
| Review Quality       | 리뷰 코멘트 수와 유형 분석           |
| Build Stability      | CI/CD 빌드 실패 여부            |
| Merge Stability      | Merge 이후 bug/hotfix 여부    |

---

## 16. Summary

이 프로젝트는 GitLab API와 Spring Boot를 연동하여 개발 활동 데이터를 수집하는 백엔드 프로젝트입니다.

현재 구현된 기능은 다음과 같습니다.

```text
1. GitLab 프로젝트 정보 조회
2. GitLab commit 목록 조회
3. GitLab Merge Request 목록 조회
4. GitLab MR discussion/comment 조회
```

현재까지 GitLab 연동, RestClient 설정, DTO 매핑, Controller API 테스트가 모두 정상적으로 완료되었습니다.

향후에는 수집한 데이터를 기반으로 commit 분석, MR 분석, review comment 분석, AI 활용 성숙도 지표 계산 기능을 추가할 예정입니다.
