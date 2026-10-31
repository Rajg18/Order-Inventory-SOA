-- Initialization script for MySQL in Docker Compose
CREATE DATABASE IF NOT EXISTS `order_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `inventory_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant permissions
GRANT ALL PRIVILEGES ON `order_db`.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON `inventory_db`.* TO 'root'@'%';
FLUSH PRIVILEGES;
