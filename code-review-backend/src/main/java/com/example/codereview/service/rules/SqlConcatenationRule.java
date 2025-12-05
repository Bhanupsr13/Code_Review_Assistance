package com.example.codereview.service.rules;

import com.example.codereview.model.Issue;
import com.example.codereview.model.IssueCategory;
import com.example.codereview.model.Severity;

import java.util.List;

class SqlConcatenationRule extends BaseRule {
	@Override
	public List<Issue> apply(AnalysisContext context) {
		List<Issue> out = list();
		String[] lines = context.getLines();
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line.toLowerCase().contains("select ")
					&& line.contains("\"")
					&& line.contains("+")) {
				out.add(makeIssue(context.getReview(), i + 1,
						"Possible SQL Injection",
						"SQL query is built using string concatenation.",
						"Use PreparedStatement or parameterized queries to avoid SQL injection.",
						IssueCategory.SECURITY, Severity.HIGH));
			}
		}
		return out;
	}

	@Override
	public String name() { return "sql-string-concatenation"; }
}


