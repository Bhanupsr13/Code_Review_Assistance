package com.example.codereview.dto;

import java.util.List;

public class AnalyzeResponse {

    private Long reviewId;
    private String filename;

    private int errorCount;
    private int warningCount;
    private int optimizationCount;
    private int securityCount;

    private List<IssueDto> issues;

    // ---------------- GETTERS & SETTERS ----------------

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(int warningCount) {
        this.warningCount = warningCount;
    }

    public int getOptimizationCount() {
        return optimizationCount;
    }

    public void setOptimizationCount(int optimizationCount) {
        this.optimizationCount = optimizationCount;
    }

    public int getSecurityCount() {
        return securityCount;
    }

    public void setSecurityCount(int securityCount) {
        this.securityCount = securityCount;
    }

    public List<IssueDto> getIssues() {
        return issues;
    }

    public void setIssues(List<IssueDto> issues) {
        this.issues = issues;
    }
}
