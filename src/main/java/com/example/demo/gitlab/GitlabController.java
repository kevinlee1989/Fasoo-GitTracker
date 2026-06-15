package com.example.demo.gitlab;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.gitlab.dto.GitlabCommitResponse;
import com.example.demo.gitlab.dto.GitlabMergeRequestResponse;
import com.example.demo.gitlab.dto.GitlabMrDiscussionResponse;
import com.example.demo.gitlab.dto.GitlabProjectResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/gitlab")
@RequiredArgsConstructor
public class GitlabController {

    private final GitlabClient gitlabClient;

    @GetMapping("/project")
    public GitlabProjectResponse getProject() {
        return gitlabClient.getProject();
    }

    @GetMapping("/commits")
    public List<GitlabCommitResponse> getCommits() {
        return gitlabClient.getCommits();
    }

    @GetMapping("/merge-requests")
    public List<GitlabMergeRequestResponse> getMergeRequests() {
        return gitlabClient.getMergeRequests();
    }

    @GetMapping("/merge-requests/{mrIid}/discussions")
    public List<GitlabMrDiscussionResponse> getMergeRequestDiscussions(
            @PathVariable Integer mrIid
    ) {
        return gitlabClient.getMergeRequestDiscussions(mrIid);
    }
        
}