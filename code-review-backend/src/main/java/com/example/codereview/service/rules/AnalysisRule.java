package com.example.codereview.service.rules;

import com.example.codereview.model.Issue;

import java.util.List;

public interface AnalysisRule {
	List<Issue> apply(AnalysisContext context);
	String name();
	boolean isEnabled();
	void setEnabled(boolean enabled);
}


