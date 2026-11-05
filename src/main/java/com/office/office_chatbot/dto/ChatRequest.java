package com.office.office_chatbot.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(UUID sessionId, @NotBlank @Size(max = 4000) String message) { }
