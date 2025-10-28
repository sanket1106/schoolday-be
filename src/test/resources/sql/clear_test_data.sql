-- Clear test data in the correct order to respect foreign key constraints
-- This script should be executed after each test to ensure clean state

USE `schoolday_test`;

-- Clear user sessions first (depends on user)
DELETE FROM `user_session` WHERE `user_id` NOT IN (
    SELECT `id` FROM `user` WHERE `id` IN ('user_1', 'user_2', 'user_3', 'user_4', 'user_5', 'user_6', 'user_7', 'user_8', 'user_9', 'user_10')
);

-- Clear parent-child relationships (depends on user and child)
DELETE FROM `parent_child` WHERE `parent_id` NOT IN (
    SELECT `id` FROM `user` WHERE `id` IN ('user_1', 'user_2', 'user_3', 'user_4', 'user_5', 'user_6', 'user_7', 'user_8', 'user_9', 'user_10')
) OR `child_id` NOT IN (
    SELECT `id` FROM `child` WHERE `id` IN ('child_1', 'child_2', 'child_3', 'child_4', 'child_5')
);

-- Clear user roles (depends on user and role)
DELETE FROM `user_role` WHERE `user_id` NOT IN (
    SELECT `id` FROM `user` WHERE `id` IN ('user_1', 'user_2', 'user_3', 'user_4', 'user_5', 'user_6', 'user_7', 'user_8', 'user_9', 'user_10')
) OR `role_id` NOT IN (
    SELECT `id` FROM `role` WHERE `id` IN ('role_1', 'role_2', 'role_3')
);

-- Clear test users (keep base users)
DELETE FROM `user` WHERE `id` NOT IN ('user_1', 'user_2', 'user_3', 'user_4', 'user_5', 'user_6', 'user_7', 'user_8', 'user_9', 'user_10');

-- Clear test children (keep base children)
DELETE FROM `child` WHERE `id` NOT IN ('child_1', 'child_2', 'child_3', 'child_4', 'child_5');

-- Clear test roles (keep base roles)
DELETE FROM `role` WHERE `id` NOT IN ('role_1', 'role_2', 'role_3'); 