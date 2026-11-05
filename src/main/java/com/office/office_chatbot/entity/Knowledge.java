package com.office.office_chatbot.entity;

import java.time.Instant;
import java.util.List;

public record Knowledge(long id, String scenario, List<String> userQueries, String solution, List<String> tags,
		String contentHash, Instant createdAt, Instant updatedAt) { }
