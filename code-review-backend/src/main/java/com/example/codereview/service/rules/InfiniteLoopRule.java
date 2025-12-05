package com.example.codereview.service.rules;

import com.example.codereview.model.Issue;
import com.example.codereview.model.IssueCategory;
import com.example.codereview.model.Severity;

import java.util.List;

class InfiniteLoopRule extends BaseRule {
	@Override
	public List<Issue> apply(AnalysisContext context) {
		List<Issue> out = list();
		String[] lines = context.getLines();

		boolean inBlockComment = false;
		for (int i = 0; i < lines.length; i++) {
			String raw = lines[i];
			String l = stripLineComments(raw);
			// Handle simple block comment toggling (best-effort)
			if (!inBlockComment && l.contains("/*")) {
				inBlockComment = true;
			}
			if (inBlockComment) {
				if (l.contains("*/")) {
					inBlockComment = false;
				}
				continue;
			}
			String trimmed = l.trim();
			if (trimmed.isEmpty()) continue;

			boolean whileTrue = trimmed.matches("^while\\s*\\(\\s*(?i:true)\\s*\\).*")
					|| trimmed.matches("^while\\s*\\(\\s*1\\s*==\\s*1\\s*\\).*")
					|| trimmed.matches("^while\\s*\\(\\s*0\\s*==\\s*0\\s*\\).*");
			boolean forEver = trimmed.matches("^for\\s*\\(\\s*;\\s*;\\s*\\).*");

			// do { ... } while (true);
			boolean doHeader = trimmed.startsWith("do");

			if (whileTrue || forEver) {
				// Case 1: Single-statement loop like 'while(true);'
				if (trimmed.endsWith(";") && !trimmed.contains("{")) {
					out.add(makeIssue(context.getReview(), i + 1,
							"Likely infinite loop",
							"This loop appears unconditional and immediately repeats.",
							"Ensure the loop has an exit condition or a break/return/throw path.",
							IssueCategory.ERROR, Severity.HIGH));
					continue;
				}
				// Case 2: Block loop – scan the block for exit statements
				int end = scanBlockEnd(lines, i);
				if (end == i) {
					// No block, but not semicolon – treat next statement
					int stmtEnd = findStatementEnd(lines, i + 1);
					if (!blockContainsExit(lines, i + 1, stmtEnd)) {
						out.add(makeIssue(context.getReview(), i + 1,
								"Likely infinite loop",
								"This loop appears unconditional without an exit in its body.",
								"Add a break/return/throw or a conditional exit.",
								IssueCategory.ERROR, Severity.HIGH));
					}
				} else {
					if (!blockContainsExit(lines, i + 1, end)) {
						out.add(makeIssue(context.getReview(), i + 1,
								"Likely infinite loop",
								"This loop appears unconditional and the body lacks an exit path.",
								"Add a break/return/throw or make the condition finite.",
								IssueCategory.ERROR, Severity.HIGH));
					}
					i = Math.max(i, end);
				}
			} else if (doHeader) {
				// Find the do { ... } while (true);
				int blockEnd = scanBlockEnd(lines, i);
				if (blockEnd > i) {
					// The while is typically on or after blockEnd
					int whileLine = Math.min(blockEnd + 1, lines.length - 1);
					boolean whileTrueAfter = false;
					for (int k = i + 1; k <= Math.min(lines.length - 1, blockEnd + 2); k++) {
						String t = stripLineComments(lines[k]).trim();
						if (t.startsWith("while")) {
							whileTrueAfter = t.matches("^while\\s*\\(\\s*(?i:true)\\s*\\)\\s*;.*")
									|| t.matches("^while\\s*\\(\\s*1\\s*==\\s*1\\s*\\)\\s*;.*")
									|| t.matches("^while\\s*\\(\\s*0\\s*==\\s*0\\s*\\)\\s*;.*");
							break;
						}
					}
					if (whileTrueAfter && !blockContainsExit(lines, i + 1, blockEnd)) {
						out.add(makeIssue(context.getReview(), i + 1,
								"Likely infinite loop",
								"This do-while loop is unconditional and has no exit path.",
								"Add a break/return/throw or make the condition finite.",
								IssueCategory.ERROR, Severity.HIGH));
					}
					i = blockEnd;
				}
			}
		}
		return out;
	}

	@Override
	public String name() { return "infinite-loop"; }

	private static String stripLineComments(String line) {
		int idx = line.indexOf("//");
		return idx >= 0 ? line.substring(0, idx) : line;
	}

	private static int scanBlockEnd(String[] lines, int headerIndex) {
		String header = stripLineComments(lines[headerIndex]);
		int braceBalance = 0;
		boolean sawOpening = false;
		for (int j = 0; j < header.length(); j++) {
			char c = header.charAt(j);
			if (c == '{') {
				braceBalance++;
				sawOpening = true;
			} else if (c == '}') {
				braceBalance--;
			}
		}
		if (!sawOpening) {
			// If next non-empty line starts with '{', start block from there
			for (int i = headerIndex + 1; i < lines.length; i++) {
				String t = stripLineComments(lines[i]).trim();
				if (t.isEmpty()) continue;
				if (t.startsWith("{")) {
					braceBalance = 1;
					headerIndex = i;
					break;
				} else {
					// No block
					return headerIndex;
				}
			}
		}
		for (int i = headerIndex + 1; i < lines.length; i++) {
			String l = stripLineComments(lines[i]);
			for (int k = 0; k < l.length(); k++) {
				char c = l.charAt(k);
				if (c == '{') braceBalance++;
				else if (c == '}') braceBalance--;
			}
			if (braceBalance <= 0) return i;
		}
		return headerIndex;
	}

	private static boolean blockContainsExit(String[] lines, int startInclusive, int endInclusive) {
		for (int i = startInclusive; i <= endInclusive && i >= 0 && i < lines.length; i++) {
			String t = stripLineComments(lines[i]).trim();
			if (t.isEmpty()) continue;
			// crude but effective: presence of a control-flow exit
			if (t.contains("break;") || t.startsWith("return") || t.startsWith("throw ")) {
				return true;
			}
		}
		return false;
	}

	private static int findStatementEnd(String[] lines, int start) {
		for (int i = start; i < lines.length; i++) {
			String t = stripLineComments(lines[i]).trim();
			if (t.endsWith(";") || t.endsWith("}")) return i;
		}
		return Math.min(start, lines.length - 1);
	}
}


