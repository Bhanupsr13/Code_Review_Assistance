package com.example.codereview.service.rules;

import com.example.codereview.model.Issue;
import com.example.codereview.model.IssueCategory;
import com.example.codereview.model.Severity;

import java.util.List;
import java.util.regex.Pattern;

class EmptyCatchBlockRule extends BaseRule {
	private static final Pattern EMPTY_CATCH = Pattern.compile("catch\\s*\\([^)]*\\)\\s*\\{\\s*\\}");

	@Override
	public List<Issue> apply(AnalysisContext context) {
		List<Issue> out = list();
		String[] lines = context.getLines();
		for (int i = 0; i < lines.length; i++) {
			if (EMPTY_CATCH.matcher(lines[i]).find()) {
				out.add(makeIssue(context.getReview(), i + 1,
						"Empty catch block",
						"Empty catch blocks hide exceptions and make debugging difficult.",
						"Log the exception and/or handle it appropriately.",
						IssueCategory.WARNING, Severity.MEDIUM));
			}
		}
		return out;
	}

	@Override
	public String name() { return "empty-catch"; }
}


