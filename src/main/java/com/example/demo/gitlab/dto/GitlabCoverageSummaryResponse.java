package com.example.demo.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitlabCoverageSummaryResponse(
        GitlabCoverageCounterResponse instruction,
        GitlabCoverageCounterResponse branch,
        GitlabCoverageCounterResponse line,
        GitlabCoverageCounterResponse method,
        
        @JsonProperty("class")
        GitlabCoverageCounterResponse clazz
) {
}