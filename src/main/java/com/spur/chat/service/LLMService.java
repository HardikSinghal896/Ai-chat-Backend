package com.spur.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spur.chat.entity.Message;
import com.spur.chat.exception.LLMException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LLMService {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.max.tokens}")
    private int maxTokens;

    @Value("${openai.temperature}")
    private double temperature;

    private static final String SYSTEM_PROMPT = """
        You are a helpful customer support agent for "TechStore", a small e-commerce store.
        
        Store Information:
        - Shipping Policy: We offer free shipping on orders over $50. Standard shipping takes 5-7 business days. Express shipping (2-3 days) is available for $15.
        - Return Policy: Items can be returned within 30 days of purchase for a full refund. Items must be unused and in original packaging.
        - Support Hours: Monday to Friday, 9 AM - 6 PM EST. We respond to emails within 24 hours.
        - Payment Methods: We accept Visa, Mastercard, American Express, and PayPal.
        - International Shipping: We ship to USA, Canada, UK, and EU countries.
        
        Answer customer questions clearly, concisely, and professionally. If you don't know something, politely say so and offer to connect them with a human agent.
        """;

    public LLMService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public String generateReply(List<Message> conversationHistory, String userMessage) {
        try {
            ObjectNode requestBody = buildRequestBody(conversationHistory, userMessage);

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(
                            requestBody.toString(),
                            MediaType.parse("application/json")
                    ))
                    .build();

            log.debug("Sending request to OpenAI API");

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    log.error("OpenAI API error: {} - {}", response.code(), errorBody);

                    if (response.code() == 401) {
                        throw new LLMException("Invalid API key. Please check your configuration.");
                    } else if (response.code() == 429) {
                        throw new LLMException("Rate limit exceeded. Please try again later.");
                    } else {
                        throw new LLMException("AI service temporarily unavailable. Please try again.");
                    }
                }

                String responseBody = response.body().string();
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                return jsonResponse
                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText();
            }

        } catch (IOException e) {
            log.error("Network error calling OpenAI API", e);
            throw new LLMException("Network error. Please check your connection and try again.");
        } catch (Exception e) {
            log.error("Unexpected error calling OpenAI API", e);
            throw new LLMException("An unexpected error occurred. Please try again.");
        }
    }

    private ObjectNode buildRequestBody(List<Message> history, String userMessage) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", model);
        root.put("max_tokens", maxTokens);
        root.put("temperature", temperature);

        ArrayNode messages = root.putArray("messages");

        // System prompt
        ObjectNode systemMsg = messages.addObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", SYSTEM_PROMPT);

        // Conversation history (limit to last 10 messages)
        int startIdx = Math.max(0, history.size() - 10);
        for (int i = startIdx; i < history.size(); i++) {
            Message msg = history.get(i);
            ObjectNode historyMsg = messages.addObject();
            historyMsg.put("role", msg.getSender() == Message.SenderType.USER ? "user" : "assistant");
            historyMsg.put("content", msg.getText());
        }

        // Current user message
        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);

        return root;
    }
}
