package com.example.codereview.model;

import jakarta.persistence.*;

@Entity
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int lineNumber;

    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String suggestion;

    @Enumerated(EnumType.STRING)
    private IssueCategory category;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @ManyToOne
    @JoinColumn(name = "review_id")
    private CodeReview codeReview;

    // ---------- GETTERS & SETTERS ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public IssueCategory getCategory() {
        return category;
    }

    public void setCategory(IssueCategory category) {
        this.category = category;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public CodeReview getCodeReview() {
        return codeReview;
    }

    public void setCodeReview(CodeReview codeReview) {
        this.codeReview = codeReview;
    }
}
