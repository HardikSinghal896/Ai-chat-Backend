# Ai-chat-Backend
This is AI chat application backend which uses LLM API to produce the output
ai-live-chat/
├── backend/
│   ├── src/main/java/com/spur/chat/
│   │   ├── AiLiveChatApplication.java
│   │   ├── config/
│   │   │   ├── CorsConfig.java
│   │   │   └── OpenAIConfig.java
│   │   ├── controller/
│   │   │   └── ChatController.java
│   │   ├── dto/
│   │   │   ├── ChatMessageRequest.java
│   │   │   └── ChatMessageResponse.java
│   │   ├── entity/
│   │   │   ├── Conversation.java
│   │   │   └── Message.java
│   │   ├── repository/
│   │   │   ├── ConversationRepository.java
│   │   │   └── MessageRepository.java
│   │   ├── service/
│   │   │   ├── ChatService.java
│   │   │   └── LLMService.java
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.java
│   │       └── LLMException.java
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── data.sql
│   └── pom.xml
└── frontend/
    ├── src/
    │   ├── components/
    │   │   ├── ChatWidget.jsx
    │   │   └── Message.jsx
    │   ├── services/
    │   │   └── chatService.js
    │   ├── App.jsx
    │   ├── App.css
    │   └── main.jsx
    ├── package.json
    └── index.html
