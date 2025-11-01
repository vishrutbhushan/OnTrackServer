-- Database schema for OnTrack application

-- Drop existing tables if they exist
DROP TABLE IF EXISTS item;
DROP TABLE IF EXISTS users;

-- Create Users table (unified user management with tokens)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    access_token VARCHAR(2000),
    fcm_token VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_email (email)
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
