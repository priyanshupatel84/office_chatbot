package com.office.office_chatbot.exception;

public class KnowledgeFileException extends RuntimeException {
	public KnowledgeFileException(String message) {
		super(message);
	}

	public KnowledgeFileException(String message, Throwable cause) {
		super(message, cause);
	}
}