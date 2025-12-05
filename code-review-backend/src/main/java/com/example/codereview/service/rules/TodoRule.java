package com.example.codereview.service.rules;

import com.example.codereview.model.Issue;
import com.example.codereview.model.IssueCategory;
import com.example.codereview.model.Severity;

import java.util.List;

class TodoRule extends BaseRule {
	@Override
	public List<Issue> apply(AnalysisContext context) {
		List<Issue> out = list();
		String[] lines = context.getLines();
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].contains("TODO")) {
				out.add(makeIssue(context.getReview(), i + 1,
						"TODO comment found",
						"There is a TODO comment in the code.",
						"Finish or remove TODO comments to keep the code clean.",
						IssueCategory.WARNING, Severity.LOW));
			}
		}
		return out;
	}

	@Override
	public String name() { return "To do comment search"; }
}


