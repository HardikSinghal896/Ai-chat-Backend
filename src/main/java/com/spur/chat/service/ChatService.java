package com.spur.chat.service;

import com.spur.chat.entity.Conversation;
import com.spur.chat.entity.Message;
import com.spur.chat.exception.LLMException;
import com.spur.chat.repository.ConversationRepository;
import com.spur.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final LLMService llmService;

    @Transactional
    public Message sendMessage(String messageText, Long sessionId) {
        // Get or create conversation
        Conversation conversation = getOrCreateConversation(sessionId);

        // Save user message
        Message userMessage = new Message();
        userMessage.setConversation(conversation);
        userMessage.setSender(Message.SenderType.USER);
        userMessage.setText(messageText);
        messageRepository.save(userMessage);

        log.info("Saved user message for conversation {}", conversation.getId());

        try {
            // Get conversation history
            List<Message> history = messageRepository.findByConversationIdOrderByTimestampAsc(conversation.getId());

            // Generate AI reply
            String aiReply = llmService.generateReply(history, messageText);

            // Save AI message
            Message aiMessage = new Message();
            aiMessage.setConversation(conversation);
            aiMessage.setSender(Message.SenderType.AI);
            aiMessage.setText(aiReply);
            messageRepository.save(aiMessage);

            log.info("Saved AI message for conversation {}", conversation.getId());

            return aiMessage;

        } catch (LLMException e) {
            log.error("LLM error for conversation {}: {}", conversation.getId(), e.getMessage());

            // Save error message as AI response
            Message errorMessage = new Message();
            errorMessage.setConversation(conversation);
            errorMessage.setSender(Message.SenderType.AI);
            errorMessage.setText("I apologize, but I'm having trouble processing your request right now. " + e.getMessage());
            messageRepository.save(errorMessage);

            return errorMessage;
        }
    }

    @Transactional(readOnly = true)
    public List<Message> getConversationHistory(Long conversationId) {
        return messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    private Conversation getOrCreateConversation(Long sessionId) {
        if (sessionId != null) {
            return conversationRepository.findById(sessionId)
                    .orElseGet(() -> conversationRepository.save(new Conversation()));
        }
        // If no sessionId provided, create a new conversation
        return conversationRepository.save(new Conversation());
    }
}
