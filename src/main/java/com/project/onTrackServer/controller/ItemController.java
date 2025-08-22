package com.project.onTrackServer.controller;

import com.project.onTrackServer.model.Item;
import com.project.onTrackServer.model.Token;
import com.project.onTrackServer.repository.ItemRepository;
import com.project.onTrackServer.repository.TokenRepository;
import com.project.onTrackServer.service.GmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow all origins for testing
public class ItemController {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TokenRepository tokenRepository;
    
    @Autowired
    private GmailService gmailService;

    // Token endpoints
    @PostMapping("/tokens")
    public ResponseEntity<?> saveToken(@RequestBody Token token) {
        try {
            // Check if token already exists for this user
            Optional<Token> existingToken = tokenRepository.findByUserId(token.getUserId());
            if (existingToken.isPresent()) {
                // Update existing token
                Token existing = existingToken.get();
                existing.setAccessToken(token.getAccessToken());
                existing.setRefreshToken(token.getRefreshToken());
                existing.setEmail(token.getEmail());
                return ResponseEntity.ok(tokenRepository.save(existing));
            } else {
                // Save new token
                return ResponseEntity.ok(tokenRepository.save(token));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error saving token: " + e.getMessage());
        }
    }

    @GetMapping("/tokens/{userId}")
    public ResponseEntity<?> getToken(@PathVariable String userId) {
        Optional<Token> token = tokenRepository.findByUserId(userId);
        if (token.isPresent()) {
            return ResponseEntity.ok(token.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/tokens/exists/{userId}")
    public ResponseEntity<Boolean> tokenExists(@PathVariable String userId) {
        Optional<Token> tokenOpt = tokenRepository.findByUserId(userId);
        if (tokenOpt.isPresent()) {
            Token token = tokenOpt.get();
            // Check if it's a valid token (not a placeholder)
            boolean hasValidToken = token.getAccessToken() != null && 
                                   !token.getAccessToken().equals("gmail_access_granted") &&
                                   !token.getAccessToken().trim().isEmpty();
            return ResponseEntity.ok(hasValidToken);
        }
        return ResponseEntity.ok(false);
    }

    // Item endpoints
    @GetMapping("/items")
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @PostMapping("/items")
    public Item createItem(@RequestBody Item item) {
        return itemRepository.save(item);
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        Optional<Item> item = itemRepository.findById(id);
        return item.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/items/user/{userId}")
    public ResponseEntity<List<Item>> getUserItems(@PathVariable String userId) {
        try {
            List<Item> items = itemRepository.findByUserIdOrderByIdDesc(userId);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/items/fetch/{userId}")
    public ResponseEntity<?> fetchAndStoreEmails(@PathVariable String userId) {
        try {
            // Get the user's token
            Optional<Token> tokenOpt = tokenRepository.findByUserId(userId);
            if (tokenOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("No token found for user: " + userId + ". Please grant Gmail permission first.");
            }
            
            Token token = tokenOpt.get();
            
            // Check if we have a real access token (not just a placeholder)
            if (token.getAccessToken() == null || "gmail_access_granted".equals(token.getAccessToken())) {
                return ResponseEntity.badRequest().body("No valid Gmail access token found for user: " + userId + ". Please grant Gmail permission first.");
            }
            
            // Fetch real emails from Gmail
            List<Item> emailItems = gmailService.fetchEmailsFromGmail(token);
            
            // Save emails to database
            List<Item> savedEmails = emailItems.stream()
                    .map(itemRepository::save)
                    .toList();
            
            return ResponseEntity.ok(savedEmails);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching emails: " + e.getMessage());
        }
    }
}
