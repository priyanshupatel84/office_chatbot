package com.office.office_chatbot.repository;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.office.office_chatbot.entity.ChatMessage;

@Repository
public class ChatRepository {
	private final JdbcTemplate jdbc;

	public ChatRepository(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public void createSession(UUID id) {
		jdbc.update("INSERT INTO chat_sessions (id) VALUES (?)", id);
	}

	public boolean sessionExists(UUID id) {
		Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM chat_sessions WHERE id = ?", Integer.class, id);
		return count != null && count > 0;
	}

	public void addMessage(UUID sessionId, String role, String content) {
		jdbc.update("INSERT INTO chat_messages (session_id, role, content) VALUES (?, ?, ?)", sessionId, role, content);
		jdbc.update("UPDATE chat_sessions SET updated_at = CURRENT_TIMESTAMP WHERE id = ?", sessionId);
	}

	public List<ChatMessage> recentMessages(UUID sessionId, int limit) {
		List<ChatMessage> messages = jdbc.query("""
				SELECT id, session_id, role, content, created_at
				FROM chat_messages WHERE session_id = ?
				ORDER BY created_at DESC, id DESC LIMIT ?
				""", (rs, row) -> new ChatMessage(rs.getLong("id"), rs.getObject("session_id", UUID.class),
					rs.getString("role"), rs.getString("content"), rs.getTimestamp("created_at").toInstant()),
				sessionId, limit);
		Collections.reverse(messages);
		return messages;
	}

	public List<ChatMessage> allMessages(UUID sessionId) {
		return jdbc.query("""
				SELECT id, session_id, role, content, created_at
				FROM chat_messages WHERE session_id = ? ORDER BY created_at, id
				""", (rs, row) -> new ChatMessage(rs.getLong("id"), rs.getObject("session_id", UUID.class),
					rs.getString("role"), rs.getString("content"), rs.getTimestamp("created_at").toInstant()), sessionId);
	}

	public boolean deleteSession(UUID sessionId) {
		return jdbc.update("DELETE FROM chat_sessions WHERE id = ?", sessionId) > 0;
	}
}
