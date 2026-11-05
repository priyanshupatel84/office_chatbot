package com.office.office_chatbot.dto;

import java.util.List;

public record IngestionResult(int processed, int skipped, List<String> errors) { }