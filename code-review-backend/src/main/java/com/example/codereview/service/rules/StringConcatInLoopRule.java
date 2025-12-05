package com.example.codereview.service.rules;

import com.example.codereview.model.Issue;
import com.example.codereview.model.IssueCategory;
import com.example.codereview.model.Severity;

import java.util.List;

class StringConcatInLoopRule extends BaseRule {
	@Override
	public List<Issue> apply(AnalysisContext context) {
		List<Issue> out = list();
		String[] lines = context.getLines();
		boolean inLoop = false;
		int braceDepthAtLoop = 0;
		for (int i = 0; i < lines.length; i++) {
			String l = lines[i];
			if (l.contains("for(") || l.contains("for (") || l.contains("while(") || l.contains("while (")) {
				inLoop = true;
				braceDepthAtLoop = countBraceDelta(l);
			}
			if (inLoop) {
				if (l.contains("+=") || l.contains("+") && l.contains("\"")) {
					out.add(makeIssue(context.getReview(), i + 1,
							"String concatenation in loop",
							"String concatenation inside loops can be inefficient.",
							"Use StringBuilder or collect results and join afterwards.",
							IssueCategory.OPTIMIZATION, Severity.LOW));
				}
				braceDepthAtLoop += countBraceDelta(l);
				if (braceDepthAtLoop <= 0) {
					inLoop = false;
				}
			}
		}
		return out;
	}

	private int countBraceDelta(String s) {
		int delta = 0;
		for (char c : s.toCharArray()) {
			if (c == '{') delta++;
			else if (c == '}') delta--;
		}
		return delta;
	}

	@Override
	public String name() { return "string-concat-in-loop"; }
}


