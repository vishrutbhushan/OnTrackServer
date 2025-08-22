package com.project.onTrackServer.controller;

import com.project.onTrackServer.model.Token;
import com.project.onTrackServer.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing user authentication tokens
 */
@RestController
@RequestMapping("/api/tokens")
@CrossOrigin(origins = "*")
public class TokenController {

    @Autowired
    private TokenRepository tokenRepository;

    /**
     * Save or update a user's token
     * @param token The token to save
     * @return Saved token
     */
    @PostMapping
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

    /**
     * Get a user's token
     * @param userId The user ID
     * @return User's token if found
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Token> getToken(@PathVariable String userId) {
        return tokenRepository.findByUserId(userId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Check if user has a valid token
     * @param userId The user ID
     * @return True if user has valid token
     */
    @GetMapping("/exists/{userId}")
    public ResponseEntity<Boolean> tokenExists(@PathVariable String userId) {
        boolean hasValidToken = tokenRepository.findByUserId(userId)
            .map(token -> token.getAccessToken() != null && 
                         !token.getAccessToken().equals("gmail_access_granted") &&
                         !token.getAccessToken().trim().isEmpty())
            .orElse(false);
        return ResponseEntity.ok(hasValidToken);
    }
}
