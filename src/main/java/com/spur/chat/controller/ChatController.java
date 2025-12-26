package com.spur.chat.controller;

import com.spur.chat.dto.ChatMessageRequest;
import com.spur.chat.dto.ChatMessageResponse;
import com.spur.chat.entity.Message;
import com.spur.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/message")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @Valid @RequestBody ChatMessageRequest request) {

        try {
            log.info("Received message: {} for session: {}", request.getMessage(), request.getSessionId());

            Message aiMessage = chatService.sendMessage(
                    request.getMessage(),
                    request.getSessionId()
            );

            return ResponseEntity.ok(ChatMessageResponse.success(
                    aiMessage.getText(),
                    aiMessage.getConversation().getId()
            ));

        } catch (Exception e) {
            log.error("Error processing message", e);
            return ResponseEntity.ok(ChatMessageResponse.error(
                    "An error occurred processing your message. Please try again.",
                    request.getSessionId()
            ));
        }
    }

    @GetMapping("/history/{conversationId}")
    public ResponseEntity<List<Message>> getHistory(
            @PathVariable Long conversationId) {

        List<Message> history = chatService.getConversationHistory(conversationId);
        return ResponseEntity.ok(history);
    }
}
