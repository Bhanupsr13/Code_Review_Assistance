package com.example.codereview.service.rules;

import com.example.codereview.model.Issue;
import com.example.codereview.model.IssueCategory;
import com.example.codereview.model.Severity;

import java.util.List;

class NestedLoopRule extends BaseRule {
	@Override
	public List<Issue> apply(AnalysisContext context) {
		List<Issue> out = list();
		String[] lines = context.getLines();
		int nesting = 0;
		for (int i = 0; i < lines.length; i++) {
			String l = lines[i];
			if (l.contains("for(") || l.contains("for (") || l.contains("while(") || l.contains("while (")) {
				nesting++;
				if (nesting >= 2) {
					out.add(makeIssue(context.getReview(), i + 1,
							"Nested loop detected",
							"Nested loops can cause performance issues on large inputs.",
							"Consider reducing algorithmic complexity or breaking out loops.",
							IssueCategory.OPTIMIZATION, Severity.MEDIUM));
				}
			}
			// track braces to roughly detect exiting loops
			if (l.contains("}")) {
				if (nesting > 0) nesting--;
			}
		}
		return out;
	}

	@Override
	public String name() { return "nested-loop"; }
}


