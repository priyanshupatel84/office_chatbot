package com.office.office_chatbot.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.office.office_chatbot.model.KnowledgeRecord;
import com.office.office_chatbot.repository.EmbeddingRepository;
import com.office.office_chatbot.repository.KnowledgeRepository;

@Service
public class KnowledgePersistenceService {
	private final KnowledgeRepository knowledgeRepository;
	private final EmbeddingRepository embeddingRepository;

	public KnowledgePersistenceService(KnowledgeRepository knowledgeRepository, EmbeddingRepository embeddingRepository) {
		this.knowledgeRepository = knowledgeRepository;
		this.embeddingRepository = embeddingRepository;
	}

	@Transactional
	public void persist(List<PreparedRecord> records) {
		for (PreparedRecord record : records) {
			long knowledgeId = knowledgeRepository.upsert(record.knowledge(), record.contentHash());
			embeddingRepository.upsert(knowledgeId, record.content(), record.embedding());
		}
	}

	public record PreparedRecord(KnowledgeRecord knowledge, String contentHash, String content,
			List<Double> embedding) { }
}