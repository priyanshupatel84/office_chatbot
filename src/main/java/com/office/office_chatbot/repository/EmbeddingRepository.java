package com.office.office_chatbot.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.office.office_chatbot.entity.Knowledge;

@Repository
public class EmbeddingRepository {
	private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() { };
	private final JdbcTemplate jdbc;
	private final ObjectMapper objectMapper;

	public EmbeddingRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
		this.jdbc = jdbc;
		this.objectMapper = objectMapper;
	}

	public void upsert(long knowledgeId, String content, List<Double> embedding) {
		String sql = """
				INSERT INTO knowledge_embeddings (knowledge_id, content, embedding)
				VALUES (?, ?, ?::vector)
				ON CONFLICT (knowledge_id) DO UPDATE SET
					content = EXCLUDED.content,
					embedding = EXCLUDED.embedding,
					created_at = CURRENT_TIMESTAMP
				""";
		jdbc.update(sql, knowledgeId, content, toVector(embedding));
	}

	public List<Match> search(List<Double> queryEmbedding, int limit) {
		String sql = """
				SELECT k.*, e.content,
					1 - (e.embedding <=> ?::vector) AS similarity
				FROM knowledge_embeddings e
				JOIN knowledge_base k ON k.id = e.knowledge_id
				ORDER BY e.embedding <=> ?::vector
				LIMIT ?
				""";
		String vector = toVector(queryEmbedding);
		return jdbc.query(sql, (rs, row) -> {
			Knowledge knowledge = new Knowledge(rs.getLong("id"), rs.getString("scenario"),
					readJson(rs.getString("user_queries")), rs.getString("solution"), readJson(rs.getString("tags")),
					rs.getString("content_hash"), rs.getTimestamp("created_at").toInstant(),
					rs.getTimestamp("updated_at").toInstant());
			return new Match(knowledge, rs.getString("content"), rs.getDouble("similarity"));
		}, vector, vector, limit);
	}

	private String toVector(List<Double> values) {
		return values.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",", "[", "]"));
	}

	private List<String> readJson(String value) {
		try {
			return objectMapper.readValue(value, STRING_LIST);
		} catch (Exception exception) {
			throw new IllegalStateException("Could not read knowledge data from the database", exception);
		}
	}

	public record Match(Knowledge knowledge, String content, double similarity) { }
}
