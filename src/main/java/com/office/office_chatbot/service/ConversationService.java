package com.office.office_chatbot.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.office.office_chatbot.entity.ChatMessage;
import com.office.office_chatbot.exception.ResourceNotFoundException;
import com.office.office_chatbot.repository.ChatRepository;

@Service
public class ConversationService {
	private final ChatRepository chatRepository;

	public ConversationService(ChatRepository chatRepository) {
		this.chatRepository = chatRepository;
	}

	public List<ChatMessage> history(UUID sessionId) {
		ensureSessionExists(sessionId);
		return chatRepository.allMessages(sessionId);
	}

	public void delete(UUID sessionId) {
		if (!chatRepository.deleteSession(sessionId)) {
			throw new ResourceNotFoundException("Chat session was not found");
		}
	}

	public void ensureSessionExists(UUID sessionId) {
		if (!chatRepository.sessionExists(sessionId)) {
			throw new ResourceNotFoundException("Chat session was not found");
		}
	}
}
