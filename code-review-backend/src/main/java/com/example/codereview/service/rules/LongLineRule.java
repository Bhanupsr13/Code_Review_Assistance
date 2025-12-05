package com.example.codereview.service.rules;

import com.example.codereview.model.Issue;
import com.example.codereview.model.IssueCategory;
import com.example.codereview.model.Severity;

import java.util.List;

class LongLineRule extends BaseRule {
	private static final int MAX_LEN = 120;

	@Override
	public List<Issue> apply(AnalysisContext context) {
		List<Issue> out = list();
		String[] lines = context.getLines();
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].length() > MAX_LEN) {
				out.add(makeIssue(context.getReview(), i + 1,
						"Long line",
						"This line is very long.",
						"Break long lines into smaller chunks for better readability.",
						IssueCategory.OPTIMIZATION, Severity.LOW));
			}
		}
		return out;
	}

	@Override
	public String name() { return "long-line"; }
}


