package com.office.office_chatbot.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.office.office_chatbot.dto.ChatRequest;
import com.office.office_chatbot.dto.ChatResponse;
import com.office.office_chatbot.service.ChatService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
	private final ChatService chatService;

	public ChatController(ChatService chatService) {
		this.chatService = chatService;
	}

	@PostMapping
	public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
		return chatService.chat(request);
	}
}
