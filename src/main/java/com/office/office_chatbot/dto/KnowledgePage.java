package com.office.office_chatbot.dto;

import java.util.List;

import com.office.office_chatbot.entity.Knowledge;

public record KnowledgePage(List<Knowledge> records, int page, int size, long totalElements) { }
