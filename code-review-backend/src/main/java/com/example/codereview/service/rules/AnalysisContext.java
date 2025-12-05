package com.example.codereview.service.rules;

import com.example.codereview.model.CodeReview;

public class AnalysisContext {
	private final CodeReview review;
	private final String code;
	private final String[] lines;

	public AnalysisContext(CodeReview review, String code, String[] lines) {
		this.review = review;
		this.code = code;
		this.lines = lines;
	}

	public CodeReview getReview() {
		return review;
	}

	public String getCode() {
		return code;
	}

	public String[] getLines() {
		return lines;
	}
}


