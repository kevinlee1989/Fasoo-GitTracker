package com.example.demo.gitlab.dto;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitlabMrDiscussionResponse(
        String id,

        @JsonProperty("individual_note")
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

            @JsonProperty("created_at")
            String createdAt,

            @JsonProperty("updated_at")
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
            @JsonProperty("old_path")
            String oldPath,

            @JsonProperty("new_path")
            String newPath,

            @JsonProperty("old_line")
            Integer oldLine,

            @JsonProperty("new_line")
            Integer newLine,

            @JsonProperty("position_type")
            String positionType
    ) {
    }
}