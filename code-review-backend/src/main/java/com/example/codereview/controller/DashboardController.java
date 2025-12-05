package com.example.codereview.controller;

import com.example.codereview.dto.DashboardSummary;
import com.example.codereview.model.CodeReview;
import com.example.codereview.repository.CodeReviewRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final CodeReviewRepository reviewRepository;

    public DashboardController(CodeReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/summary")
    public DashboardSummary getSummary() {
        List<CodeReview> reviews = reviewRepository.findAll();
        DashboardSummary summary = new DashboardSummary();

        long totalReviews = reviews.size();
        long totalIssues = 0, totalErrors = 0, totalWarnings = 0, totalOpt = 0, totalSec = 0;

        for (CodeReview r : reviews) {
            totalErrors += r.getErrorCount();
            totalWarnings += r.getWarningCount();
            totalOpt += r.getOptimizationCount();
            totalSec += r.getSecurityCount();
        }

        totalIssues = totalErrors + totalWarnings + totalOpt + totalSec;

        summary.setTotalReviews(totalReviews);
        summary.setTotalIssues(totalIssues);
        summary.setTotalErrors(totalErrors);
        summary.setTotalWarnings(totalWarnings);
        summary.setTotalOptimizations(totalOpt);
        summary.setTotalSecurityIssues(totalSec);

        return summary;
    }
}
