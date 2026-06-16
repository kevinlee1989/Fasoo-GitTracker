package com.example.demo.gitlab;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.demo.gitlab.dto.GitlabCommitResponse;
import com.example.demo.gitlab.dto.GitlabMergeRequestResponse;
import com.example.demo.gitlab.dto.GitlabProjectResponse;
import com.example.demo.gitlab.dto.GitlabMrDiscussionResponse;
import com.example.demo.gitlab.dto.GitlabMergeRequestCommitResponse;
import com.example.demo.gitlab.dto.GitlabPipelineResponse;
import com.example.demo.gitlab.dto.GitlabJobResponse;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GitlabClient {

    private final RestClient gitlabRestClient;

    @Value("${gitlab.project-id}")
    private String projectId;

    public GitlabProjectResponse getProject() {
        return gitlabRestClient.get()
                .uri("/projects/{projectId}", projectId)
                .retrieve()
                .body(GitlabProjectResponse.class);
    }

    public List<GitlabCommitResponse> getCommits() {
        return gitlabRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/projects/{projectId}/repository/commits")
                        .queryParam("per_page", 100)
                        .build(projectId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<GitlabCommitResponse>>() {});
    }


    public List<GitlabMergeRequestResponse> getMergeRequests() {
        return gitlabRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/projects/{projectId}/merge_requests")
                        .queryParam("state", "all")
                        .queryParam("per_page", 100)
                        .build(projectId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<GitlabMergeRequestResponse>>() {});
    }

    public List<GitlabMrDiscussionResponse> getMergeRequestDiscussions(Integer mrIid) {
        return gitlabRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/projects/{projectId}/merge_requests/{mrIid}/discussions")
                        .queryParam("per_page", 100)
                        .build(projectId, mrIid))
                .retrieve()
                .body(new ParameterizedTypeReference<List<GitlabMrDiscussionResponse>>() {});
    }

    public List<GitlabMergeRequestCommitResponse> getMergeRequestCommits(Integer mrIid) {
        return gitlabRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/projects/{projectId}/merge_requests/{mrIid}/commits")
                        .queryParam("per_page", 100)
                        .build(projectId, mrIid))
                .retrieve()
                .body(new ParameterizedTypeReference<List<GitlabMergeRequestCommitResponse>>() {});
    }

    public List<GitlabPipelineResponse> getPipelines() {
        return gitlabRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/projects/{projectId}/pipelines")
                        .queryParam("per_page", 100)
                        .build(projectId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<GitlabPipelineResponse>>() {});
    }

    public List<GitlabJobResponse> getJobs(Long pipelineId) {
        return gitlabRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/projects/{projectId}/pipelines/{pipelineId}/jobs")
                        .queryParam("per_page", 100)
                        .build(projectId, pipelineId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<GitlabJobResponse>>() {});
    }

    public String getJacocoXmlReport(Long jobId){
        return gitlabRestClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/projects/{projectId}/jobs/{jobId}/artifacts/build/reports/jacoco/test/jacocoTestReport.xml")
                    .build(projectId, jobId))
            .retrieve()
            .body(String.class);
        }
}