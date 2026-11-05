package com.office.office_chatbot.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.office.office_chatbot.config.ChatbotConfig;
import com.office.office_chatbot.dto.IngestionResult;
import com.office.office_chatbot.entity.Knowledge;
import com.office.office_chatbot.exception.KnowledgeFileException;
import com.office.office_chatbot.model.KnowledgeRecord;
import com.office.office_chatbot.repository.KnowledgeRepository;
import com.office.office_chatbot.service.KnowledgePersistenceService.PreparedRecord;

@Service
public class KnowledgeIngestionService {
	private final ObjectMapper objectMapper;
	private final ChatbotConfig config;
	private final EmbeddingService embeddingService;
	private final KnowledgeRepository knowledgeRepository;
	private final KnowledgePersistenceService persistenceService;

	public KnowledgeIngestionService(ObjectMapper objectMapper, ChatbotConfig config,
			EmbeddingService embeddingService, KnowledgeRepository knowledgeRepository,
			KnowledgePersistenceService persistenceService) {
		this.objectMapper = objectMapper;
		this.config = config;
		this.embeddingService = embeddingService;
		this.knowledgeRepository = knowledgeRepository;
		this.persistenceService = persistenceService;
	}

	public IngestionResult ingest() {
		Path path = Path.of(config.getKnowledge().getFile());
		JsonNode root;
		try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			root = objectMapper.readTree(reader);
		} catch (IOException exception) {
			throw new KnowledgeFileException("Could not read the configured knowledge file: " + path, exception);
		}
		if (root == null || !root.isArray()) {
			throw new KnowledgeFileException("The configured knowledge file must contain a JSON array");
		}

		List<PreparedRecord> prepared = new ArrayList<>();
		List<String> errors = new ArrayList<>();
		for (int index = 0; index < root.size(); index++) {
			KnowledgeRecord record;
			try {
				record = objectMapper.treeToValue(root.get(index), KnowledgeRecord.class);
				validate(record);
			} catch (JsonProcessingException | IllegalArgumentException exception) {
				errors.add("Record " + (index + 1) + ": " + exception.getMessage());
				continue;
			}
			String content = searchableText(record);
			prepared.add(new PreparedRecord(record, contentHash(record.scenario()), content,
					embeddingService.embedDocument(content)));
		}
		persistenceService.persist(prepared);
		return new IngestionResult(prepared.size(), errors.size(), errors);
	}

	public IngestionResult reindex() {
		List<PreparedRecord> prepared = new ArrayList<>();
		for (Knowledge knowledge : knowledgeRepository.findAll()) {
			KnowledgeRecord record = new KnowledgeRecord(knowledge.scenario(), knowledge.userQueries(),
					knowledge.solution(), knowledge.tags());
			String content = searchableText(record);
			prepared.add(new PreparedRecord(record, knowledge.contentHash(), content,
					embeddingService.embedDocument(content)));
		}
		persistenceService.persist(prepared);
		return new IngestionResult(prepared.size(), 0, List.of());
	}

	public static String searchableText(KnowledgeRecord record) {
		return "Scenario: " + record.scenario()
				+ "\n\nUser Queries: " + String.join("; ", record.userQueries())
				+ "\n\nSolution: " + record.solution()
				+ "\n\nTags: " + String.join(", ", record.tags());
	}

	private void validate(KnowledgeRecord record) {
		if (record == null || record.scenario() == null || record.scenario().isBlank()
				|| record.solution() == null || record.solution().isBlank()
				|| record.userQueries() == null || record.userQueries().isEmpty()
				|| record.userQueries().stream().anyMatch(value -> value == null || value.isBlank())
				|| record.tags() == null || record.tags().isEmpty()
				|| record.tags().stream().anyMatch(value -> value == null || value.isBlank())) {
			throw new IllegalArgumentException("scenario, solution, user_queries, and tags are required");
		}
	}

	private String contentHash(String scenario) {
		try {
			String stableKey = Normalizer.normalize(scenario.strip().toLowerCase(Locale.ROOT), Normalizer.Form.NFKC)
					.replaceAll("\\s+", " ");
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(stableKey.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (Exception exception) {
			throw new IllegalStateException("Could not generate knowledge record identifier", exception);
		}
	}
}
