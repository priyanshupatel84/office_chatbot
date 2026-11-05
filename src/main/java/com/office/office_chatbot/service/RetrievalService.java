package com.office.office_chatbot.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.office.office_chatbot.config.ChatbotConfig;
import com.office.office_chatbot.entity.ChatMessage;
import com.office.office_chatbot.repository.EmbeddingRepository;
import com.office.office_chatbot.repository.EmbeddingRepository.Match;

@Service
public class RetrievalService {
	private final EmbeddingService embeddingService;
	private final EmbeddingRepository embeddingRepository;
	private final ChatbotConfig config;

	public RetrievalService(EmbeddingService embeddingService, EmbeddingRepository embeddingRepository,
			ChatbotConfig config) {
		this.embeddingService = embeddingService;
		this.embeddingRepository = embeddingRepository;
		this.config = config;
	}

	public List<Match> retrieve(String question, List<ChatMessage> history) {
		String retrievalQuery = question;
		if (!history.isEmpty()) {
			String recentContext = history.stream()
					.map(message -> message.role() + ": " + message.content())
					.collect(java.util.stream.Collectors.joining("\n"));
			retrievalQuery = "Conversation context:\n" + recentContext + "\nCurrent question: " + question;
		}
		List<Double> vector = embeddingService.embedQuery(retrievalQuery);
		return embeddingRepository.search(vector, config.getRetrieval().getTopK()).stream()
				.filter(match -> match.similarity() >= config.getRetrieval().getSimilarityThreshold())
				.toList();
	}
}
