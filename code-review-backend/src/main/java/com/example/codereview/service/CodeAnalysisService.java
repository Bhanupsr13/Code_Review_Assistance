package com.example.codereview.service;

import com.example.codereview.dto.AnalyzeRequest;
import com.example.codereview.dto.AnalyzeResponse;
import com.example.codereview.dto.IssueDto;
import com.example.codereview.model.*;
import com.example.codereview.repository.CodeReviewRepository;
import com.example.codereview.service.rules.AnalysisContext;
import com.example.codereview.service.rules.AnalysisRule;
import com.example.codereview.service.rules.RuleRegistry;
import org.springframework.stereotype.Service;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class CodeAnalysisService {

    private final CodeReviewRepository codeReviewRepository;
    private final RuleRegistry ruleRegistry;

    public CodeAnalysisService(CodeReviewRepository codeReviewRepository, RuleRegistry ruleRegistry) {
        this.codeReviewRepository = codeReviewRepository;
        this.ruleRegistry = ruleRegistry;
    }

    public AnalyzeResponse analyze(AnalyzeRequest request) {
        String code = request.getCode();
        String[] lines = code.split("\n");

        CodeReview review = new CodeReview();
        review.setFilename(request.getFilename() != null ? request.getFilename() : "inline.java");
        review.setCode(code);
        review.setCreatedAt(LocalDateTime.now());

        List<Issue> issueEntities = new ArrayList<>();
        List<IssueDto> issueDtos = new ArrayList<>();

        int errors = 0, warnings = 0, optimizations = 0, security = 0;

        // Run pluggable rules
        AnalysisContext context = new AnalysisContext(review, code, lines);
        for (AnalysisRule rule : ruleRegistry.getEnabledRules()) {
            List<Issue> produced = rule.apply(context);
            for (Issue issue : produced) {
                issueEntities.add(issue);
                issueDtos.add(toDto(issue));
                if (issue.getCategory() == IssueCategory.ERROR) errors++;
                else if (issue.getCategory() == IssueCategory.WARNING) warnings++;
                else if (issue.getCategory() == IssueCategory.OPTIMIZATION) optimizations++;
                else if (issue.getCategory() == IssueCategory.SECURITY) security++;
            }
        }

        // Java compiler diagnostics for real syntax/type errors (best-effort)
        List<Issue> compilerIssues = runJavaCompilerDiagnostics(review, request.getFilename(), code);
        for (Issue issue : compilerIssues) {
            issueEntities.add(issue);
            issueDtos.add(toDto(issue));
            errors++;
        }

        review.setErrorCount(errors);
        review.setWarningCount(warnings);
        review.setOptimizationCount(optimizations);
        review.setSecurityCount(security);
        review.getIssues().addAll(issueEntities);

        review = codeReviewRepository.save(review);

        AnalyzeResponse response = new AnalyzeResponse();
        response.setReviewId(review.getId());
        response.setFilename(review.getFilename());
        response.setErrorCount(errors);
        response.setWarningCount(warnings);
        response.setOptimizationCount(optimizations);
        response.setSecurityCount(security);
        response.setIssues(issueDtos);

        return response;
    }

    private List<Issue> runJavaCompilerDiagnostics(CodeReview review, String originalFilename, String code) {
        List<Issue> out = new ArrayList<>();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            // JRE detected (no compiler available) - skip
            return out;
        }
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        String className = derivePrimaryClassName(originalFilename, code);
        JavaFileObject file = new InMemoryJavaSource(className, code);
        List<String> options = List.of("--release", "17");
        JavaCompiler.CompilationTask task = compiler.getTask(null, null, diagnostics, options, null, List.of(file));
        Boolean success = task.call();
        if (Boolean.TRUE.equals(success)) {
            return out;
        }
        for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
            if (d.getKind() == Diagnostic.Kind.ERROR) {
                int line = (int) d.getLineNumber();
                String message = d.getMessage(Locale.getDefault());
                Issue issue = makeIssue(review, Math.max(line, 0),
                        "Compile error",
                        message,
                        "Fix the compilation error reported by the Java compiler.",
                        IssueCategory.ERROR, Severity.HIGH);
                out.add(issue);
            }
        }
        return out;
    }

    private String derivePrimaryClassName(String filename, String code) {
        if (filename != null && filename.endsWith(".java")) {
            String nameOnly = filename.substring(0, filename.length() - 5);
            if (!nameOnly.isBlank()) return nameOnly;
        }
        // Fallback: try find 'class X' in the code
        String marker = "class ";
        int idx = code.indexOf(marker);
        if (idx >= 0) {
            int start = idx + marker.length();
            int end = start;
            while (end < code.length() && Character.isJavaIdentifierPart(code.charAt(end))) end++;
            String candidate = code.substring(start, end).trim();
            if (!candidate.isBlank()) return candidate;
        }
        return "InlineClass";
    }

    private Issue makeIssue(CodeReview review, int lineNumber,
                            String title, String description, String suggestion,
                            IssueCategory category, Severity severity) {
        Issue issue = new Issue();
        issue.setCodeReview(review);
        issue.setLineNumber(lineNumber);
        issue.setTitle(title);
        issue.setDescription(description);
        issue.setSuggestion(suggestion);
        issue.setCategory(category);
        issue.setSeverity(severity);
        return issue;
    }

    private IssueDto toDto(Issue issue) {
        IssueDto dto = new IssueDto();
        dto.setId(issue.getId());
        dto.setLineNumber(issue.getLineNumber());
        dto.setTitle(issue.getTitle());
        dto.setDescription(issue.getDescription());
        dto.setSuggestion(issue.getSuggestion());
        dto.setCategory(issue.getCategory());
        dto.setSeverity(issue.getSeverity());
        return dto;
    }

    static class InMemoryJavaSource extends SimpleJavaFileObject {
        private final String source;
        InMemoryJavaSource(String className, String source) {
            super(java.net.URI.create("string:///" + className.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension),
                    JavaFileObject.Kind.SOURCE);
            this.source = Objects.requireNonNull(source);
        }
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
        }
    }
}
