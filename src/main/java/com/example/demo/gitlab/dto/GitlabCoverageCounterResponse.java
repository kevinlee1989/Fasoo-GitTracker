package com.example.demo.gitlab.dto;


public record GitlabCoverageCounterResponse(
        String type,
        int missed,
        int covered,
        int total,
        double coverageRate
) {
}