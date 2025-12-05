package com.example.codereview.service.rules;

import com.example.codereview.model.Issue;
import com.example.codereview.model.IssueCategory;
import com.example.codereview.model.Severity;

import java.util.List;

class UnmatchedBracesRule extends BaseRule {
	@Override
	public List<Issue> apply(AnalysisContext context) {
		List<Issue> out = list();
		long openBraces = context.getCode().chars().filter(c -> c == '{').count();
		long closeBraces = context.getCode().chars().filter(c -> c == '}').count();
		if (openBraces != closeBraces) {
			out.add(makeIssue(context.getReview(), 0,
					"Unmatched braces",
					"Number of '{' and '}' characters does not match.",
					"Ensure each opening brace has a matching closing brace.",
					IssueCategory.ERROR, Severity.HIGH));
		}
		return out;
	}

	@Override
	public String name() { return "unmatched-braces"; }
}


