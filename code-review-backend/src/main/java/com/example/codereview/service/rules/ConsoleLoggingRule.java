package com.example.codereview.service.rules;

import com.example.codereview.model.Issue;
import com.example.codereview.model.IssueCategory;
import com.example.codereview.model.Severity;

import java.util.List;

class ConsoleLoggingRule extends BaseRule {
	@Override
	public List<Issue> apply(AnalysisContext context) {
		List<Issue> out = list();
		String[] lines = context.getLines();
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].contains("System.out.println")) {
				out.add(makeIssue(context.getReview(), i + 1,
						"Console logging",
						"System.out.println is used.",
						"Use a proper logging framework instead of System.out.println in production code.",
						IssueCategory.OPTIMIZATION, Severity.MEDIUM));
			}
		}
		return out;
	}

	@Override
	public String name() { return "console-logging"; }
}


