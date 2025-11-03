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
                "isNewOrder": boolean,
                "orderId": "string or null",
                "productName": "string or null",
                "price": "number or null",
                "quantity": 1,
                "productLink": "string or null",
                "orderDate": "ISO 8601 datetime string or null",
                "deliveryDate": "ISO 8601 datetime string or null",
                "shipmentStatus": "PENDING|SHIPPED|DELIVERED|CANCELLED or null",
                "categoryMatches": ["array of matching categories from provided list or empty"]
            }
            
            Rules:
            - Look for order confirmations, shipping notifications, delivery updates
            - Extract order IDs from patterns like: Order #123456, Order ID: ABC123, Confirmation #XYZ789
            - Common platforms: Amazon, eBay, Walmart, Target, Best Buy, Shopify stores
            - isNewOrder: true for new order confirmations, false for updates/tracking
            - Extract product price and quantity if available
            - Try to extract product links/URLs from email content
            - shipmentStatus: Use available status keywords. Common: PENDING (order placed), SHIPPED (on way), DELIVERED (arrived), CANCELLED
            - Use ISO 8601 format for dates: YYYY-MM-DDTHH:MM:SSZ
            - categoryMatches: Match product names or content against provided categories, return matching ones
            - Use null for missing fields
            - Only return JSON, no explanations
            - If not order-related, set isOrderRelatedEmail to false and other fields to null/false
            """;
        
        logger.info("Gemini Email Analysis Service initialized with system prompt");
    }
    
    public EmailAnalysisResult analyzeEmail(String emailContent, String subject, String sender, List<String> userCategories) {
        final int MAX_RETRIES = 3;
        final long INITIAL_WAIT_MS = 20000; // Start with 20 seconds
        
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                // Add delay before making API call to avoid rate limiting
                if (attempt > 0) {
                    long waitTime = INITIAL_WAIT_MS * (long) Math.pow(2, attempt - 1); // Exponential backoff
                    logger.info("Retrying Gemini API (attempt {}/{}), waiting {} ms", attempt + 1, MAX_RETRIES, waitTime);
                    Thread.sleep(waitTime);
                } else {
                    // Add small delay even on first attempt to space out API calls
                    Thread.sleep(2000);
                }
                
                String emailData = String.format("Subject: %s\nFrom: %s\nContent: %s", subject, sender, emailContent);
                
                // Include user categories in the prompt
                String categoriesInfo = userCategories != null && !userCategories.isEmpty() 
                    ? "\nUser categories to match against: " + String.join(", ", userCategories)
                    : "\nNo user categories provided.";
                
                var requestBody = Map.of(
                    "contents", List.of(
                        Map.of(
                            "parts", List.of(
                                Map.of("text", systemPrompt + categoriesInfo + "\n\nAnalyze this email:\n" + emailData)
                            )
                        )
                    ),
                    "generationConfig", Map.of(
                        "temperature", 0.1,
                        "maxOutputTokens", 500,
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
                String errorMsg = e.getMessage();
                
                // Check if it's a rate limit error
                if (errorMsg != null && errorMsg.contains("429")) {
                    if (attempt < MAX_RETRIES - 1) {
                        logger.warn("Rate limited by Gemini API (429), will retry. Attempt {}/{}", attempt + 1, MAX_RETRIES);
                        continue;
                    } else {
                        logger.error("Rate limited by Gemini API after {} attempts: {}", MAX_RETRIES, errorMsg);
                    }
                } else {
                    logger.error("Error analyzing email with Gemini (attempt {}/{}): {}", attempt + 1, MAX_RETRIES, errorMsg);
                }
                
                if (attempt == MAX_RETRIES - 1) {
                    logger.error("Failed to analyze email after {} retries", MAX_RETRIES);
                    return new EmailAnalysisResult(false, false, null, null, null, 1, null, null, null, null);
                }
            }
        }
        
        return new EmailAnalysisResult(false, false, null, null, null, 1, null, null, null, null);
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
                
            logger.debug("Raw Gemini response text: {}", generatedText.substring(0, Math.min(200, generatedText.length())));
            
            // Clean up the response to extract JSON
            String jsonContent = extractJsonFromText(generatedText);
            logger.debug("Extracted JSON: {}", jsonContent.substring(0, Math.min(300, jsonContent.length())));
            
            JsonNode resultNode = objectMapper.readTree(jsonContent);
            
            boolean isOrderEmail = resultNode.path("isOrderRelatedEmail").asBoolean(false);
            boolean isNewOrder = resultNode.path("isNewOrder").asBoolean(false);
            String orderId = resultNode.path("orderId").isNull() ? null : resultNode.path("orderId").asText();
            String productName = resultNode.path("productName").isNull() ? null : resultNode.path("productName").asText();
            Double price = resultNode.path("price").isNull() ? null : resultNode.path("price").asDouble();
            Integer quantity = resultNode.path("quantity").isNull() ? 1 : resultNode.path("quantity").asInt();
            String productLink = resultNode.path("productLink").isNull() ? null : resultNode.path("productLink").asText();
            String orderDate = resultNode.path("orderDate").isNull() ? null : resultNode.path("orderDate").asText();
            String deliveryDate = resultNode.path("deliveryDate").isNull() ? null : resultNode.path("deliveryDate").asText();
            String shipmentStatus = resultNode.path("shipmentStatus").isNull() ? null : resultNode.path("shipmentStatus").asText();
            
            // Parse category matches
            List<String> categoryMatches = new java.util.ArrayList<>();
            JsonNode categoryNode = resultNode.path("categoryMatches");
            if (categoryNode.isArray()) {
                for (JsonNode catMatch : categoryNode) {
                    categoryMatches.add(catMatch.asText());
                }
            }
            
            logger.info("Parsed email analysis result: isOrderEmail={}, isNewOrder={}, orderId={}, price={}, quantity={}, status={}, categories={}", 
                isOrderEmail, isNewOrder, orderId, price, quantity, shipmentStatus, categoryMatches);
            
            return new EmailAnalysisResult(isOrderEmail, isNewOrder, orderId, productName, price, quantity, productLink, orderDate, deliveryDate, shipmentStatus, categoryMatches);
            
        } catch (Exception e) {
            logger.error("Error parsing Gemini response: {}", e.getMessage(), e);
            return new EmailAnalysisResult(false, false, null, null, null, 1, null, null, null, null);
        }
    }
    
    private String extractJsonFromText(String text) {
        logger.debug("Extracting JSON from text of length: {}", text.length());
        
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
            logger.debug("JSON boundaries found: start={}, end={}", start, end);
            return text.substring(start, end);
        }
        
        logger.warn("Could not find JSON boundaries in response");
        return text;
    }
    
    public static class EmailAnalysisResult {
        private boolean isOrderRelatedEmail;
        private boolean isNewOrder;
        private String orderId;
        private String productName;
        private Double price;
        private Integer quantity;
        private String productLink;
        private String orderDate;
        private String deliveryDate;
        private String shipmentStatus;
        private List<String> categoryMatches;
        
        public EmailAnalysisResult() {}
        
        public EmailAnalysisResult(boolean isOrderRelatedEmail, boolean isNewOrder, String orderId, 
                                   String productName, Double price, Integer quantity, String productLink,
                                   String orderDate, String deliveryDate, String shipmentStatus) {
            this.isOrderRelatedEmail = isOrderRelatedEmail;
            this.isNewOrder = isNewOrder;
            this.orderId = orderId;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
            this.productLink = productLink;
            this.orderDate = orderDate;
            this.deliveryDate = deliveryDate;
            this.shipmentStatus = shipmentStatus;
            this.categoryMatches = List.of();
        }
        
        public EmailAnalysisResult(boolean isOrderRelatedEmail, boolean isNewOrder, String orderId, 
                                   String productName, Double price, Integer quantity, String productLink,
                                   String orderDate, String deliveryDate, String shipmentStatus, List<String> categoryMatches) {
            this.isOrderRelatedEmail = isOrderRelatedEmail;
            this.isNewOrder = isNewOrder;
            this.orderId = orderId;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
            this.productLink = productLink;
            this.orderDate = orderDate;
            this.deliveryDate = deliveryDate;
            this.shipmentStatus = shipmentStatus;
            this.categoryMatches = categoryMatches != null ? categoryMatches : List.of();
        }
        
        // Getters
        public boolean isOrderRelatedEmail() { 
            return isOrderRelatedEmail; 
        }
        
        public boolean isNewOrder() { 
            return isNewOrder; 
        }
        
        public String getOrderId() { 
            return orderId; 
        }
        
        public String getProductName() { 
            return productName; 
        }
        
        public Double getPrice() { 
            return price; 
        }
        
        public Integer getQuantity() { 
            return quantity; 
        }
        
        public String getProductLink() { 
            return productLink; 
        }
        
        public String getOrderDate() { 
            return orderDate; 
        }
        
        public String getDeliveryDate() { 
            return deliveryDate; 
        }
        
        public String getShipmentStatus() { 
            return shipmentStatus; 
        }
        
        public List<String> getCategoryMatches() {
            return categoryMatches;
        }
        
        // Setters
        public void setOrderRelatedEmail(boolean orderRelatedEmail) { 
            this.isOrderRelatedEmail = orderRelatedEmail; 
        }
        
        public void setNewOrder(boolean newOrder) { 
            this.isNewOrder = newOrder; 
        }
        
        public void setOrderId(String orderId) { 
            this.orderId = orderId; 
        }
        
        public void setProductName(String productName) { 
            this.productName = productName; 
        }
        
        public void setPrice(Double price) { 
            this.price = price; 
        }
        
        public void setQuantity(Integer quantity) { 
            this.quantity = quantity; 
        }
        
        public void setProductLink(String productLink) { 
            this.productLink = productLink; 
        }
        
        public void setOrderDate(String orderDate) { 
            this.orderDate = orderDate; 
        }
        
        public void setDeliveryDate(String deliveryDate) { 
            this.deliveryDate = deliveryDate; 
        }
        
        public void setShipmentStatus(String shipmentStatus) { 
            this.shipmentStatus = shipmentStatus; 
        }
        
        public void setCategoryMatches(List<String> categoryMatches) {
            this.categoryMatches = categoryMatches != null ? categoryMatches : List.of();
        }
    }
}
