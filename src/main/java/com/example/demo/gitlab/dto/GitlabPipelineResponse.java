package com.example.demo.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitlabPipelineResponse(
        Long id,
        Integer iid,

        @JsonProperty("project_id")
        Long projectId,

        String sha,
        String ref,
        String status,
        String source,

        @JsonProperty("created_at")
        String createdAt,

        @JsonProperty("updated_at")
        String updatedAt,

        @JsonProperty("web_url")
        String webUrl
) {
}
