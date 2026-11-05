package com.office.office_chatbot.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.office.office_chatbot.config.ChatbotConfig;
import com.office.office_chatbot.exception.OllamaException;

@Service
public class OllamaChatService {
	private final WebClient ollama;
	private final ChatbotConfig config;

	public OllamaChatService(WebClient ollamaWebClient, ChatbotConfig config) {
		this.ollama = ollamaWebClient;
		this.config = config;
	}

	public String generate(String prompt) {
		try {
			Map<String, Object> message = Map.of("role", "system", "content", prompt);
			Map<String, Object> request = Map.of("model", config.getOllama().getChatModel(),
					"messages", List.of(message), "stream", false,
					"options", Map.of("temperature", 0.1));
			JsonNode response = ollama.post().uri("/api/chat").bodyValue(request).retrieve()
					.bodyToMono(JsonNode.class)
					.block(Duration.ofSeconds(config.getOllama().getTimeoutSeconds()));
			String answer = response == null ? null : response.path("message").path("content").asText(null);
			if (answer == null || answer.isBlank()) {
				throw new OllamaException("Ollama returned an empty chat response");
			}
			return answer.strip();
		} catch (OllamaException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new OllamaException("Could not generate a response using Ollama", exception);
		}
	}
}
