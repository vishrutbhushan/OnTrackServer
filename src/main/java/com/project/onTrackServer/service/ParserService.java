package com.project.onTrackServer.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.onTrackServer.Models.Order;

@Service
public class ParserService {

    private static final Logger logger = LoggerFactory.getLogger(ParserService.class);

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final WebClient webClient = WebClient.builder().baseUrl("https://generativelanguage.googleapis.com/v1beta").build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public List<Order> extractOrdersFromEmail(Email email) {
        try {
            String prompt = buildPrompt(email);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", prompt)))),
                    "generationConfig", Map.of("temperature", 0.2, "maxOutputTokens", 200));

            String response = webClient.post()
                    .uri("/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseOrders(response);
        } catch (Exception e) {
            System.out.println("Failed to parse order from email: {}", e.getMessage());
            return null;
        }
    }

    private String buildPrompt(Email email) {
        return """
                Extract order details from the following email.
                Only return a JSON array of orders.
                Example output:
                [
                  {"orderId":"12345","price":499.0,"quantity":1,"shipmentStatus":"shipped"}
                ]

                If no orders found return [].

                EMAIL:
                Subject: """ + email.getSubject() + "\nFrom: " + email.getFrom() + "\nBody: " + email.getBody();
    }

    private List<Order> parseOrders(String response) {
        List<Order> orders = new ArrayList<>();
        try {
            JsonNode res = objectMapper.readTree(response)
                    .path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text");

            JsonNode orderArray = objectMapper.readTree(res.asText());
            for (JsonNode node : orderArray) {
                Order order = new Order();
                order.setOrderId(node.path("orderId").asText(null));
                order.setPrice(
                        node.path("price").isDouble() ? BigDecimal.valueOf(node.path("price").asDouble()) : null);
                order.setQuantity(node.path("quantity").asInt(1));
                order.setShipmentStatus(node.path("shipmentStatus").asText(null));
                orders.add(order);
            }
        } catch (Exception e) {
            System.out.println("Error parsing Gemini order response: {}", e.getMessage());
        }

        return orders;
    }
}
