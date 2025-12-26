package com.spur.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private String reply;
    private Long sessionId;
    private boolean success;
    private String error;

    public static ChatMessageResponse success(String reply, Long sessionId) {
        return new ChatMessageResponse(reply, sessionId, true, null);
    }

    public static ChatMessageResponse error(String error, Long sessionId) {
        return new ChatMessageResponse(null, sessionId, false, error);
    }
}