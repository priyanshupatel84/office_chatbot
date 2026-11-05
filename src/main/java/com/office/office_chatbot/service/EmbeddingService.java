package com.office.office_chatbot.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.office.office_chatbot.config.ChatbotConfig;
import com.office.office_chatbot.exception.OllamaException;

@Service
public class EmbeddingService {
	private final WebClient ollama;
	private final ChatbotConfig config;

	public EmbeddingService(WebClient ollamaWebClient, ChatbotConfig config) {
		this.ollama = ollamaWebClient;
		this.config = config;
	}

	public List<Double> embedDocument(String searchableText) {
		return embed("search_document: " + searchableText);
	}

	public List<Double> embedQuery(String question) {
		return embed("search_query: " + question);
	}

	private List<Double> embed(String input) {
		try {
			JsonNode response = ollama.post().uri("/api/embed")
					.bodyValue(Map.of("model", config.getEmbedding().getModel(), "input", input))
					.retrieve().bodyToMono(JsonNode.class)
					.block(Duration.ofSeconds(config.getOllama().getTimeoutSeconds()));
			JsonNode vector = response == null ? null : response.path("embeddings").path(0);
			if (vector == null || !vector.isArray()) {
				throw new OllamaException("Ollama returned no embedding vector");
			}
			List<Double> values = new ArrayList<>(vector.size());
			for (JsonNode value : vector) {
				double number = value.asDouble(Double.NaN);
				if (!Double.isFinite(number)) {
					throw new OllamaException("Ollama returned an invalid embedding value");
				}
				values.add(number);
			}
			if (values.size() != config.getEmbedding().getDimensions()) {
				throw new OllamaException("Embedding dimension mismatch: expected "
						+ config.getEmbedding().getDimensions() + " but received " + values.size());
			}
			return values;
		} catch (OllamaException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new OllamaException("Could not generate an embedding using Ollama", exception);
		}
	}
}
