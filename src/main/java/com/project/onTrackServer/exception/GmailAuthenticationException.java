package com.project.onTrackServer.exception;

/**
 * Exception thrown when Gmail API authentication fails
 */
public class GmailAuthenticationException extends RuntimeException {
    
    public GmailAuthenticationException(String message) {
        super(message);
    }
    
    public GmailAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
