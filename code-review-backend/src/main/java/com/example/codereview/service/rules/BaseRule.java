package com.example.codereview.service.rules;

import com.example.codereview.model.CodeReview;
import com.example.codereview.model.Issue;
import com.example.codereview.model.IssueCategory;
import com.example.codereview.model.Severity;

import java.util.ArrayList;
import java.util.List;

abstract class BaseRule implements AnalysisRule {
	private boolean enabled = true;

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	protected Issue makeIssue(CodeReview review, int lineNumber,
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

	protected static List<Issue> list() { return new ArrayList<>(); }
}


