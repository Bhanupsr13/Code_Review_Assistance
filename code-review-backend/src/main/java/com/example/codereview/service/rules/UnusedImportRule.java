package com.example.codereview.service.rules;

import com.example.codereview.model.Issue;
import com.example.codereview.model.IssueCategory;
import com.example.codereview.model.Severity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class UnusedImportRule extends BaseRule {
	@Override
	public List<Issue> apply(AnalysisContext context) {
		List<Issue> out = list();
		String[] lines = context.getLines();
		List<Integer> importLines = new ArrayList<>();
		Set<String> simpleNames = new HashSet<>();
		StringBuilder body = new StringBuilder();
		for (int i = 0; i < lines.length; i++) {
			String l = lines[i].trim();
			if (l.startsWith("import ") && l.endsWith(";") && !l.contains("*")) {
				importLines.add(i);
				String qn = l.substring("import ".length(), l.length() - 1).trim();
				int lastDot = qn.lastIndexOf('.');
				if (lastDot > 0 && lastDot + 1 < qn.length()) {
					String simple = qn.substring(lastDot + 1);
					simpleNames.add(simple);
				}
			} else {
				body.append(l).append('\n');
			}
		}
		String bodyStr = body.toString();
		for (int idx : importLines) {
			String l = lines[idx].trim();
			String qn = l.substring("import ".length(), l.length() - 1).trim();
			int lastDot = qn.lastIndexOf('.');
			if (lastDot > 0 && lastDot + 1 < qn.length()) {
				String simple = qn.substring(lastDot + 1);
				if (!bodyStr.contains(simple)) {
					out.add(makeIssue(context.getReview(), idx + 1,
							"Unused import",
							"The import '" + qn + "' does not appear to be used.",
							"Remove unused imports to keep code clean.",
							IssueCategory.WARNING, Severity.LOW));
				}
			}
		}
		return out;
	}

	@Override
	public String name() { return "unused-import"; }
}


