package com.example.codereview.service.rules;

import com.example.codereview.model.Issue;
import com.example.codereview.model.IssueCategory;
import com.example.codereview.model.Severity;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class UnusedVariableRule extends BaseRule {
	private static final Pattern POTENTIAL_LOCAL_DECL = Pattern.compile(
			"^(\\s*)(final\\s+)?(?:(?:byte|short|int|long|float|double|boolean|char|String|var)\\b|[A-Z][A-Za-z0-9_<>\\[\\]?,\\.\\s]+)\\s+(.+?);\\s*$");

	@Override
	public List<Issue> apply(AnalysisContext context) {
		List<Issue> out = list();
		String[] lines = context.getLines();

		// Collect candidate local variable declarations: name -> line index
		Map<String, Integer> decls = new LinkedHashMap<>();
		Set<Integer> declLines = new HashSet<>();

		for (int i = 0; i < lines.length; i++) {
			String raw = lines[i];
			String l = stripLineComments(raw).trim();
			if (l.isEmpty()) continue;

			// Skip obvious non-local contexts
			if (l.startsWith("import ") || l.startsWith("package ")) continue;
			if (l.startsWith("class ") || l.startsWith("interface ") || l.startsWith("enum ")) continue;
			if (l.startsWith("public ") || l.startsWith("private ") || l.startsWith("protected ")) continue;
			if (l.startsWith("@")) continue; // annotations
			if (l.startsWith("for ") || l.startsWith("for(") || l.startsWith("catch ") || l.startsWith("catch(")) continue;

			Matcher m = POTENTIAL_LOCAL_DECL.matcher(l);
			if (!m.find()) continue;

			String varsPart = m.group(3);
			if (varsPart == null || varsPart.isEmpty()) continue;

			// Split declarations: int a, b = 2, c
			String[] parts = varsPart.split(",");
			for (String p : parts) {
				String seg = p.trim();
				if (seg.isEmpty()) continue;
				// variable name precedes '=' if present, else ends at first whitespace or array brackets
				String name = seg;
				int eq = seg.indexOf('=');
				if (eq >= 0) name = seg.substring(0, eq).trim();
				// remove array brackets if any (e.g., int[] a)
				name = name.replaceAll("\\[\\s*\\]", "").trim();
				// strip any trailing generics residue or var inference artifacts
				// now the last token should be the identifier
				String[] tokens = name.split("\\s+");
				name = tokens[tokens.length - 1];
				if (name.matches("[A-Za-z_][A-Za-z0-9_]*")) {
					decls.put(name, i);
					declLines.add(i);
				}
			}
		}

		if (decls.isEmpty()) return out;

		// Build a body that excludes declaration lines to avoid counting the declaration itself
		StringBuilder body = new StringBuilder();
		for (int i = 0; i < lines.length; i++) {
			if (declLines.contains(i)) continue;
			body.append(stripLineComments(lines[i])).append('\n');
		}
		String bodyStr = body.toString();

		for (Map.Entry<String, Integer> e : decls.entrySet()) {
			String var = e.getKey();
			int lineIdx = e.getValue();
			Pattern use = Pattern.compile("\\b" + Pattern.quote(var) + "\\b");
			Matcher um = use.matcher(bodyStr);
			if (!um.find()) {
				out.add(makeIssue(context.getReview(), lineIdx + 1,
						"Unused local variable",
						"The local variable '" + var + "' does not appear to be used.",
						"Remove the variable or use it; consider '_' prefix to indicate intentional unused.",
						IssueCategory.WARNING, Severity.LOW));
			}
		}
		return out;
	}

	@Override
	public String name() { return "unused-local-variable"; }

	private static String stripLineComments(String line) {
		int idx = line.indexOf("//");
		return idx >= 0 ? line.substring(0, idx) : line;
	}
}


