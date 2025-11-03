package com.project.onTrackServer.controller;

import com.project.onTrackServer.model.Item;
import com.project.onTrackServer.model.User;
import com.project.onTrackServer.repository.ItemRepository;
import com.project.onTrackServer.repository.UserRepository;
import com.project.onTrackServer.service.GmailService;
import com.project.onTrackServer.exception.GmailAuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
    private UserRepository userRepository;
    
    @Autowired
    private GmailService gmailService;

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
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("No user found with ID: " + userId));
        
        if (user.getAccessToken() == null || "gmail_access_granted".equals(user.getAccessToken())) {
            throw new IllegalStateException("No valid Gmail access token found for user: " + userId);
        }
        
        List<Item> emailItems = gmailService.fetchEmailsFromGmail(user);
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
    
    @ExceptionHandler(GmailAuthenticationException.class)
    public ResponseEntity<String> handleGmailAuthentication(GmailAuthenticationException e) {
        return ResponseEntity.status(401).body("Gmail authentication required: " + e.getMessage());
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body("Error: " + e.getMessage());
    }
}
