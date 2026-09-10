package com.org.care_slot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatConsultRequest(
        @NotBlank(message = "Nội dung câu hỏi không được để trống")
        @Size(max = 500, message = "Nội dung câu hỏi không được vượt quá 500 ký tự")
        String message
) {}
