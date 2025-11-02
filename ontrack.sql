DROP DATABASE IF EXISTS ontrack;
CREATE DATABASE ontrack CHARACTER SET = 'utf8mb4' COLLATE = 'utf8mb4_unicode_ci';
USE ontrack;

CREATE TABLE users (
    user_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    name            VARCHAR(255),
    age             INT NULL,
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

-- USER-VENDOR MAP (Many-to-Many Relationship)
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

CREATE TABLE user_notification (
    notification_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
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

CREATE TABLE logistic_provider (
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

CREATE TABLE product_categories(
	product_categories_no		BIGINT NOT NULL PRIMARY KEY,
    product_categories			VARCHAR(250),
    
    is_deleted      TINYINT(1) DEFAULT 0,
    create_user     BIGINT NULL,
    update_user     BIGINT NULL,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE orders (
    order_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no        VARCHAR(255) NOT NULL,
    price_of_item   BIGINT,
    product_link    VARCHAR(500),
    tracking_no     VARCHAR(255),
    order_date      DATETIME,
    current_status  VARCHAR(100),
    product_category VARCHAR(255),
    order_total     DECIMAL(12,2) DEFAULT 0.00, -- cached total; derived from order_items
    avg_rating      TINYINT CHECK (avg_rating BETWEEN 1 AND 10),
    bad_quality_possible TINYINT CHECK (bad_quality_possible BETWEEN 1 AND 10),
    delay_possible  TINYINT CHECK (delay_possible BETWEEN 1 AND 10),

    fk_vendor       BIGINT,
    fk_logistic_provider BIGINT,
    fk_user         BIGINT NOT NULL,
    fk_ecommerce_platform BIGINT,
    fk_product_categories BIGINT,


    is_deleted      TINYINT(1) DEFAULT 0,
    create_user     BIGINT NULL,
    update_user     BIGINT NULL,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (fk_vendor) REFERENCES vendor(vendor_id) ON DELETE SET NULL,
    FOREIGN KEY (fk_logistic_provider) REFERENCES logistic_provider(logistic_id) ON DELETE SET NULL,
    FOREIGN KEY (fk_user) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (fk_ecommerce_platform) REFERENCES ecommerce_platform(platform_id) ON DELETE SET NULL,
	FOREIGN KEY (fk_product_categories) REFERENCES product_categories(product_categories_no) ON DELETE SET NULL,
    
    INDEX idx_orders_user (fk_user),
    INDEX idx_orders_tracking (tracking_no),
    INDEX idx_orders_status (current_status)
) ENGINE=InnoDB;

