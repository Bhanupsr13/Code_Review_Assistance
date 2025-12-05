package com.example.codereview.controller;

import com.example.codereview.dto.*;
import com.example.codereview.model.CodeReview;
import com.example.codereview.model.Issue;
import com.example.codereview.repository.CodeReviewRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.codereview.service.CodeAnalysisService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringJoiner;

@RestController
@RequestMapping("/api")
public class CodeReviewController {

    private final CodeAnalysisService analysisService;
    private final CodeReviewRepository reviewRepository;

    public CodeReviewController(CodeAnalysisService analysisService,
                                CodeReviewRepository reviewRepository) {
        this.analysisService = analysisService;
        this.reviewRepository = reviewRepository;
    }

    // 1. Analyze code from text editor
    @PostMapping("/analyze")
    public AnalyzeResponse analyze(@RequestBody AnalyzeRequest request) {
		return analysisService.analyze(request);
    }
    
    // 2. Analyze uploaded .java file
    @PostMapping(value = "/analyze/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalyzeResponse analyzeFromFile(@RequestParam("file") MultipartFile file) throws IOException {
        String code = new String(file.getBytes(), StandardCharsets.UTF_8);
        AnalyzeRequest req = new AnalyzeRequest();
        req.setCode(code);
        req.setFilename(file.getOriginalFilename());
        return analysisService.analyze(req);
    }

    // 3. Get a previous report
    @GetMapping("/reviews/{id}")
    public CodeReview getReview(@PathVariable Long id) {
        return reviewRepository.findById(id).orElseThrow();
    }

    // 4. List all reviews (for dashboard)
    @GetMapping("/reviews")
    public List<CodeReview> getAllReviews() {
        return reviewRepository.findAll();
    }

    // 5. Export report
    @GetMapping("/reviews/{id}/export")
    public ResponseEntity<String> export(@PathVariable Long id,
                                         @RequestParam(defaultValue = "html") String format) {
        CodeReview review = reviewRepository.findById(id).orElseThrow();
        if ("txt".equalsIgnoreCase(format)) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .header("Content-Disposition", "attachment; filename=\"review-" + id + ".txt\"")
                    .body(buildTextReport(review));
        } else {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .header("Content-Disposition", "attachment; filename=\"review-" + id + ".html\"")
                    .body(buildHtmlReport(review));
        }
    }

    private String buildTextReport(CodeReview r) {
        StringJoiner sj = new StringJoiner("\n");
        sj.add("Code Review Report");
        sj.add("File: " + r.getFilename() + " | Review ID: " + r.getId());
        sj.add("Errors: " + r.getErrorCount() + ", Warnings: " + r.getWarningCount()
                + ", Optimizations: " + r.getOptimizationCount() + ", Security: " + r.getSecurityCount());
        sj.add("");
        for (Issue i : r.getIssues()) {
            sj.add("[" + i.getCategory() + "] (Line " + i.getLineNumber() + ") " + i.getTitle());
            sj.add("  " + i.getDescription());
            sj.add("  Suggestion: " + i.getSuggestion());
            sj.add("  Severity: " + i.getSeverity());
            sj.add("");
        }
        return sj.toString();
    }

    private String buildHtmlReport(CodeReview r) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!doctype html><html><head><meta charset='utf-8'><title>Code Review Report</title>");
        sb.append("<style>body{font-family:Arial,Helvetica,sans-serif;padding:20px;} .issue{margin:12px 0;padding:10px;border:1px solid #ddd;border-radius:6px;} .meta{color:#555}</style>");
        sb.append("</head><body>");
        sb.append("<h1>Code Review Report</h1>");
        sb.append("<p class='meta'><b>File:</b> ").append(escape(r.getFilename())).append(" | <b>Review ID:</b> ").append(r.getId()).append("</p>");
        sb.append("<ul>");
        sb.append("<li>Errors: ").append(r.getErrorCount()).append("</li>");
        sb.append("<li>Warnings: ").append(r.getWarningCount()).append("</li>");
        sb.append("<li>Optimizations: ").append(r.getOptimizationCount()).append("</li>");
        sb.append("<li>Security: ").append(r.getSecurityCount()).append("</li>");
        sb.append("</ul>");
        sb.append("<h2>Issues</h2>");
        for (Issue i : r.getIssues()) {
            sb.append("<div class='issue'>");
            sb.append("<h3>[").append(i.getCategory()).append("] (Line ").append(i.getLineNumber()).append(") ").append(escape(i.getTitle())).append("</h3>");
            sb.append("<p>").append(escape(i.getDescription())).append("</p>");
            sb.append("<p><b>Suggestion:</b> ").append(escape(i.getSuggestion())).append("</p>");
            sb.append("<p class='meta'>Severity: ").append(i.getSeverity()).append("</p>");
            sb.append("</div>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
