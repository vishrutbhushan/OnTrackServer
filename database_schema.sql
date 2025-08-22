-- Database schema for OnTrack application

-- Drop existing tables if they exist
DROP TABLE IF EXISTS fcm_tokens;
DROP TABLE IF EXISTS item;
DROP TABLE IF EXISTS token;

-- Create Token table
CREATE TABLE token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) UNIQUE NOT NULL,
    access_token VARCHAR(2000),
    refresh_token VARCHAR(2000),
    email VARCHAR(255)
);

-- Create Item table (for emails)
CREATE TABLE item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject VARCHAR(500),
    snippet TEXT,
    sender VARCHAR(255),
    user_id VARCHAR(255),
    INDEX idx_user_id (user_id)
);

-- Create FCM Tokens table (for push notifications)
CREATE TABLE fcm_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) UNIQUE NOT NULL,
    fcm_token VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
);
