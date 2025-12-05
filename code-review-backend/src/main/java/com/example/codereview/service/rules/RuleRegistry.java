package com.example.codereview.service.rules;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class RuleRegistry {
	private final Map<String, AnalysisRule> rulesByName = new LinkedHashMap<>();

	public RuleRegistry() {
		// Register default rules in a deterministic order
		register(new TodoRule());
		register(new ConsoleLoggingRule());
		register(new SqlConcatenationRule());
		register(new LongLineRule());
		register(new UnmatchedBracesRule());
		register(new EmptyCatchBlockRule());
		register(new HardcodedSecretRule());
		register(new NestedLoopRule());
		register(new StringConcatInLoopRule());
		register(new UnreachableAfterReturnRule());
		register(new InfiniteLoopRule());
		register(new UnusedVariableRule());
		register(new UnusedImportRule());
	}

	private void register(AnalysisRule rule) {
		rulesByName.put(rule.name(), rule);
	}

	public List<AnalysisRule> getEnabledRules() {
		List<AnalysisRule> list = new ArrayList<>();
		for (AnalysisRule r : rulesByName.values()) {
			if (r.isEnabled()) list.add(r);
		}
		return list;
	}

	public Map<String, Boolean> getRuleStates() {
		Map<String, Boolean> out = new LinkedHashMap<>();
		for (Map.Entry<String, AnalysisRule> e : rulesByName.entrySet()) {
			out.put(e.getKey(), e.getValue().isEnabled());
		}
		return Collections.unmodifiableMap(out);
	}

	public void setRuleStates(Map<String, Boolean> updates) {
		for (Map.Entry<String, Boolean> e : updates.entrySet()) {
			AnalysisRule rule = rulesByName.get(e.getKey());
			if (rule != null && e.getValue() != null) {
				rule.setEnabled(e.getValue());
			}
		}
	}
}


