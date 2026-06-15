package com.example.demo.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitlabMergeRequestCommitResponse(
        String id,

        @JsonProperty("short_id")
        String shortId,

        String title,
        String message,

        @JsonProperty("author_name")
        String authorName,

        @JsonProperty("author_email")
        String authorEmail,

        @JsonProperty("authored_date")
        String authoredDate,

        @JsonProperty("committed_date")
        String committedDate,

        @JsonProperty("created_at")
        String createdAt,

        @JsonProperty("web_url")
        String webUrl
) {
}