package com.project.onTrackServer.controller;

import com.project.onTrackServer.model.Item;
import com.project.onTrackServer.model.Token;
import com.project.onTrackServer.repository.ItemRepository;
import com.project.onTrackServer.repository.TokenRepository;
import com.project.onTrackServer.service.GmailService;
import com.project.onTrackServer.service.GeminiEmailAnalysisService;
import com.project.onTrackServer.exception.GmailAuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

/**
 * Controller for managing email items
 */
@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TokenRepository tokenRepository;
    
    @Autowired
    private GmailService gmailService;
    
    @Autowired
    private GeminiEmailAnalysisService emailAnalysisService;

    /**
     * Get all items
     * @return List of all items
     */
    @GetMapping
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    /**
     * Create a new item
     * @param item The item to create
     * @return Created item
     */
    @PostMapping
    public Item createItem(@RequestBody Item item) {
        return itemRepository.save(item);
    }

    /**
     * Get item by ID
     * @param id The item ID
     * @return Item if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        Optional<Item> item = itemRepository.findById(id);
        return item.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * Get items for a specific user
     * @param userId The user ID
     * @return List of user's items
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Item>> getUserItems(@PathVariable String userId) {
        List<Item> items = itemRepository.findByUserIdOrderByIdDesc(userId);
        return ResponseEntity.ok(items);
    }
    
    /**
     * Fetch and store emails from Gmail for a user
     * @param userId The user ID
     * @return List of fetched and stored emails
     */
    @PostMapping("/fetch/{userId}")
    public ResponseEntity<List<Item>> fetchAndStoreEmails(@PathVariable String userId) {
        Token token = tokenRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("No token found for user: " + userId));
        
        if (token.getAccessToken() == null || "gmail_access_granted".equals(token.getAccessToken())) {
            throw new IllegalStateException("No valid Gmail access token found for user: " + userId);
        }
        
        List<Item> emailItems = gmailService.fetchNewEmailsFromGmail(token, itemRepository);
        List<Item> processedEmails = new ArrayList<>();
        
        for (Item email : emailItems) {
            // Analyze email with Gemini AI
            GeminiEmailAnalysisService.EmailAnalysisResult analysis = 
                emailAnalysisService.analyzeEmail(email.getSnippet(), email.getSubject(), email.getSender());
            
            // Only save order-related emails
            if (analysis.isOrderRelatedEmail()) {
                email.setOrderId(analysis.getOrderId());
                
                // Store the order ID in snippet field as requested
                String originalSnippet = email.getSnippet();
                String enhancedSnippet = analysis.getOrderId() != null ? 
                    "Order ID: " + analysis.getOrderId() + " | " + originalSnippet : originalSnippet;
                email.setSnippet(enhancedSnippet);
                
                Item savedEmail = itemRepository.save(email);
                processedEmails.add(savedEmail);
            }
        }
        
        return ResponseEntity.ok(processedEmails);
    }
    
    /**
     * Test endpoint for Gemini email analysis
     * @param request Test email data
     * @return Analysis result
     */
    @PostMapping("/test-analysis")
    public ResponseEntity<GeminiEmailAnalysisService.EmailAnalysisResult> testEmailAnalysis(@RequestBody TestEmailRequest request) {
        GeminiEmailAnalysisService.EmailAnalysisResult result = 
            emailAnalysisService.analyzeEmail(request.getContent(), request.getSubject(), request.getSender());
        return ResponseEntity.ok(result);
    }
    
    public static class TestEmailRequest {
        private String subject;
        private String content;
        private String sender;
        
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
    
    @ExceptionHandler(GmailAuthenticationException.class)
    public ResponseEntity<String> handleGmailAuthentication(GmailAuthenticationException e) {
        return ResponseEntity.status(401).body("Gmail authentication required: " + e.getMessage());
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body("Error: " + e.getMessage());
    }
}
