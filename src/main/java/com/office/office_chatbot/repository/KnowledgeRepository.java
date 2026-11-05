package com.office.office_chatbot.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.office.office_chatbot.entity.Knowledge;
import com.office.office_chatbot.model.KnowledgeRecord;

@Repository
public class KnowledgeRepository {
	private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() { };
	private final JdbcTemplate jdbc;
	private final ObjectMapper objectMapper;

	public KnowledgeRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
		this.jdbc = jdbc;
		this.objectMapper = objectMapper;
	}

	public long upsert(KnowledgeRecord record, String contentHash) {
		String sql = """
				INSERT INTO knowledge_base (scenario, user_queries, solution, tags, content_hash)
				VALUES (?, ?::jsonb, ?, ?::jsonb, ?)
				ON CONFLICT (content_hash) DO UPDATE SET
					scenario = EXCLUDED.scenario,
					user_queries = EXCLUDED.user_queries,
					solution = EXCLUDED.solution,
					tags = EXCLUDED.tags,
					updated_at = CURRENT_TIMESTAMP
				RETURNING id
				""";
		return jdbc.queryForObject(sql, Long.class, record.scenario(), writeJson(record.userQueries()),
				record.solution(), writeJson(record.tags()), contentHash);
	}

	public Knowledge findById(long id) {
		return jdbc.query("SELECT * FROM knowledge_base WHERE id = ?", (rs, row) -> mapKnowledge(rs), id)
				.stream().findFirst().orElse(null);
	}

	public List<Knowledge> findPage(int limit, int offset) {
		return jdbc.query("SELECT * FROM knowledge_base ORDER BY id LIMIT ? OFFSET ?",
				(rs, row) -> mapKnowledge(rs), limit, offset);
	}

	public List<Knowledge> findAll() {
		return jdbc.query("SELECT * FROM knowledge_base ORDER BY id", (rs, row) -> mapKnowledge(rs));
	}

	public long count() {
		return jdbc.queryForObject("SELECT COUNT(*) FROM knowledge_base", Long.class);
	}

	public boolean delete(long id) {
		return jdbc.update("DELETE FROM knowledge_base WHERE id = ?", id) > 0;
	}

	private Knowledge mapKnowledge(java.sql.ResultSet rs) throws java.sql.SQLException {
		return new Knowledge(rs.getLong("id"), rs.getString("scenario"), readJson(rs.getString("user_queries")),
				rs.getString("solution"), readJson(rs.getString("tags")), rs.getString("content_hash"),
				rs.getTimestamp("created_at").toInstant(), rs.getTimestamp("updated_at").toInstant());
	}

	private String writeJson(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (Exception exception) {
			throw new IllegalStateException("Could not serialize knowledge data", exception);
		}
	}

	private List<String> readJson(String value) {
		try {
			return objectMapper.readValue(value, STRING_LIST);
		} catch (Exception exception) {
			throw new IllegalStateException("Could not read knowledge data from the database", exception);
		}
	}
}
