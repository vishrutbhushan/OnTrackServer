package com.project.onTrackServer.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.List;

@Service
public class GeminiEmailAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(GeminiEmailAnalysisService.class);
    
    @Value("${gemini.api.key}")
    private String geminiApiKey;
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private String systemPrompt;
    
    public GeminiEmailAnalysisService() {
        this.webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com/v1beta")
            .build();
        this.objectMapper = new ObjectMapper();
    }
    
    @PostConstruct
    public void initializeSystemPrompt() {
        // One-time prompt setup - reused for all emails
        this.systemPrompt = """
            You are an email parser that extracts order information from emails.
            
            ALWAYS respond with ONLY valid JSON in this exact format:
            {
                "isOrderRelatedEmail": boolean,
                "orderId": "string or null"
            }
            
            Rules:
            - Look for order confirmations, shipping notifications, delivery updates
            - Extract order IDs from patterns like: Order #123456, Order ID: ABC123, Confirmation #XYZ789
            - Common platforms: Amazon, eBay, Walmart, Target, Best Buy, Shopify stores
            - Use null for missing order ID
            - Only return JSON, no explanations
            - If not order-related, set isOrderRelatedEmail to false and orderId to null
            """;
        
        logger.info("Gemini Email Analysis Service initialized with system prompt");
    }
    
    public EmailAnalysisResult analyzeEmail(String emailContent, String subject, String sender) {
        try {
            String emailData = String.format("Subject: %s\nFrom: %s\nContent: %s", subject, sender, emailContent);
            
            var requestBody = Map.of(
                "contents", List.of(
                    Map.of(
                        "parts", List.of(
                            Map.of("text", systemPrompt + "\n\nAnalyze this email:\n" + emailData)
                        )
                    )
                ),
                "generationConfig", Map.of(
                    "temperature", 0.1,
                    "maxOutputTokens", 200,
                    "topP", 0.8,
                    "topK", 10
                )
            );
            
            String response = webClient.post()
                .uri("/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
                
            return parseGeminiResponse(response);
            
        } catch (Exception e) {
            logger.error("Error analyzing email with Gemini: {}", e.getMessage());
            return new EmailAnalysisResult(false, null);
        }
    }
    
    private EmailAnalysisResult parseGeminiResponse(String response) {
        try {
            JsonNode jsonResponse = objectMapper.readTree(response);
            String generatedText = jsonResponse
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();
                
            // Clean up the response to extract JSON
            String jsonContent = extractJsonFromText(generatedText);
            
            JsonNode resultNode = objectMapper.readTree(jsonContent);
            boolean isOrderEmail = resultNode.path("isOrderRelatedEmail").asBoolean(false);
            String orderId = resultNode.path("orderId").isNull() ? null : resultNode.path("orderId").asText();
            
            return new EmailAnalysisResult(isOrderEmail, orderId);
            
        } catch (Exception e) {
            logger.error("Error parsing Gemini response: {}", e.getMessage());
            return new EmailAnalysisResult(false, null);
        }
    }
    
    private String extractJsonFromText(String text) {
        // Remove any markdown formatting or extra text
        text = text.trim();
        if (text.startsWith("```json")) {
            text = text.substring(7);
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }
        
        // Find JSON object boundaries
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}") + 1;
        
        if (start >= 0 && end > start) {
            return text.substring(start, end);
        }
        
        return text;
    }
    
    public static class EmailAnalysisResult {
        private boolean isOrderRelatedEmail;
        private String orderId;
        
        public EmailAnalysisResult() {}
        
        public EmailAnalysisResult(boolean isOrderRelatedEmail, String orderId) {
            this.isOrderRelatedEmail = isOrderRelatedEmail;
            this.orderId = orderId;
        }
        
        public boolean isOrderRelatedEmail() { 
            return isOrderRelatedEmail; 
        }
        
        public void setOrderRelatedEmail(boolean orderRelatedEmail) { 
            this.isOrderRelatedEmail = orderRelatedEmail; 
        }
        
        public String getOrderId() { 
            return orderId; 
        }
        
        public void setOrderId(String orderId) { 
            this.orderId = orderId; 
        }
    }
}
