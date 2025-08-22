-- Database schema for OnTrack application

-- Drop existing tables if they exist
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
