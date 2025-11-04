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
            You are a comprehensive email parser that extracts complete order information from emails.
            
            ALWAYS respond with ONLY valid JSON in this exact format:
            {
                "isOrderRelatedEmail": boolean,
                "isNewOrder": boolean,
                "orderId": "string or null",
                "productName": "string or null",
                "price": "number or null",
                "quantity": number or null,
                "productLink": "string or null",
                "orderDate": "ISO 8601 datetime string or null",
                "deliveryDate": "ISO 8601 datetime string or null",
                "shipmentStatus": "ordered|shipped|out_of_delivery|delivered|cancelled or null",
                "vendor": "vendor name or null",
                "platform": "platform name or null",
                "categoryMatches": ["array of matching categories from provided list or empty"]
            }
            
            COMPREHENSIVE EXTRACTION RULES:
            
            1. ORDER IDENTIFICATION:
               - Look for order confirmations, shipping notifications, delivery updates
               - Extract order IDs from patterns like: Order #123456, Order ID: ABC123, Confirmation #XYZ789, #XYZ789
               - isNewOrder: true for new order confirmations, false for updates/tracking
            
            2. PRODUCT INFORMATION:
               - Extract product name/description from the email content
               - Extract product price if available (can be total or unit price)
               - Extract product quantity if available
               - Try to extract product links/URLs from email content
            
            3. DATE EXTRACTION:
               - Use ISO 8601 format for all dates: YYYY-MM-DDTHH:MM:SSZ
               - orderDate: When the order was placed (typically current date if not specified)
               - deliveryDate: Expected or actual delivery date (infer from "Delivery on" or "Expected delivery")
               - If date is not available, use current date for orderDate
            
            4. STATUS MAPPING (MUST use only these values):
               - "ordered": Initial order placed, pending processing
               - "shipped": Order has been dispatched/picked up for delivery
               - "out_of_delivery": Order is with the delivery partner, out for delivery
               - "delivered": Order successfully delivered
               - "cancelled": Order has been cancelled
               - Map common terms: pending->ordered, in-transit->shipped, out-for-delivery->out_of_delivery
            
            5. VENDOR & PLATFORM EXTRACTION:
               - vendor: Extract seller/merchant name from email sender domain or email content
               - platform: Extract e-commerce platform from email domain or content
               - Common platforms: Amazon, Flipkart, eBay, Walmart, Target, Best Buy, Myntra, Ajio, Snapdeal
               - Common vendor patterns: seller email domain, "sold by", "merchant", company name in email
               - If not clearly identifiable, return null
            
            6. CATEGORY MATCHING:
               - Match product names or email content against provided user categories
               - Return array of matching category names
               - Be generous with matching but ensure relevance
               - Empty array if no matches found
            
            7. DEMO MODE - GENERATE REALISTIC VALUES FOR MISSING DATA:
               - If productName is missing: Generate realistic product name based on keywords in email
               - If price is missing: Generate realistic price (10-999 range based on product type)
               - If quantity is missing: Default to 1
               - If orderId is missing: Generate format like "ORD-XXXXXX" or "AMZ-XXXXXX" based on platform
               - If orderDate is missing: Use current date
               - If deliveryDate is missing: Generate realistic delivery date (5-14 days from order date)
               - If productLink is missing: Generate realistic URL based on platform and product
               - Examples:
                 * For electronics: $299-$999
                 * For clothing: $20-$150
                 * For books: $10-$40
                 * For home goods: $30-$200
            
            8. FINAL RULES:
               - Only return JSON, no explanations
               - NEVER include markdown code fences in response
               - If not order-related, set isOrderRelatedEmail to false, isNewOrder to false, and other fields to null
               - Ensure quantity is always a number (default 1)
               - Ensure all monetary values are numbers, not strings
               - All dates must be in ISO 8601 format or null
            """;
        
        logger.info("Gemini Email Analysis Service initialized with comprehensive system prompt");
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
            String vendor = resultNode.path("vendor").isNull() ? null : resultNode.path("vendor").asText();
            String platform = resultNode.path("platform").isNull() ? null : resultNode.path("platform").asText();
            
            // Parse category matches
            List<String> categoryMatches = new java.util.ArrayList<>();
            JsonNode categoryNode = resultNode.path("categoryMatches");
            if (categoryNode.isArray()) {
                for (JsonNode catMatch : categoryNode) {
                    categoryMatches.add(catMatch.asText());
                }
            }
            
            logger.info("Parsed email analysis result: isOrderEmail={}, isNewOrder={}, orderId={}, price={}, quantity={}, status={}, vendor={}, platform={}, categories={}", 
                isOrderEmail, isNewOrder, orderId, price, quantity, shipmentStatus, vendor, platform, categoryMatches);
            
            return new EmailAnalysisResult(isOrderEmail, isNewOrder, orderId, productName, price, quantity, productLink, orderDate, deliveryDate, shipmentStatus, vendor, platform, categoryMatches);
            
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
        private String vendor;
        private String platform;
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
            this.vendor = null;
            this.platform = null;
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
            this.vendor = null;
            this.platform = null;
            this.categoryMatches = categoryMatches != null ? categoryMatches : List.of();
        }
        
        public EmailAnalysisResult(boolean isOrderRelatedEmail, boolean isNewOrder, String orderId, 
                                   String productName, Double price, Integer quantity, String productLink,
                                   String orderDate, String deliveryDate, String shipmentStatus, String vendor, 
                                   String platform, List<String> categoryMatches) {
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
            this.vendor = vendor;
            this.platform = platform;
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
        
        public String getVendor() {
            return vendor;
        }
        
        public String getPlatform() {
            return platform;
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
        
        public void setVendor(String vendor) {
            this.vendor = vendor;
        }
        
        public void setPlatform(String platform) {
            this.platform = platform;
        }
        
        public void setCategoryMatches(List<String> categoryMatches) {
            this.categoryMatches = categoryMatches != null ? categoryMatches : List.of();
        }
    }
}
