package com.office.office_chatbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chatbot")
public class ChatbotConfig {
	private final Ollama ollama = new Ollama();
	private final Embedding embedding = new Embedding();
	private final Retrieval retrieval = new Retrieval();
	private final Knowledge knowledge = new Knowledge();
	private final Conversation conversation = new Conversation();
	private final Prompt prompt = new Prompt();

	public Ollama getOllama() { return ollama; }
	public Embedding getEmbedding() { return embedding; }
	public Retrieval getRetrieval() { return retrieval; }
	public Knowledge getKnowledge() { return knowledge; }
	public Conversation getConversation() { return conversation; }
	public Prompt getPrompt() { return prompt; }

	public static class Ollama {
		private String baseUrl = "http://localhost:11434";
		private String chatModel = "qwen3:8b";
		private int timeoutSeconds = 120;
		public String getBaseUrl() { return baseUrl; }
		public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
		public String getChatModel() { return chatModel; }
		public void setChatModel(String chatModel) { this.chatModel = chatModel; }
		public int getTimeoutSeconds() { return timeoutSeconds; }
		public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
	}

	public static class Embedding {
		private String model = "nomic-embed-text-v2-moe";
		private int dimensions = 768;
		public String getModel() { return model; }
		public void setModel(String model) { this.model = model; }
		public int getDimensions() { return dimensions; }
		public void setDimensions(int dimensions) { this.dimensions = dimensions; }
	}

	public static class Retrieval {
		private int topK = 5;
		private double similarityThreshold = 0.35;
		public int getTopK() { return topK; }
		public void setTopK(int topK) { this.topK = topK; }
		public double getSimilarityThreshold() { return similarityThreshold; }
		public void setSimilarityThreshold(double similarityThreshold) { this.similarityThreshold = similarityThreshold; }
	}

	public static class Knowledge {
		private String file = "knowledge/knowledge-base.json";
		public String getFile() { return file; }
		public void setFile(String file) { this.file = file; }
	}

	public static class Conversation {
		private int historyLimit = 12;
		public int getHistoryLimit() { return historyLimit; }
		public void setHistoryLimit(int historyLimit) { this.historyLimit = historyLimit; }
	}

	public static class Prompt {
		private String location = "classpath:prompts/cloud-support-prompt-template.txt";
		public String getLocation() { return location; }
		public void setLocation(String location) { this.location = location; }
	}
}
