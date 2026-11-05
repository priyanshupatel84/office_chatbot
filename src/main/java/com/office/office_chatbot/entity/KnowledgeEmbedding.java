package com.office.office_chatbot.entity;

import java.time.Instant;
import java.util.List;

public record KnowledgeEmbedding(long id, long knowledgeId, String content, List<Double> embedding, Instant createdAt) { }
