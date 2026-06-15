package com.example.demo.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitlabCommitResponse(
        String id,

        @JsonProperty("short_id")
        String shortId,

        String title,
        String message,

        @JsonProperty("author_name")
        String authorName,

        @JsonProperty("author_email")
        String authorEmail,

        @JsonProperty("committed_date")
        String committedDate,

        @JsonProperty("web_url")
        String webUrl
) {
}