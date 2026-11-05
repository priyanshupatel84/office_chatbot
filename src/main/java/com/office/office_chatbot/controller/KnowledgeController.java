package com.office.office_chatbot.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import com.office.office_chatbot.dto.IngestionResult;
import com.office.office_chatbot.dto.KnowledgePage;
import com.office.office_chatbot.entity.Knowledge;
import com.office.office_chatbot.exception.ResourceNotFoundException;
import com.office.office_chatbot.repository.KnowledgeRepository;
import com.office.office_chatbot.service.KnowledgeIngestionService;

@RestController
@RequestMapping("/api/admin/knowledge")
public class KnowledgeController {
	private final KnowledgeIngestionService ingestionService;
	private final KnowledgeRepository knowledgeRepository;

	public KnowledgeController(KnowledgeIngestionService ingestionService, KnowledgeRepository knowledgeRepository) {
		this.ingestionService = ingestionService;
		this.knowledgeRepository = knowledgeRepository;
	}

	@PostMapping("/ingest")
	public IngestionResult ingest() {
		return ingestionService.ingest();
	}

	@PostMapping("/reindex")
	public IngestionResult reindex() {
		return ingestionService.reindex();
	}

	@GetMapping
	public KnowledgePage list(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		if (page < 0 || size < 1 || size > 100) {
			throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100");
		}
		long offset = (long) page * size;
		if (offset > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("page is too large");
		}
		return new KnowledgePage(knowledgeRepository.findPage(size, (int) offset), page, size,
				knowledgeRepository.count());
	}

	@GetMapping("/{id}")
	public Knowledge get(@PathVariable long id) {
		Knowledge knowledge = knowledgeRepository.findById(id);
		if (knowledge == null) {
			throw new ResourceNotFoundException("Knowledge record was not found");
		}
		return knowledge;
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable long id) {
		if (!knowledgeRepository.delete(id)) {
			throw new ResourceNotFoundException("Knowledge record was not found");
		}
		return ResponseEntity.noContent().build();
	}
}
