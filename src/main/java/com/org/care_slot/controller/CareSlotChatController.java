package com.org.care_slot.controller;

import com.org.care_slot.dto.request.ChatConsultRequest;
import com.org.care_slot.dto.request.GeminiRequest;
import com.org.care_slot.dto.response.ApiResponse;
import com.org.care_slot.security.CurrentUserProvider;
import com.org.care_slot.service.impl.CareSlotChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class CareSlotChatController {

    private final CareSlotChatService chatService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/welcome")
    public ApiResponse<String> getWelcomeMessage() {
        Long userId = getCurrentUserIdSafely();
        String welcomeMsg = chatService.generateWelcomeMessage(userId);
        return ApiResponse.success("Lấy lời chào thành công", welcomeMsg);
    }

    @PostMapping("/session/new")
    public ApiResponse<Map<String, String>> createNewSession() {
        String newSessionId = chatService.createNewSession();
        return ApiResponse.success("Tạo session thành công", Map.of("sessionId", newSessionId));
    }

    @GetMapping("/history")
    public ApiResponse<List<GeminiRequest.Content>> getHistory(@RequestParam String sessionId) {
        List<GeminiRequest.Content> history = chatService.getHistory(sessionId);
        return ApiResponse.success("Lấy lịch sử trò chuyện thành công", history);
    }

    @PostMapping("/consult")
    public ApiResponse<String> consult(
            @RequestParam String sessionId,
            @Valid @RequestBody ChatConsultRequest request,
            HttpServletRequest httpRequest) {

        Long userId = getCurrentUserIdSafely();
        String clientIp = getClientIp(httpRequest);

        String reply = chatService.chat(userId, clientIp, sessionId, request.message());
        return ApiResponse.success("Thành công", reply);
    }

    private Long getCurrentUserIdSafely() {
        try {
            return currentUserProvider.getCurrentPatientUserId();
        } catch (Exception e) {
            return null; // Guest user hoặc chưa đăng nhập
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isBlank()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
