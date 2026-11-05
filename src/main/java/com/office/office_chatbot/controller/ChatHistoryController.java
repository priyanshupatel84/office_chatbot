package com.office.office_chatbot.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.office.office_chatbot.entity.ChatMessage;
import com.office.office_chatbot.service.ConversationService;

@RestController
@RequestMapping("/api/chat/history")
public class ChatHistoryController {
	private final ConversationService conversationService;

	public ChatHistoryController(ConversationService conversationService) {
		this.conversationService = conversationService;
	}

	@GetMapping("/{sessionId}")
	public List<ChatMessage> history(@PathVariable UUID sessionId) {
		return conversationService.history(sessionId);
	}

	@DeleteMapping("/{sessionId}")
	public ResponseEntity<Void> delete(@PathVariable UUID sessionId) {
		conversationService.delete(sessionId);
		return ResponseEntity.noContent().build();
	}
}
