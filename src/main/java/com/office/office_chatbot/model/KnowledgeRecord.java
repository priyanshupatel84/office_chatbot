package com.office.office_chatbot.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KnowledgeRecord(
		String scenario,
		@JsonProperty("user_queries") List<String> userQueries,
		String solution,
		List<String> tags) { }
