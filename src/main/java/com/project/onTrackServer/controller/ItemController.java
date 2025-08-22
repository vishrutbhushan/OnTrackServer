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
@CrossOrigin(origins = "*")
public class ItemController {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TokenRepository tokenRepository;
    
    @Autowired
    private GmailService gmailService;

    // Token endpoints
    @PostMapping("/tokens")
    public ResponseEntity<Token> saveToken(@RequestBody Token token) {
        Token existingToken = tokenRepository.findByUserId(token.getUserId())
            .map(existing -> {
                existing.setAccessToken(token.getAccessToken());
                existing.setRefreshToken(token.getRefreshToken());
                existing.setEmail(token.getEmail());
                return existing;
            })
            .orElse(token);
        
        return ResponseEntity.ok(tokenRepository.save(existingToken));
    }

    @GetMapping("/tokens/{userId}")
    public ResponseEntity<Token> getToken(@PathVariable String userId) {
        return tokenRepository.findByUserId(userId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/tokens/exists/{userId}")
    public ResponseEntity<Boolean> tokenExists(@PathVariable String userId) {
        boolean hasValidToken = tokenRepository.findByUserId(userId)
            .map(token -> token.getAccessToken() != null && 
                         !token.getAccessToken().equals("gmail_access_granted") &&
                         !token.getAccessToken().trim().isEmpty())
            .orElse(false);
        return ResponseEntity.ok(hasValidToken);
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
        List<Item> items = itemRepository.findByUserIdOrderByIdDesc(userId);
        return ResponseEntity.ok(items);
    }
    
    @PostMapping("/items/fetch/{userId}")
    public ResponseEntity<List<Item>> fetchAndStoreEmails(@PathVariable String userId) {
        Token token = tokenRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("No token found for user: " + userId));
        
        if (token.getAccessToken() == null || "gmail_access_granted".equals(token.getAccessToken())) {
            throw new IllegalStateException("No valid Gmail access token found for user: " + userId);
        }
        
        List<Item> emailItems = gmailService.fetchEmailsFromGmail(token);
        List<Item> savedEmails = emailItems.stream()
                .map(itemRepository::save)
                .toList();
        
        return ResponseEntity.ok(savedEmails);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body("Error: " + e.getMessage());
    }
}
