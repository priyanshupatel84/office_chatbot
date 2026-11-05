package com.office.office_chatbot.dto;

import java.util.List;
import java.util.UUID;

public record ChatResponse(UUID sessionId, String answer, List<SourceDTO> sources) { }
