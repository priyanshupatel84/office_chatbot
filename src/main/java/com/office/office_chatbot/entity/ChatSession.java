package com.office.office_chatbot.entity;

import java.time.Instant;
import java.util.UUID;

public record ChatSession(UUID id, Instant createdAt, Instant updatedAt) { }
