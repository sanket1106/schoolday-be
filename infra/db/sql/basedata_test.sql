USE `schoolday_test`;

INSERT INTO `user` (`id`, `email`, `password`, `first_name`, `last_name`, `created`, `updated`, `status`)
VALUES
        ('user_1', 'sanket1106@gmail.com', 'password', 'Sanket', 'Apte', NOW(), NOW(), 'ACTIVE'),
        ('user_2', 'niluka@gmail.com', 'password', 'Niluka', 'A', NOW(), NOW(), 'ACTIVE'),
        ('user_3', 'kshruts@gmail.com', 'password', 'Shruti', 'Kulkarni', NOW(), NOW(), 'ACTIVE'),
        ('user_4', 'elena@gmail.com', 'password', 'Elena', 'K', NOW(), NOW(), 'ACTIVE'),
        ('user_5', 'andrew@gmail.com', 'password', 'Andrew', 'Yohanna', NOW(), NOW(), 'ACTIVE'),
        ('user_6', 'chapa@gmail.com', 'password', 'Chapa', 'Yohanna', NOW(), NOW(), 'ACTIVE'),
        ('user_7', 'luisa@gmail.com', 'password', 'Luisa', 'Daniel', NOW(), NOW(), 'ACTIVE'),
        ('user_8', 'alan@gmail.com', 'password', 'Alan', 'Ruck', NOW(), NOW(), 'ACTIVE'),
        ('user_9', 'kavita@gmail.com', 'password', 'Kavita', 'T', NOW(), NOW(), 'ACTIVE'),
        ('user_10', 'Archit@gmail.com', 'password', 'Archit', 'T', NOW(), NOW(), 'ACTIVE');

INSERT INTO `role` (`id`, `name`, `status`, `created`, `updated`, `permissions`)
VALUES
        ('role_1', 'ADMIN', 'ENABLED', NOW(), NOW(), 'CREATE_USER,MANAGE_CHILD'),
        ('role_2', 'TEACHER', 'ENABLED', NOW(), NOW(), NULL),
        ('role_3', 'PARENT', 'ENABLED', NOW(), NOW(), 'MANAGE_CHILD');

INSERT INTO `user_role` (`user_id`, `role_id`, `status`, `created`, `updated`)
VALUES
        ('user_2', 'role_1', 'ENABLED', NOW(), NOW()),
        ('user_2', 'role_2', 'ENABLED', NOW(), NOW()),
        ('user_4', 'role_2', 'ENABLED', NOW(), NOW()),
        ('user_1', 'role_3', 'ENABLED', NOW(), NOW()),
        ('user_3', 'role_3', 'ENABLED', NOW(), NOW()),
        ('user_4', 'role_3', 'ENABLED', NOW(), NOW()),
        ('user_5', 'role_3', 'ENABLED', NOW(), NOW()),
        ('user_6', 'role_3', 'ENABLED', NOW(), NOW()),
        ('user_7', 'role_3', 'ENABLED', NOW(), NOW()),
        ('user_8', 'role_3', 'ENABLED', NOW(), NOW()),
        ('user_9', 'role_3', 'ENABLED', NOW(), NOW()),
        ('user_10', 'role_3', 'ENABLED', NOW(), NOW());

INSERT INTO `child` (`id`, `first_name`, `last_name`, `date_of_birth`, `status`, `created`, `updated`)
VALUES
        ('child_1', 'Samyra', 'Apte', '2021-03-10', 'ACTIVE', NOW(), NOW()),
        ('child_2', 'Sansheya', 'Yohanna', '2021-03-03', 'ACTIVE', NOW(), NOW()),
        ('child_3', 'Ahaana', 'T', '2021-05-31', 'ACTIVE', NOW(), NOW()),
        ('child_4', 'Sophie', 'Ruck', '2021-02-22', 'ACTIVE', NOW(), NOW()),
        ('child_5', 'Emilia', 'Ruck', '2024-05-14', 'ACTIVE', NOW(), NOW());

exit; 