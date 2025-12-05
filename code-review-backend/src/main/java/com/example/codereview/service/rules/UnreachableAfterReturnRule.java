package com.example.codereview.service.rules;

import com.example.codereview.model.Issue;
import com.example.codereview.model.IssueCategory;
import com.example.codereview.model.Severity;

import java.util.List;

class UnreachableAfterReturnRule extends BaseRule {
	@Override
	public List<Issue> apply(AnalysisContext context) {
		List<Issue> out = list();
		String[] lines = context.getLines();
		boolean sawExit = false; // return, throw, break, continue
		int braceBalance = 0;
		for (int i = 0; i < lines.length; i++) {
			String l = lines[i].trim();
			for (char c : l.toCharArray()) {
				if (c == '{') braceBalance++;
				else if (c == '}') braceBalance--;
			}
			// If we previously saw an exit statement, flag the next real statement in the same block
			if (sawExit && braceBalance >= 0 && !l.isEmpty() && !l.startsWith("}") && !isCaseLabel(l)) {
				out.add(makeIssue(context.getReview(), i + 1,
						"Unreachable code after control-flow exit",
						"Statements after return/throw/break/continue in the same block are unreachable.",
						"Remove or refactor unreachable statements.",
						IssueCategory.WARNING, Severity.MEDIUM));
				// reset to avoid flooding
				sawExit = false;
			}
			// Start tracking if current line is an exit statement
			if (startsWithReturn(l) || startsWithThrow(l) || startsWithBreakOrContinue(l)) {
				sawExit = true;
				continue;
			}
			// Reset on block close or switch case/default boundaries
			if (l.endsWith("}") || isCaseLabel(l)) {
				sawExit = false;
			}
		}
		return out;
	}

	@Override
	public String name() { return "unreachable-after-return"; }

	private static boolean startsWithReturn(String l) {
		return l.startsWith("return");
	}

	private static boolean startsWithThrow(String l) {
		return l.startsWith("throw ");
	}

	private static boolean startsWithBreakOrContinue(String l) {
		return l.startsWith("break") || l.startsWith("continue");
	}

	private static boolean isCaseLabel(String l) {
		return l.startsWith("case ") || l.startsWith("default:");
	}
}


