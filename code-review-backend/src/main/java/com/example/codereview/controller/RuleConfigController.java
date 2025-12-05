package com.example.codereview.controller;

import com.example.codereview.service.rules.RuleRegistry;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rules")
public class RuleConfigController {

	private final RuleRegistry registry;

	public RuleConfigController(RuleRegistry registry) {
		this.registry = registry;
	}

	@GetMapping
	public Map<String, Boolean> getRules() {
		return registry.getRuleStates();
	}

	@PutMapping
	public Map<String, Boolean> update(@RequestBody Map<String, Boolean> updates) {
		registry.setRuleStates(updates);
		return registry.getRuleStates();
	}
}


