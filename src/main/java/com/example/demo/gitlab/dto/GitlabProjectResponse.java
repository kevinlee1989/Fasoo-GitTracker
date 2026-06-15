package com.example.demo.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// gitlab api에서 프로젝트 정보를 받아올 때 사용하는 응답 객체
public record GitlabProjectResponse(
        Long id,
        String name,

        @JsonProperty("path_with_namespace")
        String pathWithNamespace,

        @JsonProperty("web_url")
        String webUrl,

        @JsonProperty("default_branch")
        String defaultBranch,

        String visibility,

        @JsonProperty("last_activity_at")
        String lastActivityAt
) {
}