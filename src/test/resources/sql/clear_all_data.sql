-- Clear all data including base data
-- This script completely resets the test database
-- Use this when you need complete isolation between tests

USE `schoolday_test`;

-- Disable foreign key checks temporarily to allow deletion in any order
SET FOREIGN_KEY_CHECKS = 0;

-- Clear all data from all tables
DELETE FROM `user_session`;
DELETE FROM `parent_child`;
DELETE FROM `user_role`;
DELETE FROM `child`;
DELETE FROM `user`;
DELETE FROM `role`;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1; 