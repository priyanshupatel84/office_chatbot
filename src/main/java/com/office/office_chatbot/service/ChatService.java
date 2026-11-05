package com.office.office_chatbot.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.office.office_chatbot.config.ChatbotConfig;
import com.office.office_chatbot.dto.ChatRequest;
import com.office.office_chatbot.dto.ChatResponse;
import com.office.office_chatbot.dto.SourceDTO;
import com.office.office_chatbot.entity.ChatMessage;
import com.office.office_chatbot.exception.ResourceNotFoundException;
import com.office.office_chatbot.repository.ChatRepository;
import com.office.office_chatbot.repository.EmbeddingRepository.Match;

@Service
public class ChatService {
	private static final String NO_CONTEXT_ANSWER = "I could not find relevant documentation in the current knowledge base, so I cannot provide a grounded answer. Please rephrase your question or contact the support team.";
	private final ChatRepository chatRepository;
	private final RetrievalService retrievalService;
	private final OllamaChatService ollamaChatService;
	private final ChatbotConfig config;
	private final String promptTemplate;

	public ChatService(ChatRepository chatRepository, RetrievalService retrievalService,
			OllamaChatService ollamaChatService, ChatbotConfig config, ResourceLoader resourceLoader) {
		this.chatRepository = chatRepository;
		this.retrievalService = retrievalService;
		this.ollamaChatService = ollamaChatService;
		this.config = config;
		Resource prompt = resourceLoader.getResource(config.getPrompt().getLocation());
		try (var input = prompt.getInputStream()) {
			this.promptTemplate = new String(input.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not load the configured cloud support prompt", exception);
		}
	}

	public ChatResponse chat(ChatRequest request) {
		if (request.message() == null || request.message().isBlank()) {
			throw new IllegalArgumentException("message must not be blank");
		}
		UUID sessionId = request.sessionId();
		if (sessionId == null) {
			sessionId = UUID.randomUUID();
			chatRepository.createSession(sessionId);
		} else if (!chatRepository.sessionExists(sessionId)) {
			throw new ResourceNotFoundException("Chat session was not found");
		}

		List<ChatMessage> history = chatRepository.recentMessages(sessionId, config.getConversation().getHistoryLimit());
		chatRepository.addMessage(sessionId, "user", request.message().strip());
		List<Match> matches = retrievalService.retrieve(request.message().strip(), history);
		String answer = matches.isEmpty() ? NO_CONTEXT_ANSWER
				: ollamaChatService.generate(buildPrompt(matches, history, request.message().strip()));
		chatRepository.addMessage(sessionId, "assistant", answer);

		List<SourceDTO> sources = matches.stream()
				.map(match -> new SourceDTO(match.knowledge().id(), match.knowledge().scenario(), match.similarity()))
				.toList();
		return new ChatResponse(sessionId, answer, sources);
	}

	private String buildPrompt(List<Match> matches, List<ChatMessage> history, String question) {
		String context = matches.stream()
				.map(match -> "Source scenario: " + match.knowledge().scenario() + "\n" + match.content())
				.collect(Collectors.joining("\n\n---\n\n"));
		String conversation = history.stream()
				.map(message -> message.role().toUpperCase() + ": " + message.content())
				.collect(Collectors.joining("\n"));
		return promptTemplate.replace("{context}", context)
				.replace("{history}", conversation.isBlank() ? "No prior conversation." : conversation)
				.replace("{question}", question);
	}
}
