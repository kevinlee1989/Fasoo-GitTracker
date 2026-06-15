package com.example.demo.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitlabJobResponse(
        Long id,
        String status,
        String stage,
        String name,
        String ref,

        @JsonProperty("allow_failure")
        Boolean allowFailure,

        @JsonProperty("failure_reason")
        String failureReason,

        @JsonProperty("created_at")
        String createdAt,

        @JsonProperty("started_at")
        String startedAt,

        @JsonProperty("finished_at")
        String finishedAt,

        Double duration,

        @JsonProperty("queued_duration")
        Double queuedDuration,

        @JsonProperty("web_url")
        String webUrl,

        User user
) {
    public record User(
            Long id,
            String username,
            String name
    ) {
    }
}
