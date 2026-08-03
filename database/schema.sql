-- ====================================================
-- ShopSense AI Database Schema Initialization (Phase 1)
-- ====================================================

CREATE DATABASE IF NOT EXISTS shopsense_db;
USE shopsense_db;

-- -----------------------------------------------------
-- Table `users`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Initial Seed Data (Password: "password123" BCrypt encoded)
-- $2a$10$e.w2.W5uJg0cQG2pZzGZEO4a2S5Lqg7B2XyJg2S5Lqg7B2XyJg2S5 (will be populated dynamically via application)
