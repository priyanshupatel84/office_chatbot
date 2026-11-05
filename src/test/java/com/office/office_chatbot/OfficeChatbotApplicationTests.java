package com.office.office_chatbot;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.office.office_chatbot.model.KnowledgeRecord;
import com.office.office_chatbot.service.KnowledgeIngestionService;

class OfficeChatbotApplicationTests {
	@Test
	void searchableTextIncludesEveryKnowledgeField() {
		KnowledgeRecord record = new KnowledgeRecord("Delete a Service Bus topic",
				List.of("Why is delete disabled?", "How do I delete a topic?"), "Remove child references first.",
				List.of("Azure", "Service Bus"));

		assertEquals("Scenario: Delete a Service Bus topic\n\nUser Queries: Why is delete disabled?; How do I delete a topic?"
				+ "\n\nSolution: Remove child references first.\n\nTags: Azure, Service Bus",
				KnowledgeIngestionService.searchableText(record));
	}
}
