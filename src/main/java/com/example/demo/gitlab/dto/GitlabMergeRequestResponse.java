package com.example.demo.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitlabMergeRequestResponse(
        Long id,
        Integer iid,
        String title,
        String description,
        String state,
        Author author,

        @JsonProperty("source_branch")
        String sourceBranch,

        @JsonProperty("target_branch")
        String targetBranch,

        @JsonProperty("created_at")
        String createdAt,

        @JsonProperty("updated_at")
        String updatedAt,

        @JsonProperty("merged_at")
        String mergedAt,

        @JsonProperty("closed_at")
        String closedAt,

        @JsonProperty("changes_count")
        String changesCount,

        @JsonProperty("web_url")
        String webUrl
) {
    public record Author(
            Long id,
            String username,
            String name
    ) {
    }
}