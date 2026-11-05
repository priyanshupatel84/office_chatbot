package com.office.office_chatbot.entity;

import java.time.Instant;
import java.util.UUID;

public record ChatMessage(long id, UUID sessionId, String role, String content, Instant createdAt) { }
