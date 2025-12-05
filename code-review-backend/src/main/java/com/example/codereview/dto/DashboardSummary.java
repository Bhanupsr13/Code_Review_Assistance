package com.example.codereview.dto;

public class DashboardSummary {

    private long totalReviews;
    private long totalIssues;
    private long totalErrors;
    private long totalWarnings;
    private long totalOptimizations;
    private long totalSecurityIssues;

    public long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(long totalReviews) {
        this.totalReviews = totalReviews;
    }

    public long getTotalIssues() {
        return totalIssues;
    }

    public void setTotalIssues(long totalIssues) {
        this.totalIssues = totalIssues;
    }

    public long getTotalErrors() {
        return totalErrors;
    }

    public void setTotalErrors(long totalErrors) {
        this.totalErrors = totalErrors;
    }

    public long getTotalWarnings() {
        return totalWarnings;
    }

    public void setTotalWarnings(long totalWarnings) {
        this.totalWarnings = totalWarnings;
    }

    public long getTotalOptimizations() {
        return totalOptimizations;
    }

    public void setTotalOptimizations(long totalOptimizations) {
        this.totalOptimizations = totalOptimizations;
    }

    public long getTotalSecurityIssues() {
        return totalSecurityIssues;
    }

    public void setTotalSecurityIssues(long totalSecurityIssues) {
        this.totalSecurityIssues = totalSecurityIssues;
    }
}
