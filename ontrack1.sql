
/*
OnTrack - Workbench-ready SQL schema
MySQL (InnoDB) - fully relational with audit columns and views for visualization
Save this file and open in MySQL Workbench -> Run to create schema/tables.
*/

DROP DATABASE IF EXISTS ontrack2;
CREATE DATABASE ontrack2 CHARACTER SET = 'utf8mb4' COLLATE = 'utf8mb4_unicode_ci';
USE ontrack2;

-- =====================================================
-- Helper: Ensure the server supports strict mode for checks if available
-- =====================================================

-- =====================================================
-- USERS
-- =====================================================
CREATE TABLE users (
    user_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    name            VARCHAR(255),
    auth_token      TEXT,
    fcm_token       TEXT,
    push_notification_enabled TINYINT(1) DEFAULT 1
                     COMMENT '1 = push notifications ON, 0 = OFF',
	polling_frequency ENUM('15 minutes', '30 minutes', '1 hour', '2 hours') DEFAULT '30 minutes' COMMENT 'Static polling interval',
    is_deleted      TINYINT(1) DEFAULT 0,
    create_user     BIGINT NULL,
    update_user     BIGINT NULL,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =====================================================
-- USER-VENDOR MAP (Many-to-Many Relationship)
-- =====================================================
CREATE TABLE user_vendor_map (
    user_vendor_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    fk_user         BIGINT NOT NULL,
    fk_vendor       BIGINT NOT NULL,

    is_deleted      TINYINT(1) DEFAULT 0,
    create_user     BIGINT NULL,
    update_user     BIGINT NULL,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (fk_user) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (fk_vendor) REFERENCES vendor(vendor_id) ON DELETE CASCADE,

    UNIQUE KEY ux_user_vendor (fk_user, fk_vendor)
) ENGINE=InnoDB COMMENT='Static vendor association per user';

-- =====================================================
-- ECOMMERCE PLATFORM
-- =====================================================
CREATE TABLE ecommerce_platform (
    platform_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    avg_rating      DECIMAL(3,2),
    api_endpoint    VARCHAR(500),

    is_deleted      TINYINT(1) DEFAULT 0,
    create_user     BIGINT NULL,
    update_user     BIGINT NULL,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE ecommerce_platform_reviews (
    review_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    platform_id     BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    rating          TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 10),
    comment         TEXT,
    was_delayed     TINYINT(1) DEFAULT 0,
    was_bad_quality TINYINT(1) DEFAULT 0,

    is_deleted      TINYINT(1) DEFAULT 0,
    create_user     BIGINT NULL,
    update_user     BIGINT NULL,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (platform_id) REFERENCES ecommerce_platform(platform_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- VENDOR & REVIEWS
-- =====================================================
CREATE TABLE vendor (
    vendor_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    avg_rating      DECIMAL(3,2),
    can_delay       TINYINT CHECK (can_delay BETWEEN 1 AND 10),
    can_be_bad_quality TINYINT CHECK (can_be_bad_quality BETWEEN 1 AND 10),

    is_deleted      TINYINT(1) DEFAULT 0,
    create_user     BIGINT NULL,
    update_user     BIGINT NULL,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

INSERT INTO vendor (name, avg_rating, can_delay, can_be_bad_quality)
VALUES
('Amazon',   9.5, 2, 1),
('Flipkart', 9.0, 3, 2),
('Myntra',   8.8, 2, 3),
('Ajio',     8.5, 3, 2),
('TataCliq', 8.2, 4, 2),
('Snapdeal', 7.9, 4, 3),
('DHL',   8.0, 3, 4),
('Nykaa',    8.7, 2, 2),
('Croma',    8.9, 3, 1),
('Reliance Digital', 8.6, 3, 2);

CREATE TABLE vendor_reviews (
    review_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_id       BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    rating          TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 10),
    comment         TEXT,
    was_delayed     TINYINT(1) DEFAULT 0,
    was_bad_shape   TINYINT(1) DEFAULT 0,

    is_deleted      TINYINT(1) DEFAULT 0,
    create_user     BIGINT NULL,
    update_user     BIGINT NULL,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (vendor_id) REFERENCES vendor(vendor_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- LOGISTIC PARTNER & REVIEWS
-- =====================================================
CREATE TABLE logistic_partner (
    logistic_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    avg_rating      DECIMAL(3,2),
    api_endpoint    VARCHAR(500),
    can_delay       TINYINT CHECK (can_delay BETWEEN 1 AND 10),
    can_be_bad_quality TINYINT CHECK (can_be_bad_quality BETWEEN 1 AND 10),

    is_deleted      TINYINT(1) DEFAULT 0,
    create_user     BIGINT NULL,
    update_user     BIGINT NULL,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE logistic_partner_reviews (
    review_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    logistic_id     BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    rating          TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 10),
    comment         TEXT,
    was_delayed     TINYINT(1) DEFAULT 0,
    was_bad_shape   TINYINT(1) DEFAULT 0,

    is_deleted      TINYINT(1) DEFAULT 0,
    create_user     BIGINT NULL,
    update_user     BIGINT NULL,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (logistic_id) REFERENCES logistic_partner(logistic_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- GENERAL CATEGORY & TAGS
-- =====================================================
CREATE TABLE general_category (
    category_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,

    is_deleted      TINYINT(1) DEFAULT 0,
    create_user     BIGINT NULL,
    update_user     BIGINT NULL,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE tags (
    tag_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    name            VARCHAR(255) NOT NULL,

    is_deleted      TINYINT(1) DEFAULT 0,
    create_user     BIGINT NULL,
    update_user     BIGINT NULL,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY ux_user_tag (user_id, name)
) ENGINE=InnoDB;

-- =====================================================
-- ORDERS + ORDER ITEMS + SHIPMENT EVENTS
-- =====================================================
CREATE TABLE orders (
    order_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no        VARCHAR(255) NOT NULL,
    tracking_no     VARCHAR(255),
    order_date      DATETIME,
    current_status  VARCHAR(100),
    product_category VARCHAR(255),
    order_total     DECIMAL(12,2) DEFAULT 0.00, -- cached total; derived from order_items
    avg_rating      TINYINT CHECK (avg_rating BETWEEN 1 AND 10),
    bad_quality_possible TINYINT CHECK (bad_quality_possible BETWEEN 1 AND 10),
    delay_possible  TINYINT CHECK (delay_possible BETWEEN 1 AND 10),

    fk_vendor       BIGINT,
    fk_logistic_partner BIGINT,
    fk_user         BIGINT NOT NULL,
    fk_category     BIGINT,
    fk_tag          BIGINT,
    fk_platform     BIGINT,

    is_deleted      TINYINT(1) DEFAULT 0,
    create_user     BIGINT NULL,
    update_user     BIGINT NULL,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (fk_vendor) REFERENCES vendor(vendor_id) ON DELETE SET NULL,
    FOREIGN KEY (fk_logistic_partner) REFERENCES logistic_partner(logistic_id) ON DELETE SET NULL,
    FOREIGN KEY (fk_user) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (fk_category) REFERENCES general_category(category_id) ON DELETE SET NULL,
    FOREIGN KEY (fk_tag) REFERENCES tags(tag_id) ON DELETE SET NULL,
    FOREIGN KEY (fk_platform) REFERENCES ecommerce_platform(platform_id) ON DELETE SET NULL,

    INDEX idx_orders_user (fk_user),
    INDEX idx_orders_tracking (tracking_no),
    INDEX idx_orders_status (current_status)
) ENGINE=InnoDB;

CREATE TABLE order_items (
    item_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT NOT NULL,
    sku             VARCHAR(255),
    title           VARCHAR(500),
    quantity        INT DEFAULT 1,
    unit_price      DECIMAL(12,2) DEFAULT 0.00,
    total_price     DECIMAL(12,2) GENERATED ALWAYS AS (quantity * unit_price) VIRTUAL,

    is_deleted      TINYINT(1) DEFAULT 0,
    create_user     BIGINT NULL,
    update_user     BIGINT NULL,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    INDEX idx_order_items_order (order_id)
) ENGINE=InnoDB;

CREATE TABLE shipment_events (
    event_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT NULL,  -- nullable because custom tracking may be external
    custom_tracking_id BIGINT NULL,
    logistic_id     BIGINT NULL,
    event_time      DATETIME,
    status          VARCHAR(100),
    location        VARCHAR(255),
    raw_payload     JSON, -- store API webhook/responses if available

    is_deleted      TINYINT(1) DEFAULT 0,
    create_user     BIGINT NULL,
    update_user     BIGINT NULL,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (logistic_id) REFERENCES logistic_partner(logistic_id) ON DELETE SET NULL,
    INDEX idx_shipment_order (order_id),
    INDEX idx_shipment_logistic (logistic_id),
    INDEX idx_shipment_status (status)
) ENGINE=InnoDB;

-- =====================================================
-- CUSTOM TRACKING ORDERS (manually added tracking numbers)
-- =====================================================
CREATE TABLE custom_tracking_orders (
    custom_tracking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    tracking_no     VARCHAR(255) NOT NULL,
    description     VARCHAR(1000),
    last_status     VARCHAR(255),

    is_deleted      TINYINT(1) DEFAULT 0,
    create_user     BIGINT NULL,
    update_user     BIGINT NULL,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY ux_user_tracking (user_id, tracking_no)
) ENGINE=InnoDB;

-- Link custom tracking events to shipment_events via custom_tracking_id if needed
ALTER TABLE shipment_events
    ADD CONSTRAINT fk_shipment_custom_tracking FOREIGN KEY (custom_tracking_id) REFERENCES custom_tracking_orders(custom_tracking_id) ON DELETE CASCADE;

-- =====================================================
-- USER PREFERENCES
-- =====================================================
CREATE TABLE user_preferences (
    preference_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL UNIQUE,
    declutter       TINYINT(1) DEFAULT 0,
    notifications   TINYINT(1) DEFAULT 1,

    is_deleted      TINYINT(1) DEFAULT 0,
    create_user     BIGINT NULL,
    update_user     BIGINT NULL,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

