-- =============================================================================
-- MySQL Initialization Script for Employee Management System
-- =============================================================================
-- This script runs automatically when the MySQL container is first created.
-- It sets up the database and initial configuration.
-- =============================================================================

-- Create database if not exists (already created by MYSQL_DATABASE env var)
-- This is just for safety
CREATE DATABASE IF NOT EXISTS employee_management_db;

USE employee_management_db;

-- Grant all privileges to the application user
GRANT ALL PRIVILEGES ON employee_management_db.* TO 'emsuser'@'%';
FLUSH PRIVILEGES;

-- Optional: Create initial admin user
-- Password is BCrypt encoded: admin123
-- You can use https://bcrypt-generator.com/ to generate password hashes
-- INSERT INTO users (username, email, password, first_name, last_name, role, enabled, account_non_locked, created_at)
-- VALUES ('admin', 'admin@company.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
--         'System', 'Administrator', 'ADMIN', true, true, NOW())
-- ON DUPLICATE KEY UPDATE username=username;

-- Log completion
SELECT 'Database initialization completed successfully' AS Status;
