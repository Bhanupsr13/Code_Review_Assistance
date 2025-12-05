package com.example.codereview.service.rules;

import com.example.codereview.model.Issue;
import com.example.codereview.model.IssueCategory;
import com.example.codereview.model.Severity;

import java.util.List;
import java.util.regex.Pattern;

class HardcodedSecretRule extends BaseRule {
	private static final Pattern SECRET_PAT = Pattern.compile("(?i)(password\\s*=|api[_-]?key\\s*=|secret\\s*=|AKIA[0-9A-Z]{16})");

	@Override
	public List<Issue> apply(AnalysisContext context) {
		List<Issue> out = list();
		String[] lines = context.getLines();
		for (int i = 0; i < lines.length; i++) {
			if (SECRET_PAT.matcher(lines[i]).find()) {
				out.add(makeIssue(context.getReview(), i + 1,
						"Hardcoded secret detected",
						"Potentially sensitive credential detected in code.",
						"Do not commit secrets. Use environment variables or a secrets manager.",
						IssueCategory.SECURITY, Severity.HIGH));
			}
		}
		return out;
	}

	@Override
	public String name() { return "hardcoded-secret"; }
}


