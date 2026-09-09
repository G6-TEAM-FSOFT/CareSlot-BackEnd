package com.org.care_slot.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiRequest(
        SystemInstruction system_instruction,
        List<Content> contents
) {
    public record SystemInstruction(List<Part> parts) {}
    public record Content(String role, List<Part> parts) {}
    public record Part(String text) {}

    public static GeminiRequest create(String systemPrompt, List<Content> existingHistory, String userMessage) {
        SystemInstruction instruction = new SystemInstruction(List.of(new Part(systemPrompt)));
        
        List<Content> updatedContents = new ArrayList<>();
        if (existingHistory != null) {
            updatedContents.addAll(existingHistory);
        }
        updatedContents.add(new Content("user", List.of(new Part(userMessage))));
        
        return new GeminiRequest(instruction, updatedContents);
    }
}
